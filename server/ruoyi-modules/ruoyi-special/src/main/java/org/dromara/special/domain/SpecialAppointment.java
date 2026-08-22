package org.dromara.special.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 特教预约对象 special_appointment
 *
 * @author special
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("special_appointment")
public class SpecialAppointment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 资源ID
     */
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
     * 老师档案ID
     */
    private Long teacherId;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
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

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;

}
