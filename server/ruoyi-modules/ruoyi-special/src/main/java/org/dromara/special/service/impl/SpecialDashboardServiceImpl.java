package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.special.domain.SpecialAppointment;
import org.dromara.special.domain.SpecialOrganization;
import org.dromara.special.domain.SpecialResource;
import org.dromara.special.domain.vo.SpecialDashboardStatsVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialOrganizationMapper;
import org.dromara.special.mapper.SpecialResourceMapper;
import org.dromara.special.service.ISpecialDashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 特教工作台概览
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialDashboardServiceImpl implements ISpecialDashboardService {

    private static final List<String> DASHBOARD_RESOURCE_TYPES = List.of(
        "course", "tool", "teacher", "assessment"
    );

    private final SpecialResourceMapper resourceMapper;
    private final SpecialOrganizationMapper organizationMapper;
    private final SpecialAppointmentMapper appointmentMapper;

    @Override
    public SpecialDashboardStatsVo queryStats() {
        SpecialDashboardStatsVo vo = new SpecialDashboardStatsVo();

        Map<String, Long> byType = new LinkedHashMap<>();
        long typeTotal = 0L;
        for (String type : DASHBOARD_RESOURCE_TYPES) {
            Long count = resourceMapper.selectCount(
                Wrappers.<SpecialResource>lambdaQuery().eq(SpecialResource::getResourceType, type)
            );
            byType.put(type, count);
            typeTotal += count;
        }
        vo.setResourceByType(byType);
        vo.setResourceTotal(typeTotal);

        vo.setResourceDraftCount(resourceMapper.selectCount(
            Wrappers.<SpecialResource>lambdaQuery()
                .eq(SpecialResource::getStatus, 0)
                .in(SpecialResource::getResourceType, DASHBOARD_RESOURCE_TYPES)
        ));

        vo.setOrgAuditPending(organizationMapper.selectCount(
            Wrappers.<SpecialOrganization>lambdaQuery().eq(SpecialOrganization::getAuditStatus, 0)
        ));

        vo.setAppointmentPending(appointmentMapper.selectCount(
            Wrappers.<SpecialAppointment>lambdaQuery().eq(SpecialAppointment::getAppointStatus, 0)
        ));

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        vo.setAppointmentToday(appointmentMapper.selectCount(
            Wrappers.<SpecialAppointment>lambdaQuery()
                .ge(SpecialAppointment::getCreateTime, start)
                .le(SpecialAppointment::getCreateTime, end)
        ));

        return vo;
    }
}
