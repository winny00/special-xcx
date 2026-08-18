package org.dromara.special.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.special.domain.SpecialResource;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 特教资源业务对象 special_resource
 *
 * @author special
 */
@Data
@AutoMapper(target = SpecialResource.class, reverseConvertGenerate = false)
public class SpecialResourceBo implements Serializable {

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
     * 状态（0草稿 1已发布）
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Long viewCount;

}
