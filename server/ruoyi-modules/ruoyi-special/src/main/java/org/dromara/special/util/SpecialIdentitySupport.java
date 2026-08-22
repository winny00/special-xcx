package org.dromara.special.util;

import java.util.Set;

/**
 * 一人一号多角色的身份规则纯函数。
 */
public final class SpecialIdentitySupport {

    public static final String PARENT_ROLE_KEY = SpecialParentSupport.PARENT_ROLE_KEY;
    public static final String TEACHER_ROLE_KEY = "special_teacher";
    public static final String SUPERADMIN_ROLE_KEY = "superadmin";
    public static final String PC_CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e";
    public static final String XCX_CLIENT_ID = "special_xcx_client_id";

    private SpecialIdentitySupport() {
    }

    public static boolean isPhoneLogin(String username) {
        if (username == null || username.length() != 11) {
            return false;
        }
        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static String defaultCurrentRole(Set<String> roleKeys) {
        if (roleKeys == null || roleKeys.isEmpty()) {
            return null;
        }
        if (roleKeys.contains(PARENT_ROLE_KEY)) {
            return PARENT_ROLE_KEY;
        }
        if (roleKeys.contains(TEACHER_ROLE_KEY)) {
            return TEACHER_ROLE_KEY;
        }
        return null;
    }

    public static boolean canSwitchTo(Set<String> owned, String target) {
        if (owned == null || target == null) {
            return false;
        }
        if (!owned.contains(target)) {
            return false;
        }
        return PARENT_ROLE_KEY.equals(target) || TEACHER_ROLE_KEY.equals(target);
    }

    /**
     * 非法切换时返回失败文案，合法则 null。
     */
    public static String switchError(Set<String> owned, String target) {
        if (canSwitchTo(owned, target)) {
            return null;
        }
        return "当前账号没有该身份";
    }

    public static boolean canAccessPcAdmin(Set<String> roleKeys) {
        if (roleKeys == null || roleKeys.isEmpty()) {
            return false;
        }
        return roleKeys.contains(SUPERADMIN_ROLE_KEY) || roleKeys.contains(TEACHER_ROLE_KEY);
    }

    /**
     * getInfo 会话空时选用的 clientId。不把 LoginUser.clientKey 映射成 UUID。
     */
    public static String clientIdForEmptyCurrentRole(Set<String> roleKeys) {
        if (canAccessPcAdmin(roleKeys)
            && roleKeys != null
            && roleKeys.contains(TEACHER_ROLE_KEY)
            && !roleKeys.contains(SUPERADMIN_ROLE_KEY)) {
            return PC_CLIENT_ID;
        }
        return XCX_CLIENT_ID;
    }

    public static void assertKeepAtLeastOneRole(boolean parent, boolean teacher) {
        if (!parent && !teacher) {
            throw new IllegalArgumentException("至少保留一个角色");
        }
    }

    public static boolean smsCodeMatches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.equals(actual);
    }

    public static boolean isSmsPlaceholderConfig(String accessKeyId) {
        if (accessKeyId == null || accessKeyId.isBlank()) {
            return true;
        }
        return accessKeyId.contains("您的");
    }
}
