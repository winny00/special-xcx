package org.dromara.special.util;

import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialAuditSupportTest {

    @Test
    void rejectWithoutRemarkFails() {
        ServiceException ex = assertThrows(ServiceException.class,
            () -> SpecialAuditSupport.requireRemarkWhenReject(2, "  "));
        assertEquals("拒绝时必须填写审核备注", ex.getMessage());
    }

    @Test
    void rejectWithRemarkPasses() {
        assertDoesNotThrow(() -> SpecialAuditSupport.requireRemarkWhenReject(2, "资质不全"));
    }

    @Test
    void approveWithoutRemarkPasses() {
        assertDoesNotThrow(() -> SpecialAuditSupport.requireRemarkWhenReject(1, null));
    }
}
