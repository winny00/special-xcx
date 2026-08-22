package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.service.ISpecialTeacherService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 特教老师档案
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/teacher")
public class SpecialTeacherController extends BaseController {

    private final ISpecialTeacherService specialTeacherService;

    @SaCheckPermission("special:teacher:list")
    @GetMapping("/list")
    public R<PageResult<SpecialTeacherVo>> list(@Validated(QueryGroup.class) SpecialTeacherBo bo, PageQuery pageQuery) {
        return R.ok(specialTeacherService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("special:teacher:query")
    @GetMapping("/{id}")
    public R<SpecialTeacherVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(specialTeacherService.queryById(id));
    }

    @SaCheckPermission("special:teacher:add")
    @Log(title = "特教老师", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@RequestBody SpecialTeacherBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialTeacherService.insertByBo(bo));
    }

    @SaCheckPermission("special:teacher:edit")
    @Log(title = "特教老师", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialTeacherBo bo) {
        return toAjax(specialTeacherService.updateByBo(bo));
    }

    @SaCheckPermission("special:teacher:edit")
    @Log(title = "特教老师", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody SpecialAuditBo bo) {
        return toAjax(specialTeacherService.audit(bo));
    }

    @SaCheckPermission("special:teacher:remove")
    @Log(title = "特教老师", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(specialTeacherService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
