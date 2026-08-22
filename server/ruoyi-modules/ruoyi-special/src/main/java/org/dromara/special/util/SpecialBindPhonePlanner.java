package org.dromara.special.util;

/**
 * 绑手机：写号 / 合并临时微信用户 / 拒绝 openid 冲突。
 */
public final class SpecialBindPhonePlanner {

    public enum BindAction {
        WRITE_PHONE,
        MERGE,
        REJECT
    }

    public static final class BindPlan {
        private final BindAction action;
        private final Long keepUserId;
        private final Long disableUserId;
        private final String message;

        public BindPlan(BindAction action, Long keepUserId, Long disableUserId, String message) {
            this.action = action;
            this.keepUserId = keepUserId;
            this.disableUserId = disableUserId;
            this.message = message;
        }

        public BindAction action() {
            return action;
        }

        public Long keepUserId() {
            return keepUserId;
        }

        public Long disableUserId() {
            return disableUserId;
        }

        public String message() {
            return message;
        }
    }

    private SpecialBindPhonePlanner() {
    }

    public static BindPlan plan(Long currentUserId, String currentPhone, Long phoneOwnerId, boolean openidBoundToOther) {
        if (openidBoundToOther) {
            return new BindPlan(BindAction.REJECT, null, null, "该微信已绑定其他账号");
        }
        if (phoneOwnerId == null || phoneOwnerId.equals(currentUserId)) {
            return new BindPlan(BindAction.WRITE_PHONE, currentUserId, null, null);
        }
        return new BindPlan(BindAction.MERGE, phoneOwnerId, currentUserId, null);
    }
}
