package org.dromara.special.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 移动端家长资料视图
 *
 * @author special
 */
@Data
public class SpecialMobileProfileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 脱敏手机号
     */
    private String phone;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 会话当前身份
     */
    private String currentRole;

    /**
     * 已开通角色
     */
    private List<String> roles;

    /**
     * 是否已绑定手机号
     */
    private Boolean phoneBound;

}
