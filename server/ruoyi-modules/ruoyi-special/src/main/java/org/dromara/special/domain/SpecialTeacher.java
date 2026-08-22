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
 * 特教老师档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("special_teacher")
public class SpecialTeacher extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private String name;

    private String title;

    private String specialties;

    private String qualification;

    private String certImageUrl;

    private String avatarUrl;

    private Long orgId;

    private String intro;

    /**
     * 0待审 1已通过 2已拒绝
     */
    private Integer status;

    private Long resourceId;

    private String auditRemark;

    private Long auditBy;

    private LocalDateTime auditTime;

    @TableLogic
    private Long delFlag;
}
