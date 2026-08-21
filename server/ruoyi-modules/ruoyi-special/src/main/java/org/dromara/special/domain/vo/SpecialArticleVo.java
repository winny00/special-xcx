package org.dromara.special.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.special.domain.SpecialArticle;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特教资讯视图对象 special_article
 *
 * @author special
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecialArticle.class)
public class SpecialArticleVo implements Serializable {

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
     * 摘要
     */
    @ExcelProperty(value = "摘要")
    private String summary;

    /**
     * 正文（HTML）
     */
    @ExcelProperty(value = "正文")
    private String content;

    /**
     * 封面地址
     */
    @ExcelProperty(value = "封面地址")
    private String coverUrl;

    /**
     * 分类
     */
    @ExcelProperty(value = "分类")
    private String category;

    /**
     * 状态（0草稿 1已发布 2已下架）
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 发布时间
     */
    @ExcelProperty(value = "发布时间")
    private LocalDateTime publishTime;

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
