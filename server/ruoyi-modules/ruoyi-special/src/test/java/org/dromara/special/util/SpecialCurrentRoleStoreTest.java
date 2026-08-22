package org.dromara.special.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialCurrentRoleStoreTest {

    @Test
    void pickRoleForLoginForcesTeacherOnPcAndPrefersParentOnXcx() {
        assertEquals("special_teacher", SpecialCurrentRoleStore.pickRoleForLogin(
            SpecialIdentitySupport.PC_CLIENT_ID, Set.of("special_teacher", "special_parent")));
        assertEquals("special_parent", SpecialCurrentRoleStore.pickRoleForLogin(
            SpecialIdentitySupport.XCX_CLIENT_ID, Set.of("special_parent", "special_teacher")));
    }

    @Test
    void xcxUnopenedAccountThrowsBeforeAnySessionWrite() {
        ServiceException ex = assertThrows(ServiceException.class, () ->
            SpecialCurrentRoleStore.requireRoleForLogin(
                SpecialIdentitySupport.XCX_CLIENT_ID, Set.<String>of()));
        assertEquals("账号未开通", ex.getMessage());
        assertEquals("special_parent", SpecialCurrentRoleStore.requireRoleForLogin(
            SpecialIdentitySupport.XCX_CLIENT_ID, Set.of("special_parent")));
    }

    @Test
    void resolveForGetInfoKeepsSessionAndFillsWithoutClientKey() {
        assertEquals("special_parent", SpecialCurrentRoleStore.resolveForGetInfo(
            "special_parent", Set.of("special_parent", "special_teacher")));
        assertEquals("special_parent", SpecialCurrentRoleStore.resolveForGetInfo(
            null, Set.of("special_parent", "special_teacher")));
        assertEquals("special_teacher", SpecialCurrentRoleStore.resolveForGetInfo(
            null, Set.of("special_teacher")));
        assertEquals("special_parent", SpecialCurrentRoleStore.resolveForGetInfo(
            "", Set.of("special_parent")));
        assertNull(SpecialCurrentRoleStore.resolveForGetInfo(null, Set.of("superadmin")));
    }

    @Test
    void shouldPersistRoleRejectsNullSoWriteDoesNotThrow() {
        assertFalse(SpecialCurrentRoleStore.shouldPersistRole(null));
        assertTrue(SpecialCurrentRoleStore.shouldPersistRole("special_parent"));
        assertTrue(SpecialCurrentRoleStore.shouldPersistRole("special_teacher"));
    }

    @Test
    void readOrFillDoesNotWriteNullForSuperadmin() {
        SaSession session = mock(SaSession.class);
        when(session.get(SpecialCurrentRoleStore.SESSION_KEY)).thenReturn(null);
        LoginUser loginUser = new LoginUser();
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class);
             MockedStatic<LoginHelper> helper = mockStatic(LoginHelper.class)) {
            stp.when(StpUtil::getTokenSession).thenReturn(session);
            helper.when(LoginHelper::getLoginUser).thenReturn(loginUser);

            assertNull(SpecialCurrentRoleStore.readOrFill(Set.of("superadmin")));

            verify(session, never()).set(eq(SpecialCurrentRoleStore.SESSION_KEY), isNull());
            verify(session, never()).set(eq(LoginHelper.LOGIN_USER_KEY), any());
            assertNull(loginUser.getCurrentRole());
        }
    }
}
