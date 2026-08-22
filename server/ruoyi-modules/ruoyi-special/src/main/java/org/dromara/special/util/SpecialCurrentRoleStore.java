package org.dromara.special.util;

import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;

import java.util.Set;

/**
 * 登录会话中的当前角色读写。
 */
public final class SpecialCurrentRoleStore {

    public static final String SESSION_KEY = "specialCurrentRole";

    private SpecialCurrentRoleStore() {
    }

    /**
     * PC 老师（非超管）固定老师身份；其余走默认角色。
     */
    public static String pickRoleForLogin(String clientId, Set<String> roleKeys) {
        if (SpecialIdentitySupport.PC_CLIENT_ID.equals(clientId)
            && roleKeys != null
            && roleKeys.contains(SpecialIdentitySupport.TEACHER_ROLE_KEY)
            && !roleKeys.contains(SpecialIdentitySupport.SUPERADMIN_ROLE_KEY)) {
            return SpecialIdentitySupport.TEACHER_ROLE_KEY;
        }
        return SpecialIdentitySupport.defaultCurrentRole(roleKeys);
    }

    /**
     * 按登录端选择默认角色并写入会话。xcx 且无可切换角色时拒绝。
     */
    public static void applyDefault(String clientId, Set<String> roleKeys) {
        String roleKey = pickRoleForLogin(clientId, roleKeys);
        if (roleKey == null
            && SpecialIdentitySupport.XCX_CLIENT_ID.equals(clientId)
            && !SpecialIdentitySupport.canAccessPcAdmin(roleKeys)) {
            throw new ServiceException("账号未开通");
        }
        write(roleKey);
    }

    public static void write(String roleKey) {
        StpUtil.getTokenSession().set(SESSION_KEY, roleKey);
        LoginUser loginUser = LoginHelper.getLoginUser();
        loginUser.setCurrentRole(roleKey);
        StpUtil.getTokenSession().set(LoginHelper.LOGIN_USER_KEY, loginUser);
    }

    public static String read() {
        Object value = StpUtil.getTokenSession().get(SESSION_KEY);
        return value == null ? null : String.valueOf(value);
    }
}
