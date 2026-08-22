package org.dromara.special.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 特教机构对象 special_organization
 *
 * @author special
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("special_organization")
public class SpecialOrganization extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 机构名称
     */
    private String name;

    /**
     * 机构类型
     */
    private String orgType;

    /**
     * 许可证编号
     */
    private String licenseNo;

    /**
     * 许可证地址
     */
    private String licenseUrl;

    /**
     * 地址
     */
    private String address;

    /**
     * 地区
     */
    private String region;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图 URL
     */
    private String coverUrl;

    /**
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    private Integer auditStatus;

    private String auditRemark;

    private Long auditBy;

    private java.time.LocalDateTime auditTime;

    /**
     * 状态（0停用 1正常）
     */
    private Integer status;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;

}
