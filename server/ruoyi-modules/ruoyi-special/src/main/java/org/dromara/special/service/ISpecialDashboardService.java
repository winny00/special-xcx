package org.dromara.special.service;

import org.dromara.special.domain.vo.SpecialDashboardStatsVo;

/**
 * 特教工作台概览
 *
 * @author special
 */
public interface ISpecialDashboardService {

    SpecialDashboardStatsVo queryStats();
}
