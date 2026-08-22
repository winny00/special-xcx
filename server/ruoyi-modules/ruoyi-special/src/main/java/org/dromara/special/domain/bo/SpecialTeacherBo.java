package org.dromara.special.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.special.domain.SpecialTeacher;

import java.io.Serial;
import java.io.Serializable;

/**
 * 特教老师业务对象
 */
@Data
@AutoMapper(target = SpecialTeacher.class, reverseConvertGenerate = false)
public class SpecialTeacherBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private Long userId;

    @NotBlank(message = "姓名不能为空", groups = {AddGroup.class, EditGroup.class})
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
     * 开通登录用手机号（不落老师档案表）
     */
    private String phone;

    /**
     * 新建登录账号时的初始密码（不落老师档案表）
     */
    private String initPassword;
}
