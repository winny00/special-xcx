package org.dromara.special.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialAppointment;
import org.dromara.special.domain.bo.BindPhoneBody;
import org.dromara.special.domain.vo.SpecialBindPhoneVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.system.domain.SysSocial;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysSocialMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysPermissionService;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialMobileMeServiceImplBindPhoneTest {

    private static final Long CURRENT_USER_ID = 1001L;
    private static final Long PHONE_OWNER_ID = 2002L;
    private static final String PHONE = "13800138000";
    private static final String SMS_CODE = "1234";
    private static final String TOKEN = "keep-token";

    @Mock
    private ISysUserService userService;
    @Mock
    private ISpecialAppointmentService appointmentService;
    @Mock
    private SpecialAppointmentMapper appointmentMapper;
    @Mock
    private SysSocialMapper socialMapper;
    @Mock
    private ISysSocialService socialService;
    @Mock
    private ISysRoleService roleService;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private ISysClientService clientService;
    @Mock
    private ISysPermissionService permissionService;

    private SpecialMobileMeServiceImpl service;
    private BindPhoneBody body;

    @BeforeEach
    void setUp() {
        Function<String, String> smsCodes = phone -> SMS_CODE;
        service = new SpecialMobileMeServiceImpl(
            userService, appointmentService, appointmentMapper, socialMapper, socialService,
            roleService, userRoleMapper, clientService, permissionService, smsCodes);
        body = new BindPhoneBody();
        body.setPhone(PHONE);
        body.setSmsCode(SMS_CODE);
    }

    @Test
    void productionConstructorIsAutowiredForSpring() {
        Constructor<?> production = Arrays.stream(SpecialMobileMeServiceImpl.class.getDeclaredConstructors())
            .filter(ctor -> ctor.getParameterCount() == 9)
            .findFirst()
            .orElseThrow();
        assertTrue(production.isAnnotationPresent(Autowired.class));
    }

    @Test
    void invalidPhoneIsRejected() {
        body.setPhone("admin");
        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(CURRENT_USER_ID);
            ServiceException ex = assertThrows(ServiceException.class, () -> service.bindPhone(body));
            assertEquals("请输入正确的手机号", ex.getMessage());
        }
    }

    @Test
    void mismatchedSmsCodeIsRejected() {
        body.setSmsCode("0000");
        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(CURRENT_USER_ID);
            ServiceException ex = assertThrows(ServiceException.class, () -> service.bindPhone(body));
            assertEquals("验证码无效", ex.getMessage());
        }
    }

    @Test
    void rejectsWhenOpenidAlreadyBoundToAnotherAccount() {
        when(userService.selectUserById(CURRENT_USER_ID)).thenReturn(currentUser(null));
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(phoneOwner());
        SysSocialVo otherBinding = new SysSocialVo();
        otherBinding.setUserId(PHONE_OWNER_ID);
        otherBinding.setOpenId("oid-other");
        otherBinding.setAuthId("WECHAT_MINI_PROGRAM" + "oid-current");
        when(socialService.queryListByUserId(CURRENT_USER_ID)).thenReturn(List.of(currentSocial()));
        when(socialService.selectByAuthId("WECHAT_MINI_PROGRAMoid-current")).thenReturn(List.of(otherBinding));

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(CURRENT_USER_ID);
            ServiceException ex = assertThrows(ServiceException.class, () -> service.bindPhone(body));
            assertEquals("该微信已绑定其他账号", ex.getMessage());
        }
        verify(userService, never()).updateUserStatus(any(), any());
    }

    @Test
    void writesPhoneWhenNumberHasNoOwner() {
        when(userService.selectUserById(CURRENT_USER_ID)).thenReturn(currentUser(null));
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(null);
        when(socialService.queryListByUserId(CURRENT_USER_ID)).thenReturn(List.of());
        when(roleService.selectRolesByUserId(CURRENT_USER_ID)).thenReturn(List.of());
        when(roleService.selectRoleAll()).thenReturn(List.of(parentRole()));
        when(userService.updateUserProfile(any())).thenReturn(1);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(CURRENT_USER_ID);
            stp.when(StpUtil::getTokenValue).thenReturn(TOKEN);
            stp.when(StpUtil::getTokenTimeout).thenReturn(7200L);
            stp.when(() -> StpUtil.getExtra(LoginHelper.CLIENT_KEY)).thenReturn(SpecialIdentitySupport.XCX_CLIENT_ID);

            SpecialBindPhoneVo vo = service.bindPhone(body);

            assertEquals(TOKEN, vo.getAccessToken());
            assertEquals(7200L, vo.getExpireIn());
            assertEquals(SpecialIdentitySupport.XCX_CLIENT_ID, vo.getClientId());
        }
        ArgumentCaptor<SysUserBo> captor = ArgumentCaptor.forClass(SysUserBo.class);
        verify(userService).updateUserProfile(captor.capture());
        assertEquals(PHONE, captor.getValue().getPhoneNumber());
        assertEquals(CURRENT_USER_ID, captor.getValue().getUserId());
        verify(userRoleMapper).insert(argThat((SysUserRole ur) ->
            CURRENT_USER_ID.equals(ur.getUserId()) && Long.valueOf(7L).equals(ur.getRoleId())));
    }

    @Test
    void mergeMovesSocialAppointmentsAddsParentAndDisablesTempUser() {
        when(userService.selectUserById(CURRENT_USER_ID)).thenReturn(currentUser(null));
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(phoneOwner());
        when(socialService.queryListByUserId(CURRENT_USER_ID)).thenReturn(List.of(currentSocial()));
        when(socialService.selectByAuthId("WECHAT_MINI_PROGRAMoid-current")).thenReturn(List.of(currentSocial()));
        when(roleService.selectRolesByUserId(PHONE_OWNER_ID)).thenReturn(List.of());
        when(roleService.selectRoleAll()).thenReturn(List.of(parentRole()));
        when(userService.selectUserById(PHONE_OWNER_ID)).thenReturn(phoneOwner());
        when(permissionService.getMenuPermission(PHONE_OWNER_ID)).thenReturn(java.util.Set.of());
        when(permissionService.getRolePermission(PHONE_OWNER_ID)).thenReturn(java.util.Set.of("special_parent"));
        when(clientService.queryByClientId(SpecialIdentitySupport.XCX_CLIENT_ID)).thenReturn(xcxClient());
        SpecialAppointment appointment = new SpecialAppointment();
        appointment.setId(9L);
        appointment.setUserId(CURRENT_USER_ID);
        when(appointmentMapper.selectList(any())).thenReturn(List.of(appointment));
        SaSession session = mock(SaSession.class);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(CURRENT_USER_ID);
            helper.when(LoginHelper::getLoginUser).thenReturn(null);
            stp.when(StpUtil::getTokenValue).thenReturn(TOKEN);
            stp.when(StpUtil::getTokenTimeout).thenReturn(3600L);
            stp.when(() -> StpUtil.getExtra(LoginHelper.CLIENT_KEY)).thenReturn(SpecialIdentitySupport.XCX_CLIENT_ID);
            stp.when(StpUtil::getTokenSession).thenReturn(session);

            SpecialBindPhoneVo vo = service.bindPhone(body);

            assertEquals(TOKEN, vo.getAccessToken());
            assertEquals(3600L, vo.getExpireIn());
            assertEquals(SpecialIdentitySupport.XCX_CLIENT_ID, vo.getClientId());
        }
        verify(socialMapper).updateById(argThat((SysSocial s) ->
            PHONE_OWNER_ID.equals(s.getUserId()) && Long.valueOf(3L).equals(s.getId())));
        verify(appointmentMapper).updateById(argThat((SpecialAppointment a) ->
            PHONE_OWNER_ID.equals(a.getUserId())));
        verify(userRoleMapper).insert(argThat((SysUserRole ur) ->
            PHONE_OWNER_ID.equals(ur.getUserId()) && Long.valueOf(7L).equals(ur.getRoleId())));
        verify(userService).updateUserStatus(CURRENT_USER_ID, SystemConstants.DISABLE);
        verify(userService, never()).updateUserStatus(PHONE_OWNER_ID, SystemConstants.DISABLE);
    }

    private static SysUserVo currentUser(String phone) {
        SysUserVo user = new SysUserVo();
        user.setUserId(CURRENT_USER_ID);
        user.setUserName("wx_temp");
        user.setPhoneNumber(phone);
        user.setUserType("sys_user");
        user.setStatus(SystemConstants.NORMAL);
        return user;
    }

    private static SysUserVo phoneOwner() {
        SysUserVo user = new SysUserVo();
        user.setUserId(PHONE_OWNER_ID);
        user.setUserName(PHONE);
        user.setPhoneNumber(PHONE);
        user.setUserType("sys_user");
        user.setStatus(SystemConstants.NORMAL);
        return user;
    }

    private static SysSocialVo currentSocial() {
        SysSocialVo social = new SysSocialVo();
        social.setId(3L);
        social.setUserId(CURRENT_USER_ID);
        social.setOpenId("oid-current");
        social.setSource("WECHAT_MINI_PROGRAM");
        social.setAuthId("WECHAT_MINI_PROGRAMoid-current");
        return social;
    }

    private static SysRoleVo parentRole() {
        SysRoleVo role = new SysRoleVo();
        role.setRoleId(7L);
        role.setRoleKey(SpecialIdentitySupport.PARENT_ROLE_KEY);
        role.setStatus(SystemConstants.NORMAL);
        return role;
    }

    private static SysClientVo xcxClient() {
        SysClientVo client = new SysClientVo();
        client.setClientId(SpecialIdentitySupport.XCX_CLIENT_ID);
        client.setClientKey("xcx");
        client.setDeviceType("xcx");
        client.setTimeout(3600L);
        client.setActiveTimeout(1800L);
        return client;
    }
}
