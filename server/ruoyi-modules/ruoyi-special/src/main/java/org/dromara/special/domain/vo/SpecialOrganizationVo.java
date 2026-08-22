package org.dromara.special.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.special.domain.SpecialOrganization;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特教机构视图对象 special_organization
 *
 * @author special
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecialOrganization.class)
public class SpecialOrganizationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 机构名称
     */
    @ExcelProperty(value = "机构名称")
    private String name;

    /**
     * 机构类型
     */
    @ExcelProperty(value = "机构类型")
    private String orgType;

    /**
     * 许可证编号
     */
    @ExcelProperty(value = "许可证编号")
    private String licenseNo;

    /**
     * 许可证地址
     */
    @ExcelProperty(value = "许可证地址")
    private String licenseUrl;

    /**
     * 地址
     */
    @ExcelProperty(value = "地址")
    private String address;

    /**
     * 地区
     */
    @ExcelProperty(value = "地区")
    private String region;

    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    private String contactName;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String contactPhone;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 封面图 URL
     */
    @ExcelProperty(value = "封面图")
    private String coverUrl;

    /**
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    @ExcelProperty(value = "审核状态")
    private Integer auditStatus;

    private String auditRemark;

    private Long auditBy;

    private LocalDateTime auditTime;

    /**
     * 状态（0停用 1正常）
     */
    @ExcelProperty(value = "状态")
    private Integer status;

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
