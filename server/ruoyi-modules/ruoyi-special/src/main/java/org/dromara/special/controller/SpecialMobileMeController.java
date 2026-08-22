package org.dromara.special.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.BindPhoneBody;
import org.dromara.special.domain.bo.SpecialMobileProfileBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialBindPhoneVo;
import org.dromara.special.domain.vo.SpecialMobileProfileVo;
import org.dromara.special.service.ISpecialMobileMeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端家长中心（需登录）
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/mobile/me")
public class SpecialMobileMeController extends BaseController {

    private final ISpecialMobileMeService mobileMeService;

    /**
     * 获取当前用户资料
     */
    @GetMapping("/profile")
    public R<SpecialMobileProfileVo> profile() {
        return R.ok(mobileMeService.getProfile());
    }

    /**
     * 更新当前用户资料
     */
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SpecialMobileProfileBo bo) {
        return toAjax(mobileMeService.updateProfile(bo));
    }

    /**
     * 当前用户预约列表
     */
    @GetMapping("/appointments")
    public R<PageResult<SpecialAppointmentVo>> appointments(PageQuery pageQuery) {
        return R.ok(mobileMeService.listMyAppointments(pageQuery));
    }

    /**
     * 当前用户预约详情
     */
    @GetMapping("/appointments/{id}")
    public R<SpecialAppointmentVo> appointmentDetail(@NotNull(message = "主键不能为空")
                                                     @PathVariable("id") Long id) {
        return R.ok(mobileMeService.getMyAppointment(id));
    }

    /**
     * 绑定手机号（短信）。需登录。
     */
    @PostMapping("/bind-phone")
    public R<SpecialBindPhoneVo> bindPhone(@RequestBody BindPhoneBody body) {
        return R.ok(mobileMeService.bindPhone(body));
    }

}
