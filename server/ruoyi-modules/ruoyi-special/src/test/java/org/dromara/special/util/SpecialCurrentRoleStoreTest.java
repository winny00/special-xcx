package org.dromara.special.util;

import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
