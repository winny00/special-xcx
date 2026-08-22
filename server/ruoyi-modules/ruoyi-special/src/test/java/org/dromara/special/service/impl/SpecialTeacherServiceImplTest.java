package org.dromara.special.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@ExtendWith(MockitoExtension.class)
class SpecialTeacherServiceImplTest {

    @Mock
    private SpecialTeacherMapper baseMapper;

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
}
