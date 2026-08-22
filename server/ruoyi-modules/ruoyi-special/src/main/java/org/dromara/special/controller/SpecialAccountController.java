package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.SpecialAccountBo;
import org.dromara.special.domain.bo.SpecialAccountPasswordBody;
import org.dromara.special.domain.bo.SpecialAccountRolesBody;
import org.dromara.special.domain.vo.SpecialAccountRoleResult;
import org.dromara.special.domain.vo.SpecialAccountVo;
import org.dromara.special.service.ISpecialAccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 后台用户角色（家长 / 老师）
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/account")
public class SpecialAccountController extends BaseController {

    private final ISpecialAccountService specialAccountService;

    @SaCheckPermission("special:account:list")
    @GetMapping("/list")
    public R<PageResult<SpecialAccountVo>> list(SpecialAccountBo bo, PageQuery pageQuery) {
        return R.ok(specialAccountService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("special:account:edit")
    @Log(title = "用户角色", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/roles")
    public R<Map<String, Object>> updateRoles(
        @NotNull(message = "用户ID不能为空") @PathVariable("userId") Long userId,
        @RequestBody SpecialAccountRolesBody body
    ) {
        SpecialAccountRoleResult result = specialAccountService.updateRoles(userId, body);
        if (!result.isSuccess()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("needTeacherProfile", result.isNeedTeacherProfile());
            data.put("phone", result.getPhone());
            return R.fail(result.getMessage(), data);
        }
        return R.ok();
    }

    @SaCheckPermission("special:account:edit")
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/password")
    public R<Void> resetPassword(
        @NotNull(message = "用户ID不能为空") @PathVariable("userId") Long userId,
        @RequestBody SpecialAccountPasswordBody body
    ) {
        specialAccountService.resetPassword(userId, body == null ? null : body.getPassword());
        return R.ok();
    }
}
