package org.dromara.special.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 特教资源对象 special_resource
 *
 * @author special
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("special_resource")
public class SpecialResource extends BaseEntity {

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
     * 资源类型
     */
    private String resourceType;

    /**
     * 分类
     */
    private String category;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 内容
     */
    private String content;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 机构ID
     */
    private Long orgId;

    /**
     * 提供方名称
     */
    private String providerName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地区
     */
    private String region;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 状态（0草稿 1已发布 2已下架）
     */
    private Integer status;

    private String auditRemark;

    private Long auditBy;

    private java.time.LocalDateTime auditTime;

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
