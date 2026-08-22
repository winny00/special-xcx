package org.dromara.special.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAccountRolesBody;
import org.dromara.special.domain.vo.SpecialAccountRoleResult;
import org.dromara.special.mapper.SpecialAccountMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class SpecialAccountServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final String PHONE = "13800138000";

    @Mock
    private SpecialAccountMapper accountMapper;
    @Mock
    private SpecialTeacherMapper teacherMapper;
    @Mock
    private ISysUserService userService;
    @Mock
    private ISysRoleService roleService;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private SpecialAccountServiceImpl service;

    @Test
    void cannotDropLastRole() {
        SpecialAccountRolesBody body = new SpecialAccountRolesBody();
        body.setParent(false);
        body.setTeacher(false);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateRoles(USER_ID, body));
        assertEquals("至少保留一个角色", ex.getMessage());
    }

    @Test
    void checkingTeacherWithoutProfileAsksToCompleteArchive() {
        when(userService.selectUserById(USER_ID)).thenReturn(existingUser());
        when(teacherMapper.selectOne(any())).thenReturn(null);

        SpecialAccountRolesBody body = new SpecialAccountRolesBody();
        body.setParent(true);
        body.setTeacher(true);
        SpecialAccountRoleResult result = service.updateRoles(USER_ID, body);

        assertFalse(result.isSuccess());
        assertTrue(result.isNeedTeacherProfile());
        assertEquals("请先补全老师档案", result.getMessage());
        assertEquals(PHONE, result.getPhone());
        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    void uncheckingTeacherRemovesRoleButKeepsArchive() {
        when(userService.selectUserById(USER_ID)).thenReturn(existingUser());
        when(roleService.selectRolesByUserId(USER_ID)).thenReturn(List.of(
            role("special_parent", 7L), role("special_teacher", 8L)));
        when(roleService.selectRoleAll()).thenReturn(List.of(
            role("special_parent", 7L), role("special_teacher", 8L)));

        SpecialAccountRolesBody body = new SpecialAccountRolesBody();
        body.setParent(true);
        body.setTeacher(false);
        SpecialAccountRoleResult result = service.updateRoles(USER_ID, body);

        assertTrue(result.isSuccess());
        verify(userRoleMapper).delete(any());
        verify(teacherMapper, never()).deleteById(any(Long.class));
        verify(teacherMapper, never()).updateById(any(SpecialTeacher.class));
    }

    @Test
    void resetPasswordUsesBcrypt() {
        when(userService.selectUserById(USER_ID)).thenReturn(existingUser());

        service.resetPassword(USER_ID, "Abcd1234");

        verify(userService).resetUserPwd(eq(USER_ID), argThat(hash -> BCrypt.checkpw("Abcd1234", hash)));
    }

    private static SysUserVo existingUser() {
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        user.setPhoneNumber(PHONE);
        user.setNickName("家长");
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
