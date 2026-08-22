package org.dromara.special.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.system.api.model.RegisterBody;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialParentRegisterServiceTest {

    private static final String PHONE = "13800138000";
    private static final String SMS_CODE = "1234";

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private ISysUserService userService;
    @Mock
    private ISysRoleService roleService;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    private SpecialParentRegisterService service;
    private RegisterBody body;

    @BeforeEach
    void setUp() {
        Function<String, String> smsCodes = phone -> SMS_CODE;
        service = new SpecialParentRegisterService(userMapper, userService, roleService, userRoleMapper, smsCodes, null);
        body = new RegisterBody();
        body.setUsername(PHONE);
        body.setPassword("pass123");
        body.setCode(SMS_CODE);
        body.setClientId(SpecialIdentitySupport.XCX_CLIENT_ID);
    }

    @Test
    void productionConstructorIsAutowiredForSpring() {
        Constructor<?> production = Arrays.stream(SpecialParentRegisterService.class.getDeclaredConstructors())
            .filter(ctor -> ctor.getParameterCount() == 5)
            .findFirst()
            .orElseThrow();
        assertTrue(production.isAnnotationPresent(Autowired.class));
        long autowiredCount = Arrays.stream(SpecialParentRegisterService.class.getDeclaredConstructors())
            .filter(ctor -> ctor.isAnnotationPresent(Autowired.class))
            .count();
        assertEquals(1, autowiredCount);
    }

    @Test
    void alreadyParentIsRejected() {
        SysUserVo existingParent = existingUser(10L);
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(existingParent);
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of(role("special_parent", 7L)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.register(body));
        assertEquals("账号已注册，请直接登录", ex.getMessage());
        verify(userService, never()).insertUser(any());
    }

    @Test
    void invalidPhoneIsRejected() {
        body.setUsername("admin");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.register(body));
        assertEquals("请输入正确的手机号", ex.getMessage());
    }

    @Test
    void mismatchedSmsCodeIsRejected() {
        body.setCode("0000");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.register(body));
        assertEquals("验证码无效", ex.getMessage());
    }

    @Test
    void wxPhoneCodeSkipsSmsAndRegistersResolvedPhone() {
        service = new SpecialParentRegisterService(
            userMapper, userService, roleService, userRoleMapper,
            phone -> {
                throw new AssertionError("SMS must be skipped when wxPhoneCode is present");
            },
            code -> {
                assertEquals("wx-phone-code", code);
                return PHONE;
            });
        body.setUsername("wxphone");
        body.setCode("0000");
        body.setWxPhoneCode("wx-phone-code");
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(null);
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_parent", 7L)));

        service.register(body);

        ArgumentCaptor<SysUserBo> captor = ArgumentCaptor.forClass(SysUserBo.class);
        verify(userService).insertUser(captor.capture());
        assertEquals(PHONE, captor.getValue().getUserName());
        assertEquals(PHONE, captor.getValue().getPhoneNumber());
    }

    @Test
    void createsParentWhenUserMissing() {
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(null);
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_parent", 7L)));

        service.register(body);

        ArgumentCaptor<SysUserBo> captor = ArgumentCaptor.forClass(SysUserBo.class);
        verify(userService).insertUser(captor.capture());
        SysUserBo created = captor.getValue();
        assertEquals(PHONE, created.getUserName());
        assertEquals(PHONE, created.getPhoneNumber());
        assertEquals(7L, created.getRoleIds()[0]);
        assertTrue(BCrypt.checkpw("pass123", created.getPassword()));
    }

    @Test
    void addsParentWhenUserExistsWithoutParent() {
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(existingUser(10L));
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of(role("special_teacher", 8L)));
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_parent", 7L)));

        service.register(body);

        verify(userRoleMapper).insert(argThat((SysUserRole ur) ->
            Long.valueOf(10L).equals(ur.getUserId()) && Long.valueOf(7L).equals(ur.getRoleId())));
        verify(userService).resetUserPwd(eq(10L), argThat(hash -> BCrypt.checkpw("pass123", hash)));
        verify(userService, never()).insertUser(any());
    }

    @Test
    void skipsPasswordUpdateWhenBlankOnExistingUser() {
        body.setPassword("  ");
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(existingUser(10L));
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of());
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_parent", 7L)));

        service.register(body);

        verify(userRoleMapper).insert(any(SysUserRole.class));
        verify(userService, never()).resetUserPwd(any(), any());
    }

    private static SysUserVo existingUser(Long userId) {
        SysUserVo user = new SysUserVo();
        user.setUserId(userId);
        user.setPhoneNumber(PHONE);
        user.setStatus(SystemConstants.NORMAL);
        return user;
    }

    private static SysRoleVo role(String roleKey, Long roleId) {
        SysRoleVo role = new SysRoleVo();
        role.setRoleKey(roleKey);
        role.setRoleId(roleId);
        role.setStatus(SystemConstants.NORMAL);
        return role;
    }
}
