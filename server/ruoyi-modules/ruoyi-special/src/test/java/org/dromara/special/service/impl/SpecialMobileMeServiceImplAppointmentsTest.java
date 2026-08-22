package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialMobileMeServiceImplAppointmentsTest {

    private static final Long USER_ID = 1001L;
    private static final Long TEACHER_ID = 9001L;
    private static final Long APPOINTMENT_ID = 7001L;

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
    void listMyAppointmentsAsParentFiltersByUserId() {
        when(appointmentService.queryPageList(any(), any())).thenReturn(PageResult.build(List.of(), 0));

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_parent");

            service.listMyAppointments(new PageQuery(), null);

            ArgumentCaptor<SpecialAppointmentBo> captor = ArgumentCaptor.forClass(SpecialAppointmentBo.class);
            verify(appointmentService).queryPageList(captor.capture(), any());
            assertEquals(USER_ID, captor.getValue().getUserId());
            assertNull(captor.getValue().getTeacherId());
        }
    }

    @Test
    void listMyAppointmentsAsTeacherFiltersByOwnTeacherId() {
        SpecialTeacher teacher = ownTeacher();
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(teacher);
        when(appointmentService.queryPageList(any(), any())).thenReturn(PageResult.build(List.of(), 0));

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            service.listMyAppointments(new PageQuery(), 0);

            ArgumentCaptor<SpecialAppointmentBo> captor = ArgumentCaptor.forClass(SpecialAppointmentBo.class);
            verify(appointmentService).queryPageList(captor.capture(), any());
            assertEquals(TEACHER_ID, captor.getValue().getTeacherId());
            assertNull(captor.getValue().getUserId());
            assertEquals(0, captor.getValue().getAppointStatus());
        }
    }

    @Test
    void listMyAppointmentsAsTeacherWithoutRowReturnsEmpty() {
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            PageResult<SpecialAppointmentVo> result = service.listMyAppointments(new PageQuery(), null);

            assertEquals(0, result.getTotal());
            assertTrue(result.getRows().isEmpty());
            verify(appointmentService, never()).queryPageList(any(), any());
        }
    }

    @Test
    void getMyAppointmentAsTeacherAuthorizesByTeacherId() {
        SpecialTeacher teacher = ownTeacher();
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(teacher);
        SpecialAppointmentVo vo = new SpecialAppointmentVo();
        vo.setId(APPOINTMENT_ID);
        vo.setUserId(2002L);
        vo.setTeacherId(TEACHER_ID);
        when(appointmentService.queryById(APPOINTMENT_ID)).thenReturn(vo);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            SpecialAppointmentVo result = service.getMyAppointment(APPOINTMENT_ID);
            assertEquals(APPOINTMENT_ID, result.getId());
        }
    }

    @Test
    void getMyAppointmentAsTeacherRejectsOtherTeacherId() {
        SpecialTeacher teacher = ownTeacher();
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(teacher);
        SpecialAppointmentVo vo = new SpecialAppointmentVo();
        vo.setId(APPOINTMENT_ID);
        vo.setUserId(USER_ID);
        vo.setTeacherId(8888L);
        when(appointmentService.queryById(APPOINTMENT_ID)).thenReturn(vo);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            ServiceException ex = assertThrows(ServiceException.class, () -> service.getMyAppointment(APPOINTMENT_ID));
            assertEquals("无权查看该预约", ex.getMessage());
        }
    }

    @Test
    void getMyAppointmentAsTeacherWithoutRowIsForbidden() {
        when(teacherMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        SpecialAppointmentVo vo = new SpecialAppointmentVo();
        vo.setId(APPOINTMENT_ID);
        vo.setUserId(USER_ID);
        when(appointmentService.queryById(APPOINTMENT_ID)).thenReturn(vo);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class);
             MockedStatic<SpecialCurrentRoleStore> store = mockStatic(SpecialCurrentRoleStore.class)) {
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);
            store.when(SpecialCurrentRoleStore::read).thenReturn("special_teacher");

            ServiceException ex = assertThrows(ServiceException.class, () -> service.getMyAppointment(APPOINTMENT_ID));
            assertEquals("无权查看该预约", ex.getMessage());
        }
    }

    private static SpecialTeacher ownTeacher() {
        SpecialTeacher teacher = new SpecialTeacher();
        teacher.setId(TEACHER_ID);
        teacher.setUserId(USER_ID);
        return teacher;
    }
}
