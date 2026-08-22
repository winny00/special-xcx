package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialAppointment;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialResourceMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.system.api.model.LoginUser;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialAppointmentServiceImplScopeTest {

    private static final Long USER_ID = 10L;
    private static final Long OWN_TEACHER_ID = 9001L;

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
    void teacherOnlyListForcesOwnTeacherId() {
        SpecialTeacher own = new SpecialTeacher();
        own.setId(OWN_TEACHER_ID);
        own.setUserId(USER_ID);
        when(teacherMapper.selectOne(any())).thenReturn(own);
        Page<SpecialAppointmentVo> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(appointmentMapper.selectVoPage(any(), any())).thenReturn(page);
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        PageQuery query = new PageQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin());
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            service.queryPageList(bo, query);
        }

        assertEquals(OWN_TEACHER_ID, bo.getTeacherId());
    }

    @Test
    void teacherOnlyCannotReadAnotherTeachersAppointment() {
        SpecialAppointmentVo vo = new SpecialAppointmentVo();
        vo.setId(1L);
        vo.setTeacherId(8888L);
        when(appointmentMapper.selectVoById(1L)).thenReturn(vo);
        SpecialTeacher own = new SpecialTeacher();
        own.setId(OWN_TEACHER_ID);
        own.setUserId(USER_ID);
        when(teacherMapper.selectOne(any())).thenReturn(own);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin());
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.queryById(1L));
            assertEquals("没有权限访问", ex.getMessage());
        }
    }

    @Test
    void teacherOnlyCannotUpdateAnotherTeachersAppointment() {
        SpecialAppointment current = new SpecialAppointment();
        current.setId(1L);
        current.setTeacherId(8888L);
        when(appointmentMapper.selectById(1L)).thenReturn(current);
        SpecialTeacher own = new SpecialTeacher();
        own.setId(OWN_TEACHER_ID);
        own.setUserId(USER_ID);
        when(teacherMapper.selectOne(any())).thenReturn(own);
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setId(1L);
        bo.setAppointStatus(1);

        try (MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            helper.when(LoginHelper::getLoginUser).thenReturn(teacherLogin());
            helper.when(LoginHelper::getUserId).thenReturn(USER_ID);

            ServiceException ex = assertThrows(ServiceException.class, () -> service.updateByBo(bo));
            assertEquals("没有权限访问", ex.getMessage());
        }
    }

    private static LoginUser teacherLogin() {
        LoginUser user = new LoginUser();
        user.setUserId(USER_ID);
        user.setRolePermission(Set.of("special_teacher"));
        return user;
    }
}
