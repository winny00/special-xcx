package org.dromara.special.service.impl;

import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.vo.SpecialMobileProfileVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.util.SpecialCurrentRoleStore;
import org.dromara.system.api.domain.RoleDTO;
import org.dromara.system.api.model.LoginUser;
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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialMobileMeServiceImplGetProfileTest {

    private static final Long USER_ID = 1001L;

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
    @Mock
    private SpecialTeacherMapper teacherMapper;

    private SpecialMobileMeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SpecialMobileMeServiceImpl(
            userService, appointmentService, appointmentMapper, socialMapper, socialService,
            roleService, userRoleMapper, clientService, permissionService, teacherMapper,
            phone -> null);
    }

    @Test
    void currentRoleComesFromSessionStoreNotForcedParent() {
        SysUserVo user = user("13800138000");
        LoginUser loginUser = loginUserWithBothRoles();
        when(userService.selectUserById(USER_ID)).thenReturn(user);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            helper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            SpecialMobileProfileVo vo = service.getProfile();

            assertEquals("special_teacher", vo.getCurrentRole());
            assertEquals("special_teacher", vo.getRoleKey());
            assertEquals("特教老师", vo.getRoleName());
            assertTrue(vo.getRoles().contains("special_parent"));
            assertTrue(vo.getRoles().contains("special_teacher"));
            assertTrue(vo.getPhoneBound());
            assertEquals("138****8000", vo.getPhone());
        }
    }

    @Test
    void phoneBoundIsFalseAndPhoneBlankWhenUnbound() {
        SysUserVo user = user(null);
        LoginUser loginUser = loginUserWithBothRoles();
        when(userService.selectUserById(USER_ID)).thenReturn(user);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            helper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_parent");

            SpecialMobileProfileVo vo = service.getProfile();

            assertEquals("special_parent", vo.getCurrentRole());
            assertFalse(vo.getPhoneBound());
            assertEquals(null, vo.getPhone());
        }
    }

    private static SysUserVo user(String phone) {
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        user.setUserName("13800138000");
        user.setNickName("小明家长");
        user.setPhoneNumber(phone);
        return user;
    }

    private static LoginUser loginUserWithBothRoles() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(USER_ID);
        loginUser.setRolePermission(Set.of("special_parent", "special_teacher"));
        RoleDTO parent = new RoleDTO();
        parent.setRoleKey("special_parent");
        parent.setRoleName("特教家长");
        RoleDTO teacher = new RoleDTO();
        teacher.setRoleKey("special_teacher");
        teacher.setRoleName("特教老师");
        loginUser.setRoles(List.of(parent, teacher));
        return loginUser;
    }
}
