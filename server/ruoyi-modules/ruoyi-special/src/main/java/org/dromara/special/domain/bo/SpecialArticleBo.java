package org.dromara.special.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.special.domain.SpecialArticle;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特教资讯业务对象 special_article
 *
 * @author special
 */
@Data
@AutoMapper(target = SpecialArticle.class, reverseConvertGenerate = false)
public class SpecialArticleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空", groups = {AddGroup.class, EditGroup.class})
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 正文（HTML）
     */
    private String content;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 分类 policy/news/guide
     */
    private String category;

    /**
     * 状态（0草稿 1已发布 2已下架）
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 浏览次数
     */
    private Long viewCount;

}
