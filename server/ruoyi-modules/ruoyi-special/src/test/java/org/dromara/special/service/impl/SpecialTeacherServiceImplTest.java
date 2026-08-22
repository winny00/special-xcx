package org.dromara.special.service.impl;

import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialTeacherMapper;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private static SysRoleVo role(String roleKey, Long roleId) {
        SysRoleVo role = new SysRoleVo();
        role.setRoleKey(roleKey);
        role.setRoleId(roleId);
        role.setStatus(SystemConstants.NORMAL);
        return role;
    }
}
