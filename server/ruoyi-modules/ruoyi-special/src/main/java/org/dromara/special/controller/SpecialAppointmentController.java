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
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.service.ISpecialAppointmentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 特教预约Controller
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/appointment")
public class SpecialAppointmentController extends BaseController {

    private final ISpecialAppointmentService specialAppointmentService;

    /**
     * 查询特教预约列表
     */
    @SaCheckPermission("special:appointment:list")
    @GetMapping("/list")
    public R<PageResult<SpecialAppointmentVo>> list(@Validated(QueryGroup.class) SpecialAppointmentBo bo, PageQuery pageQuery) {
        return R.ok(specialAppointmentService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出特教预约列表
     */
    @SaCheckPermission("special:appointment:export")
    @Log(title = "特教预约", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated SpecialAppointmentBo bo, HttpServletResponse response) {
        List<SpecialAppointmentVo> list = specialAppointmentService.queryList(bo);
        ExcelBuilder.of(list, SpecialAppointmentVo.class).sheetName("特教预约").toResponse(response);
    }

    /**
     * 获取特教预约详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("special:appointment:query")
    @GetMapping("/{id}")
    public R<SpecialAppointmentVo> getInfo(@NotNull(message = "主键不能为空")
                                           @PathVariable("id") Long id) {
        return R.ok(specialAppointmentService.queryById(id));
    }

    /**
     * 新增特教预约
     */
    @SaCheckPermission("special:appointment:add")
    @Log(title = "特教预约", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@RequestBody SpecialAppointmentBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialAppointmentService.insertByBo(bo));
    }

    /**
     * 修改特教预约
     */
    @SaCheckPermission("special:appointment:edit")
    @Log(title = "特教预约", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialAppointmentBo bo) {
        return toAjax(specialAppointmentService.updateByBo(bo));
    }

    /**
     * 删除特教预约
     *
     * @param ids 主键串
     */
    @SaCheckPermission("special:appointment:remove")
    @Log(title = "特教预约", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(specialAppointmentService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}
