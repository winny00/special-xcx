package org.dromara.system.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.domain.model.LoginBody;
import org.dromara.common.core.validate.RegisterGroup;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {

    /**
     * 用户名
     */
    @NotBlank(groups = {Default.class, RegisterGroup.class}, message = "{user.username.not.blank}")
    @Length(min = 2, max = 30, groups = {Default.class, RegisterGroup.class}, message = "{user.username.length.valid}")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(groups = {Default.class, RegisterGroup.class}, message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, groups = {Default.class, RegisterGroup.class}, message = "{user.password.length.valid}")
//    @Pattern(regexp = RegexConstants.PASSWORD, message = "{user.password.format.valid}")
    private String password;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 微信 getPhoneNumber 的 code；非空时后端换真号并跳过短信。
     */
    private String wxPhoneCode;

    /**
     * 注册校验 {@link RegisterGroup}，不继承登录的 grantType。
     */
    @NotBlank(groups = RegisterGroup.class, message = "{auth.clientid.not.blank}")
    @Override
    public String getClientId() {
        return super.getClientId();
    }

}
