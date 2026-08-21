package org.dromara.special.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 特教资讯对象 special_article
 *
 * @author special
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("special_article")
public class SpecialArticle extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 标题
     */
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

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;

}
