package org.dromara.special.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.special.domain.SpecialTeacher;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 特教老师视图
 */
@Data
@AutoMapper(target = SpecialTeacher.class)
public class SpecialTeacherVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    private Integer status;

    private Long resourceId;

    /**
     * 已绑账号的手机号（来自 sys_user，非档案表字段）
     */
    private String phone;

    private String auditRemark;

    private Long auditBy;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
