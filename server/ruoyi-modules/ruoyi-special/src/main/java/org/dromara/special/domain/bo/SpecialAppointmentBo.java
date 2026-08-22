package org.dromara.special.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.special.domain.SpecialAppointment;

import java.io.Serial;
import java.io.Serializable;

/**
 * 特教预约业务对象 special_appointment
 *
 * @author special
 */
@Data
@AutoMapper(target = SpecialAppointment.class, reverseConvertGenerate = false)
public class SpecialAppointmentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 资源ID
     */
    @NotNull(message = "资源ID不能为空", groups = {AddGroup.class})
    private Long resourceId;

    /**
     * 资源标题
     */
    private String resourceTitle;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空", groups = {AddGroup.class})
    private String contactName;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空", groups = {AddGroup.class})
    private String contactPhone;

    /**
     * 儿童年龄
     */
    private Integer childAge;

    /**
     * 备注
     */
    private String remark;

    /**
     * 预约状态（0待处理 1已确认 2已取消 3已完成）
     */
    private Integer appointStatus;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理备注
     */
    private String handlerRemark;

}
