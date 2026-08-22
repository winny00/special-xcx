package org.dromara.special.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 特教工作台概览统计
 *
 * @author special
 */
@Data
public class SpecialDashboardStatsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long resourceTotal;

    private Map<String, Long> resourceByType;

    private Long resourceDraftCount;

    private Long orgAuditPending;

    private Long teacherAuditPending;

    private Long appointmentPending;

    private Long appointmentToday;
}
