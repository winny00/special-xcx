package org.dromara.special.util;

import cn.hutool.core.util.DesensitizedUtil;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 家长 CRM 只读查询的角色判断与手机号脱敏。
 */
public final class SpecialParentSupport {

    public static final String PARENT_ROLE_KEY = "special_parent";

    private SpecialParentSupport() {
    }

    public static String maskPhone(String phone) {
        if (phone == null) {
            return null;
        }
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        return DesensitizedUtil.mobilePhone(phone);
    }

    public static boolean isParent(List<SysRoleVo> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(role -> role != null && PARENT_ROLE_KEY.equals(role.getRoleKey()));
    }
}
