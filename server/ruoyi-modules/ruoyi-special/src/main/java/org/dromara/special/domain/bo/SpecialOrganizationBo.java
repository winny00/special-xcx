package org.dromara.special.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.special.domain.SpecialOrganization;

import java.io.Serial;
import java.io.Serializable;

/**
 * 特教机构业务对象 special_organization
 *
 * @author special
 */
@Data
@AutoMapper(target = SpecialOrganization.class, reverseConvertGenerate = false)
public class SpecialOrganizationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 机构名称
     */
    @NotBlank(message = "机构名称不能为空", groups = {AddGroup.class, EditGroup.class})
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
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    private Integer auditStatus;

    /**
     * 状态（0停用 1正常）
     */
    private Integer status;

}
