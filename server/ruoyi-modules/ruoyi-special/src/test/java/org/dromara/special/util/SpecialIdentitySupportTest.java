package org.dromara.special.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialIdentitySupportTest {

    @Test
    void phoneLoginIsElevenDigits() {
        assertTrue(SpecialIdentitySupport.isPhoneLogin("13800138000"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin("admin"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin("1380013800"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin(null));
        assertFalse(SpecialIdentitySupport.isPhoneLogin("１３８００１３８０００"));
    }

    @Test
    void defaultRolePrefersParentThenTeacher() {
        assertEquals("special_parent", SpecialIdentitySupport.defaultCurrentRole(Set.of("special_parent", "special_teacher")));
        assertEquals("special_teacher", SpecialIdentitySupport.defaultCurrentRole(Set.of("special_teacher")));
        assertNull(SpecialIdentitySupport.defaultCurrentRole(Set.of("superadmin")));
    }

    @Test
    void switchOnlyToOwnedSpecialRoles() {
        Set<String> both = Set.of("special_parent", "special_teacher");
        assertTrue(SpecialIdentitySupport.canSwitchTo(both, "special_teacher"));
        assertFalse(SpecialIdentitySupport.canSwitchTo(Set.of("special_parent"), "special_teacher"));
        assertFalse(SpecialIdentitySupport.canSwitchTo(both, "superadmin"));
    }

    @Test
    void pcAccess() {
        assertTrue(SpecialIdentitySupport.canAccessPcAdmin(Set.of("superadmin")));
        assertTrue(SpecialIdentitySupport.canAccessPcAdmin(Set.of("special_teacher", "special_parent")));
        assertFalse(SpecialIdentitySupport.canAccessPcAdmin(Set.of("special_parent")));
    }

    @Test
    void cannotDropLastRole() {
        assertThrows(IllegalArgumentException.class,
            () -> SpecialIdentitySupport.assertKeepAtLeastOneRole(false, false));
        SpecialIdentitySupport.assertKeepAtLeastOneRole(true, false);
    }
}
