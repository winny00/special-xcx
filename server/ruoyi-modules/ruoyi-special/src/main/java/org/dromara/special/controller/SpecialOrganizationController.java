package org.dromara.special.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelBuilder;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialOrganizationBo;
import org.dromara.special.domain.vo.SpecialOrganizationVo;
import org.dromara.special.service.ISpecialOrganizationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 特教机构Controller
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/organization")
public class SpecialOrganizationController extends BaseController {

    private final ISpecialOrganizationService specialOrganizationService;

    /**
     * 查询特教机构列表
     */
    @SaCheckPermission("special:organization:list")
    @GetMapping("/list")
    public R<PageResult<SpecialOrganizationVo>> list(@Validated(QueryGroup.class) SpecialOrganizationBo bo, PageQuery pageQuery) {
        return R.ok(specialOrganizationService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出特教机构列表
     */
    @SaCheckPermission("special:organization:export")
    @Log(title = "特教机构", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated SpecialOrganizationBo bo, HttpServletResponse response) {
        List<SpecialOrganizationVo> list = specialOrganizationService.queryList(bo);
        ExcelBuilder.of(list, SpecialOrganizationVo.class).sheetName("特教机构").toResponse(response);
    }

    /**
     * 获取特教机构详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("special:organization:query")
    @GetMapping("/{id}")
    public R<SpecialOrganizationVo> getInfo(@NotNull(message = "主键不能为空")
                                              @PathVariable("id") Long id) {
        return R.ok(specialOrganizationService.queryById(id));
    }

    /**
     * 新增特教机构
     */
    @SaCheckPermission("special:organization:add")
    @Log(title = "特教机构", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@RequestBody SpecialOrganizationBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialOrganizationService.insertByBo(bo));
    }

    /**
     * 修改特教机构
     */
    @SaCheckPermission("special:organization:edit")
    @Log(title = "特教机构", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialOrganizationBo bo) {
        return toAjax(specialOrganizationService.updateByBo(bo));
    }

    @SaCheckPermission("special:organization:edit")
    @Log(title = "特教机构", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody SpecialAuditBo bo) {
        return toAjax(specialOrganizationService.audit(bo));
    }

    /**
     * 删除特教机构
     *
     * @param ids 主键串
     */
    @SaCheckPermission("special:organization:remove")
    @Log(title = "特教机构", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(specialOrganizationService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}
