package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.vo.SpecialDashboardStatsVo;
import org.dromara.special.service.ISpecialDashboardService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 特教工作台概览
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/dashboard")
public class SpecialDashboardController extends BaseController {

    private final ISpecialDashboardService dashboardService;

    @SaCheckPermission("special:dashboard:view")
    @GetMapping("/stats")
    public R<SpecialDashboardStatsVo> stats() {
        return R.ok(dashboardService.queryStats());
    }
}
