package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialTeacherServiceImplTest {

    private static final String PHONE = "13800138000";

    @Mock
    private SpecialTeacherMapper baseMapper;
    @Mock
    private ISysUserService userService;
    @Mock
    private ISysRoleService roleService;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private SpecialTeacherServiceImpl service;

    @Test
    void unpublishedTeacherCannotOpenOnMobile() {
        SpecialTeacher teacher = new SpecialTeacher();
        teacher.setId(1L);
        teacher.setStatus(0);
        when(baseMapper.selectById(1L)).thenReturn(teacher);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.queryApprovedById(1L));
        assertEquals("老师不存在或未通过审核", ex.getMessage());
    }

    @Test
    void approvedTeacherReturnsVo() {
        SpecialTeacher teacher = new SpecialTeacher();
        teacher.setId(1L);
        teacher.setStatus(1);
        when(baseMapper.selectById(1L)).thenReturn(teacher);
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1L);
        vo.setName("李老师");
        when(baseMapper.selectVoById(1L)).thenReturn(vo);

        assertEquals("李老师", service.queryApprovedById(1L).getName());
    }

    @Test
    void existingParentPhoneAddsTeacherRoleWithoutCreatingUser() {
        SysUserVo parent = new SysUserVo();
        parent.setUserId(10L);
        parent.setPhoneNumber(PHONE);
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(parent);
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of(role("special_parent", 7L)));
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_teacher", 8L)));
        when(baseMapper.selectOne(any())).thenReturn(null);

        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setName("周老师");
        bo.setPhone(PHONE);
        bo.setInitPassword("Abcd1234");

        Long userId = service.bindAccountByPhone(bo, null);

        assertEquals(10L, userId);
        verify(userService, never()).insertUser(any(SysUserBo.class));
        ArgumentCaptor<SysUserRole> captor = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper).insert(captor.capture());
        assertEquals(10L, captor.getValue().getUserId());
        assertEquals(8L, captor.getValue().getRoleId());
    }

    @Test
    void alreadyTeacherPhoneIsRejected() {
        SysUserVo teacherUser = new SysUserVo();
        teacherUser.setUserId(11L);
        teacherUser.setPhoneNumber(PHONE);
        when(userService.selectUserByPhoneNumber(PHONE)).thenReturn(teacherUser);
        when(roleService.selectRolesByUserId(11L)).thenReturn(List.of(role("special_teacher", 8L)));

        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setName("周老师");
        bo.setPhone(PHONE);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.bindAccountByPhone(bo, null));
        assertEquals("该老师账号已存在", ex.getMessage());
        verify(userService, never()).insertUser(any());
    }

    @Test
    void approvedDetailOmitsBoundLoginPhone() {
        SpecialTeacher teacher = new SpecialTeacher();
        teacher.setId(1L);
        teacher.setStatus(1);
        teacher.setUserId(10L);
        when(baseMapper.selectById(1L)).thenReturn(teacher);
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1L);
        vo.setUserId(10L);
        vo.setName("李老师");
        vo.setPhone(PHONE);
        when(baseMapper.selectVoById(1L)).thenReturn(vo);

        SpecialTeacherVo result = service.queryApprovedById(1L);

        assertNull(result.getPhone());
    }

    @Test
    void approvedListOmitsBoundLoginPhone() {
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1L);
        vo.setUserId(10L);
        vo.setName("李老师");
        vo.setPhone(PHONE);
        Page<SpecialTeacherVo> page = new Page<>(1, 10);
        page.setRecords(List.of(vo));
        page.setTotal(1);
        when(baseMapper.selectVoPage(any(), any())).thenReturn(page);

        PageQuery query = new PageQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        PageResult<SpecialTeacherVo> result = service.queryApprovedPageList(new SpecialTeacherBo(), query);

        SpecialTeacherVo row = result.getRows().iterator().next();
        assertNull(row.getPhone());
    }

    @Test
    void adminDetailMasksBoundLoginPhone() {
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1L);
        vo.setUserId(10L);
        vo.setName("李老师");
        when(baseMapper.selectVoById(1L)).thenReturn(vo);
        when(userService.selectUserByIds(any(), any())).thenReturn(List.of(user(10L, PHONE)));

        SpecialTeacherVo result;
        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(null);
            result = service.queryById(1L);
        }

        assertEquals(SpecialParentSupport.maskPhone(PHONE), result.getPhone());
        assertNotEquals(PHONE, result.getPhone());
    }

    @Test
    void teacherOnlyCannotReadAnotherTeacherRow() {
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(1L);
        vo.setUserId(99L);
        when(baseMapper.selectVoById(1L)).thenReturn(vo);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin(10L));
            helper.when(LoginHelper::getUserId).thenReturn(10L);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.queryById(1L));
            assertEquals("没有权限访问", ex.getMessage());
        }
    }

    @Test
    void teacherOnlyListForcesOwnUserId() {
        Page<SpecialTeacherVo> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(baseMapper.selectVoPage(any(), any())).thenReturn(page);
        SpecialTeacherBo bo = new SpecialTeacherBo();
        PageQuery query = new PageQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin(10L));
            helper.when(LoginHelper::getUserId).thenReturn(10L);

            service.queryPageList(bo, query);
        }

        assertEquals(10L, bo.getUserId());
    }

    @Test
    void teacherOnlyCannotChangeAuditStatus() {
        SpecialTeacher current = new SpecialTeacher();
        current.setId(1L);
        current.setUserId(10L);
        current.setStatus(0);
        when(baseMapper.selectById(1L)).thenReturn(current);
        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setId(1L);
        bo.setName("周老师");
        bo.setStatus(1);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin(10L));
            helper.when(LoginHelper::getUserId).thenReturn(10L);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.updateByBo(bo));
            assertEquals("没有权限访问", ex.getMessage());
        }
    }

    @Test
    void rebindingPhoneRevokesTeacherRoleOnPreviousUser() {
        String newPhone = "13900139000";
        when(userService.selectUserByPhoneNumber(newPhone)).thenReturn(user(20L, newPhone));
        when(roleService.selectRolesByUserId(20L)).thenReturn(List.of(role("special_parent", 7L)));
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of(
            role("special_parent", 7L), role("special_teacher", 8L)));
        when(roleService.selectRoleAll()).thenReturn(List.of(
            role("special_parent", 7L), role("special_teacher", 8L)));
        when(baseMapper.selectOne(any())).thenReturn(null);

        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setName("周老师");
        bo.setPhone(newPhone);

        Long userId = service.bindAccountByPhone(bo, 1L, 10L);

        assertEquals(20L, userId);
        verify(userService, never()).insertUser(any(SysUserBo.class));
        ArgumentCaptor<SysUserRole> inserted = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper).insert(inserted.capture());
        assertEquals(20L, inserted.getValue().getUserId());
        assertEquals(8L, inserted.getValue().getRoleId());
        verify(userRoleMapper).delete(any());
    }

    @Test
    void rebindingPhoneDisablesPreviousUserWhenTeacherWasOnlyRole() {
        String newPhone = "13900139000";
        when(userService.selectUserByPhoneNumber(newPhone)).thenReturn(user(20L, newPhone));
        when(roleService.selectRolesByUserId(20L)).thenReturn(List.of(role("special_parent", 7L)));
        when(roleService.selectRolesByUserId(10L)).thenReturn(List.of(role("special_teacher", 8L)));
        when(roleService.selectRoleAll()).thenReturn(List.of(role("special_teacher", 8L)));
        when(baseMapper.selectOne(any())).thenReturn(null);

        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setName("周老师");
        bo.setPhone(newPhone);

        assertEquals(20L, service.bindAccountByPhone(bo, 1L, 10L));

        verify(userRoleMapper).delete(any());
        verify(userService).updateUserStatus(10L, SystemConstants.DISABLE);
    }

    private static SysUserVo user(Long userId, String phone) {
        SysUserVo user = new SysUserVo();
        user.setUserId(userId);
        user.setPhoneNumber(phone);
        user.setStatus(SystemConstants.NORMAL);
        return user;
    }

    private static LoginUser teacherLogin(Long userId) {
        LoginUser user = new LoginUser();
        user.setUserId(userId);
        user.setRolePermission(Set.of("special_teacher"));
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
