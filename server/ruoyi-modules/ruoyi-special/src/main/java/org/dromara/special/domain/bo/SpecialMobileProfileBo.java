package org.dromara.special.domain.bo;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.common.core.constant.RegexConstants;
import org.dromara.common.core.xss.Xss;

import java.io.Serial;
import java.io.Serializable;

/**
 * 移动端家长资料更新
 *
 * @author special
 */
@Data
public class SpecialMobileProfileBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过{max}个字符")
    private String nickname;

    /**
     * 手机号
     */
    @Pattern(regexp = RegexConstants.MOBILE, message = "手机号格式不正确")
    private String phone;

}
