package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.bo.SpecialOrganizationBo;
import org.dromara.special.domain.bo.SpecialResourceBo;
import org.dromara.special.domain.vo.SpecialOrganizationVo;
import org.dromara.special.domain.vo.SpecialResourceVo;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.service.ISpecialOrganizationService;
import org.dromara.special.service.ISpecialResourceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 特教移动端Controller
 *
 * @author special
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/mobile")
public class SpecialMobileController extends BaseController {

    private final ISpecialResourceService specialResourceService;
    private final ISpecialOrganizationService specialOrganizationService;
    private final ISpecialAppointmentService specialAppointmentService;

    /**
     * 查询已发布资源列表
     */
    @GetMapping("/resource/list")
    public R<PageResult<SpecialResourceVo>> resourceList(@Validated(QueryGroup.class) SpecialResourceBo bo, PageQuery pageQuery) {
        return R.ok(specialResourceService.queryPublishedPageList(bo, pageQuery));
    }

    /**
     * 获取已发布资源详情
     *
     * @param id 主键
     */
    @GetMapping("/resource/{id}")
    public R<SpecialResourceVo> resourceDetail(@NotNull(message = "主键不能为空")
                                               @PathVariable("id") Long id) {
        return R.ok(specialResourceService.queryPublishedById(id));
    }

    /**
     * 查询已审核通过机构列表
     */
    @GetMapping("/organization/list")
    public R<PageResult<SpecialOrganizationVo>> organizationList(@Validated(QueryGroup.class) SpecialOrganizationBo bo, PageQuery pageQuery) {
        return R.ok(specialOrganizationService.queryApprovedPageList(bo, pageQuery));
    }

    /**
     * 创建预约（登录可选）
     */
    @PostMapping("/appointment")
    public R<Void> createAppointment(@RequestBody SpecialAppointmentBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialAppointmentService.createMobileAppointment(bo));
    }

}
