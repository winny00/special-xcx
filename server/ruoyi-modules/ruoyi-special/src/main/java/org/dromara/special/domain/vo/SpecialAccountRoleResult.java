package org.dromara.special.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开关角色结果：勾选老师但无档案时 needTeacherProfile=true。
 */
@Data
public class SpecialAccountRoleResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;

    private boolean needTeacherProfile;

    private String message;

    private String phone;

    public static SpecialAccountRoleResult ok() {
        SpecialAccountRoleResult result = new SpecialAccountRoleResult();
        result.setSuccess(true);
        return result;
    }

    public static SpecialAccountRoleResult needProfile(String phone) {
        SpecialAccountRoleResult result = new SpecialAccountRoleResult();
        result.setSuccess(false);
        result.setNeedTeacherProfile(true);
        result.setMessage("请先补全老师档案");
        result.setPhone(phone);
        return result;
    }
}
