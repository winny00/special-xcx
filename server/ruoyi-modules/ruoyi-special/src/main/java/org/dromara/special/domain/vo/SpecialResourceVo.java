package org.dromara.special.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.special.domain.SpecialResource;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 特教资源视图对象 special_resource
 *
 * @author special
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecialResource.class)
public class SpecialResourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 资源类型
     */
    @ExcelProperty(value = "资源类型")
    private String resourceType;

    /**
     * 分类
     */
    @ExcelProperty(value = "分类")
    private String category;

    /**
     * 摘要
     */
    @ExcelProperty(value = "摘要")
    private String summary;

    /**
     * 内容
     */
    @ExcelProperty(value = "内容")
    private String content;

    /**
     * 封面地址
     */
    @ExcelProperty(value = "封面地址")
    private String coverUrl;

    /**
     * 机构ID
     */
    @ExcelProperty(value = "机构ID")
    private Long orgId;

    /**
     * 提供方名称
     */
    @ExcelProperty(value = "提供方名称")
    private String providerName;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String contactPhone;

    /**
     * 地区
     */
    @ExcelProperty(value = "地区")
    private String region;

    /**
     * 价格
     */
    @ExcelProperty(value = "价格")
    private BigDecimal price;

    /**
     * 状态（0草稿 1已发布）
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 浏览次数
     */
    @ExcelProperty(value = "浏览次数")
    private Long viewCount;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
