package org.dromara.special.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialResourceMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialAppointmentServiceImplCreateTest {

    private static final Long USER_ID = 1001L;
    private static final Long TEACHER_ID = 9001L;

    @Mock
    private SpecialAppointmentMapper appointmentMapper;
    @Mock
    private SpecialResourceMapper resourceMapper;
    @Mock
    private SpecialTeacherMapper teacherMapper;
    @Mock
    private ISysUserService userService;

    private SpecialAppointmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SpecialAppointmentServiceImpl(appointmentMapper, resourceMapper, teacherMapper, userService);
    }

    @Test
    void createMobileAppointmentRejectsWhenNotLoggedIn() {
        SpecialAppointmentBo bo = teacherOnlyBo();
        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::isLogin).thenReturn(false);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.createMobileAppointment(bo));
            assertEquals("请先绑定手机号", ex.getMessage());
        }
    }

    @Test
    void createMobileAppointmentRejectsWhenPhoneUnbound() {
        SpecialAppointmentBo bo = teacherOnlyBo();
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        when(userService.selectUserById(USER_ID)).thenReturn(user);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::isLogin).thenReturn(true);
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.createMobileAppointment(bo));
            assertEquals("请先绑定手机号", ex.getMessage());
        }
    }

    @Test
    void createMobileAppointmentRejectsUnknownTeacher() {
        SpecialAppointmentBo bo = teacherOnlyBo();
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        user.setPhoneNumber("13800138000");
        when(userService.selectUserById(USER_ID)).thenReturn(user);
        when(teacherMapper.selectById(TEACHER_ID)).thenReturn(null);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::isLogin).thenReturn(true);
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.createMobileAppointment(bo));
            assertEquals("老师不存在或未通过审核", ex.getMessage());
        }
    }

    @Test
    void createMobileAppointmentRejectsUnapprovedTeacher() {
        SpecialAppointmentBo bo = teacherOnlyBo();
        SysUserVo user = new SysUserVo();
        user.setUserId(USER_ID);
        user.setPhoneNumber("13800138000");
        when(userService.selectUserById(USER_ID)).thenReturn(user);
        SpecialTeacher teacher = new SpecialTeacher();
        teacher.setId(TEACHER_ID);
        teacher.setStatus(0);
        when(teacherMapper.selectById(TEACHER_ID)).thenReturn(teacher);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::isLogin).thenReturn(true);
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.createMobileAppointment(bo));
            assertEquals("老师不存在或未通过审核", ex.getMessage());
        }
    }

    private static SpecialAppointmentBo teacherOnlyBo() {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setTeacherId(TEACHER_ID);
        bo.setContactName("家长");
        bo.setContactPhone("13800138000");
        return bo;
    }
}
