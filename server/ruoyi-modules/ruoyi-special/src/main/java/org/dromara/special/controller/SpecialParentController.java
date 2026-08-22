package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.SpecialParentBo;
import org.dromara.special.domain.vo.SpecialParentDetailVo;
import org.dromara.special.domain.vo.SpecialParentVo;
import org.dromara.special.service.ISpecialParentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 家长 CRM（只读）
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/parent")
public class SpecialParentController extends BaseController {

    private final ISpecialParentService specialParentService;

    /**
     * 家长列表（手机号脱敏）
     */
    @SaCheckPermission("special:parent:list")
    @GetMapping("/list")
    public R<PageResult<SpecialParentVo>> list(SpecialParentBo bo, PageQuery pageQuery) {
        return R.ok(specialParentService.queryPageList(bo, pageQuery));
    }

    /**
     * 家长详情与最近预约（完整手机号）
     */
    @SaCheckPermission("special:parent:query")
    @GetMapping("/{userId}")
    public R<SpecialParentDetailVo> getInfo(@NotNull(message = "用户ID不能为空")
                                            @PathVariable("userId") Long userId) {
        return R.ok(specialParentService.queryById(userId));
    }
}
