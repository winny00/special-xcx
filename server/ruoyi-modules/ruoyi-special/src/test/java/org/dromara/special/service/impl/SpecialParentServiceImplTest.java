package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.bo.SpecialParentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialParentDetailVo;
import org.dromara.special.domain.vo.SpecialParentVo;
import org.dromara.special.mapper.SpecialParentMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialParentServiceImplTest {

    @Mock
    private SpecialParentMapper parentMapper;

    @Mock
    private ISysUserService userService;

    @Mock
    private ISpecialAppointmentService appointmentService;

    @InjectMocks
    private SpecialParentServiceImpl service;

    @Test
    void listMasksPhone() {
        SpecialParentVo row = new SpecialParentVo();
        row.setUserId(1L);
        row.setPhone("13800001234");
        row.setAppointmentCount(2L);
        Page<SpecialParentVo> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(parentMapper.selectParentPage(any(), any())).thenReturn(page);

        SpecialParentBo bo = new SpecialParentBo();
        bo.setKeyword("138");
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        PageResult<SpecialParentVo> result = service.queryPageList(bo, pageQuery);
        SpecialParentVo first = result.getRows().iterator().next();
        assertEquals("138****1234", first.getPhone());
        assertEquals(2L, first.getAppointmentCount());
        assertEquals(1L, result.getTotal());
    }

    @Test
    void detailRejectsMissingUser() {
        when(userService.selectUserById(9L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.queryById(9L));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void detailRejectsNonParent() {
        SysUserVo user = new SysUserVo();
        user.setUserId(2L);
        user.setPhoneNumber("13800001234");
        SysRoleVo teacher = new SysRoleVo();
        teacher.setRoleKey("special_teacher");
        user.setRoles(List.of(teacher));
        when(userService.selectUserById(2L)).thenReturn(user);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.queryById(2L));
        assertEquals("该用户不是家长", ex.getMessage());
    }

    @Test
    void detailKeepsFullPhoneAndRecentAppointments() {
        SysUserVo user = new SysUserVo();
        user.setUserId(1L);
        user.setNickName("张家长");
        user.setPhoneNumber("13800001234");
        user.setAvatarUrl("https://example.com/a.png");
        user.setCreateTime(LocalDateTime.of(2026, 1, 1, 8, 0));
        SysRoleVo parent = new SysRoleVo();
        parent.setRoleKey("special_parent");
        user.setRoles(List.of(parent));
        when(userService.selectUserById(1L)).thenReturn(user);

        SpecialAppointmentVo appointment = new SpecialAppointmentVo();
        appointment.setResourceTitle("感统课");
        when(appointmentService.queryPageList(any(), any()))
            .thenReturn(PageResult.build(List.of(appointment), 3));

        SpecialParentDetailVo detail = service.queryById(1L);
        assertEquals("13800001234", detail.getPhone());
        assertEquals("张家长", detail.getNickName());
        assertEquals("https://example.com/a.png", detail.getAvatar());
        assertEquals(1, detail.getAppointments().size());
        assertEquals(3L, detail.getAppointmentCount());

        ArgumentCaptor<SpecialAppointmentBo> boCaptor = ArgumentCaptor.forClass(SpecialAppointmentBo.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(appointmentService).queryPageList(boCaptor.capture(), pageCaptor.capture());
        assertEquals(1L, boCaptor.getValue().getUserId());
        assertEquals(20, pageCaptor.getValue().getPageSize());
        assertEquals(1, pageCaptor.getValue().getPageNum());
    }
}
