package org.dromara.special.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialCurrentRoleStoreTest {

    @Test
    void pickRoleForLoginForcesTeacherOnPcAndPrefersParentOnXcx() {
        assertEquals("special_teacher", SpecialCurrentRoleStore.pickRoleForLogin(
            SpecialIdentitySupport.PC_CLIENT_ID, Set.of("special_teacher", "special_parent")));
        assertEquals("special_parent", SpecialCurrentRoleStore.pickRoleForLogin(
            SpecialIdentitySupport.XCX_CLIENT_ID, Set.of("special_parent", "special_teacher")));
    }
}
