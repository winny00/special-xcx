package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.util.SpecialCurrentRoleStore;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialMobileMeServiceImplTeacherProfileTest {

    private static final Long USER_ID = 1001L;
    private static final Long TEACHER_ID = 9001L;

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
            phone -> null, null);
    }

    @Test
    void getMyTeacherProfileReturnsOwnRowByUserId() {
        SpecialTeacherVo vo = new SpecialTeacherVo();
        vo.setId(TEACHER_ID);
        vo.setUserId(USER_ID);
        vo.setName("李老师");
        vo.setStatus(1);
        when(teacherMapper.selectVoOne(any(Wrapper.class), eq(false))).thenReturn(vo);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            SpecialTeacherVo result = service.getMyTeacherProfile();
            assertEquals("李老师", result.getName());
            assertEquals(TEACHER_ID, result.getId());
        }
    }

    @Test
    void getMyTeacherProfileRejectsNonTeacherIdentity() {
        try (MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_parent");

            ServiceException ex = assertThrows(ServiceException.class, () -> service.getMyTeacherProfile());
            assertEquals("当前账号没有该身份", ex.getMessage());
        }
    }

    @Test
    void updateMyTeacherProfileIgnoresClientStatusAndKeepsOwnId() {
        SpecialTeacher existing = new SpecialTeacher();
        existing.setId(TEACHER_ID);
        existing.setUserId(USER_ID);
        existing.setStatus(1);
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        when(teacherMapper.updateById(any(SpecialTeacher.class))).thenReturn(1);

        SpecialTeacherBo bo = new SpecialTeacherBo();
        bo.setId(1L);
        bo.setName("新名字");
        bo.setIntro("简介");
        bo.setStatus(0);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            service.updateMyTeacherProfile(bo);

            ArgumentCaptor<SpecialTeacher> captor = ArgumentCaptor.forClass(SpecialTeacher.class);
            verify(teacherMapper).updateById(captor.capture());
            SpecialTeacher update = captor.getValue();
            assertEquals(TEACHER_ID, update.getId());
            assertEquals("新名字", update.getName());
            assertEquals("简介", update.getIntro());
            assertNull(update.getStatus());
        }
    }
}
