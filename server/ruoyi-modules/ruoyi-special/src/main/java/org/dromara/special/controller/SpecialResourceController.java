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
import org.dromara.special.domain.bo.SpecialResourceBo;
import org.dromara.special.domain.vo.SpecialResourceVo;
import org.dromara.special.service.ISpecialResourceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 特教资源Controller
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/resource")
public class SpecialResourceController extends BaseController {

    private final ISpecialResourceService specialResourceService;

    /**
     * 查询特教资源列表
     */
    @SaCheckPermission("special:resource:list")
    @GetMapping("/list")
    public R<PageResult<SpecialResourceVo>> list(@Validated(QueryGroup.class) SpecialResourceBo bo, PageQuery pageQuery) {
        return R.ok(specialResourceService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出特教资源列表
     */
    @SaCheckPermission("special:resource:export")
    @Log(title = "特教资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated SpecialResourceBo bo, HttpServletResponse response) {
        List<SpecialResourceVo> list = specialResourceService.queryList(bo);
        ExcelBuilder.of(list, SpecialResourceVo.class).sheetName("特教资源").toResponse(response);
    }

    /**
     * 获取特教资源详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("special:resource:query")
    @GetMapping("/{id}")
    public R<SpecialResourceVo> getInfo(@NotNull(message = "主键不能为空")
                                        @PathVariable("id") Long id) {
        return R.ok(specialResourceService.queryById(id));
    }

    /**
     * 新增特教资源
     */
    @SaCheckPermission("special:resource:add")
    @Log(title = "特教资源", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@RequestBody SpecialResourceBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialResourceService.insertByBo(bo));
    }

    /**
     * 修改特教资源
     */
    @SaCheckPermission("special:resource:edit")
    @Log(title = "特教资源", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialResourceBo bo) {
        return toAjax(specialResourceService.updateByBo(bo));
    }

    @SaCheckPermission("special:resource:edit")
    @Log(title = "特教资源", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody SpecialAuditBo bo) {
        return toAjax(specialResourceService.audit(bo));
    }

    /**
     * 删除特教资源
     *
     * @param ids 主键串
     */
    @SaCheckPermission("special:resource:remove")
    @Log(title = "特教资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(specialResourceService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}
