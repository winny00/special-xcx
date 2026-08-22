package org.dromara.special.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialBindPhonePlannerTest {

    private static final Long CURRENT_USER_ID = 1001L;
    private static final Long PHONE_OWNER_ID = 2002L;
    private static final String CURRENT_PHONE = "";
    private static final String PHONE = "13800138000";

    @Test
    void rejectsWhenOpenidAlreadyBoundToAnotherAccount() {
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, CURRENT_PHONE, PHONE, PHONE_OWNER_ID, true);

        assertEquals(SpecialBindPhonePlanner.BindAction.REJECT, plan.action());
        assertEquals("该微信已绑定其他账号", plan.message());
        assertNull(plan.keepUserId());
        assertNull(plan.disableUserId());
    }

    @Test
    void writesPhoneWhenNumberHasNoOwner() {
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, CURRENT_PHONE, PHONE, null, false);

        assertEquals(SpecialBindPhonePlanner.BindAction.WRITE_PHONE, plan.action());
        assertEquals(CURRENT_USER_ID, plan.keepUserId());
        assertNull(plan.disableUserId());
        assertNull(plan.message());
    }

    @Test
    void writesPhoneWhenOwnerIsCurrentUser() {
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, PHONE, PHONE, CURRENT_USER_ID, false);

        assertEquals(SpecialBindPhonePlanner.BindAction.WRITE_PHONE, plan.action());
        assertEquals(CURRENT_USER_ID, plan.keepUserId());
        assertNull(plan.disableUserId());
        assertNull(plan.message());
    }

    @Test
    void mergesWhenPhoneBelongsToAnotherUser() {
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, CURRENT_PHONE, PHONE, PHONE_OWNER_ID, false);

        assertEquals(SpecialBindPhonePlanner.BindAction.MERGE, plan.action());
        assertEquals(PHONE_OWNER_ID, plan.keepUserId());
        assertEquals(CURRENT_USER_ID, plan.disableUserId());
        assertNull(plan.message());
    }

    @Test
    void rejectsMergeWhenCurrentUserAlreadyHasDifferentPhone() {
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, "13900139000", PHONE, PHONE_OWNER_ID, false);

        assertEquals(SpecialBindPhonePlanner.BindAction.REJECT, plan.action());
        assertEquals("当前账号已绑定其他手机号", plan.message());
        assertNull(plan.keepUserId());
        assertNull(plan.disableUserId());
    }

    @Test
    void openidConflictWinsOverWritePhoneAndMerge() {
        SpecialBindPhonePlanner.BindPlan noOwner = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, CURRENT_PHONE, PHONE, null, true);
        SpecialBindPhonePlanner.BindPlan selfOwner = SpecialBindPhonePlanner.plan(
            CURRENT_USER_ID, PHONE, PHONE, CURRENT_USER_ID, true);

        assertEquals(SpecialBindPhonePlanner.BindAction.REJECT, noOwner.action());
        assertEquals("该微信已绑定其他账号", noOwner.message());
        assertEquals(SpecialBindPhonePlanner.BindAction.REJECT, selfOwner.action());
        assertEquals("该微信已绑定其他账号", selfOwner.message());
    }
}
