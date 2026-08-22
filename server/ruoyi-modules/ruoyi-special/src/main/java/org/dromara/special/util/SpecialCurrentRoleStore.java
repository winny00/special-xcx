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
     * 校验 xcx 账号已开通，返回应写入的角色。不碰会话。
     */
    public static String requireRoleForLogin(String clientId, Set<String> roleKeys) {
        String roleKey = pickRoleForLogin(clientId, roleKeys);
        if (roleKey == null
            && SpecialIdentitySupport.XCX_CLIENT_ID.equals(clientId)
            && !SpecialIdentitySupport.canAccessPcAdmin(roleKeys)) {
            throw new ServiceException("账号未开通");
        }
        return roleKey;
    }

    /**
     * 按登录端选择默认角色并写入会话。调用方须先 {@link #requireRoleForLogin}。
     */
    public static void applyDefault(String clientId, Set<String> roleKeys) {
        write(pickRoleForLogin(clientId, roleKeys));
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

    /**
     * getInfo：有会话角色则沿用；空则走 spec 默认（家长优先，否则老师）。
     * 不知道登录端，不能按「有老师」当成 PC。
     */
    public static String resolveForGetInfo(String sessionRole, Set<String> roleKeys) {
        if (sessionRole != null && !sessionRole.isBlank()) {
            return sessionRole;
        }
        return SpecialIdentitySupport.defaultCurrentRole(roleKeys);
    }

    /**
     * 读会话 currentRole；为空则按 {@link #resolveForGetInfo} 填回会话。
     */
    public static String readOrFill(Set<String> roleKeys) {
        String current = read();
        String resolved = resolveForGetInfo(current, roleKeys);
        if (current == null || current.isBlank()) {
            write(resolved);
        }
        return resolved;
    }
}
