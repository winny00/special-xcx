package org.dromara.special.util;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

/**
 * 审核状态机：拒绝必须填写备注。
 */
public final class SpecialAuditSupport {

    public static final int PENDING = 0;
    public static final int APPROVED = 1;
    public static final int REJECTED = 2;

    private SpecialAuditSupport() {
    }

    public static void requireRemarkWhenReject(Integer status, String remark) {
        if (Integer.valueOf(REJECTED).equals(status) && StringUtils.isBlank(remark)) {
            throw new ServiceException("拒绝时必须填写审核备注");
        }
    }
}
