package org.dromara.special.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.special.domain.SpecialAppointment;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特教预约视图对象 special_appointment
 *
 * @author special
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecialAppointment.class)
public class SpecialAppointmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 资源ID
     */
    @ExcelProperty(value = "资源ID")
    private Long resourceId;

    /**
     * 资源标题
     */
    @ExcelProperty(value = "资源标题")
    private String resourceTitle;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

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
     * 儿童年龄
     */
    @ExcelProperty(value = "儿童年龄")
    private Integer childAge;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 预约状态（0待处理 1已确认 2已取消 3已完成）
     */
    @ExcelProperty(value = "预约状态")
    private Integer appointStatus;

    /**
     * 处理人ID
     */
    @ExcelProperty(value = "处理人ID")
    private Long handlerId;

    /**
     * 处理备注
     */
    @ExcelProperty(value = "处理备注")
    private String handlerRemark;

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
