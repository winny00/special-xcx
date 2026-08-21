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
import org.dromara.special.domain.bo.SpecialArticleBo;
import org.dromara.special.domain.vo.SpecialArticleVo;
import org.dromara.special.service.ISpecialArticleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 特教资讯Controller
 *
 * @author special
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/special/article")
public class SpecialArticleController extends BaseController {

    private final ISpecialArticleService specialArticleService;

    /**
     * 查询资讯列表
     */
    @SaCheckPermission("special:article:list")
    @GetMapping("/list")
    public R<PageResult<SpecialArticleVo>> list(@Validated(QueryGroup.class) SpecialArticleBo bo, PageQuery pageQuery) {
        return R.ok(specialArticleService.queryPageList(bo, pageQuery));
    }

    /**
     * 导出资讯列表
     */
    @SaCheckPermission("special:article:export")
    @Log(title = "特教资讯", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated SpecialArticleBo bo, HttpServletResponse response) {
        List<SpecialArticleVo> list = specialArticleService.queryList(bo);
        ExcelBuilder.of(list, SpecialArticleVo.class).sheetName("特教资讯").toResponse(response);
    }

    /**
     * 获取资讯详细信息
     */
    @SaCheckPermission("special:article:query")
    @GetMapping("/{id}")
    public R<SpecialArticleVo> getInfo(@NotNull(message = "主键不能为空")
                                       @PathVariable("id") Long id) {
        return R.ok(specialArticleService.queryById(id));
    }

    /**
     * 新增资讯
     */
    @SaCheckPermission("special:article:add")
    @Log(title = "特教资讯", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@RequestBody SpecialArticleBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        return toAjax(specialArticleService.insertByBo(bo));
    }

    /**
     * 修改资讯
     */
    @SaCheckPermission("special:article:edit")
    @Log(title = "特教资讯", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialArticleBo bo) {
        return toAjax(specialArticleService.updateByBo(bo));
    }

    /**
     * 删除资讯
     */
    @SaCheckPermission("special:article:remove")
    @Log(title = "特教资讯", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(specialArticleService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}
