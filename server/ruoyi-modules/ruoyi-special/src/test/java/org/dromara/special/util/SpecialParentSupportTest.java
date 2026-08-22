package org.dromara.special.util;

import org.dromara.system.domain.vo.SysRoleVo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialParentSupportTest {

    @Test
    void masksElevenDigitMobile() {
        assertEquals("138****1234", SpecialParentSupport.maskPhone("13800001234"));
    }

    @Test
    void blankPhoneStaysBlank() {
        assertEquals("", SpecialParentSupport.maskPhone(""));
        assertNull(SpecialParentSupport.maskPhone(null));
    }

    @Test
    void detectsParentRole() {
        SysRoleVo parent = new SysRoleVo();
        parent.setRoleKey("special_parent");
        SysRoleVo teacher = new SysRoleVo();
        teacher.setRoleKey("special_teacher");
        assertTrue(SpecialParentSupport.isParent(List.of(parent)));
        assertFalse(SpecialParentSupport.isParent(List.of(teacher)));
        assertFalse(SpecialParentSupport.isParent(List.of()));
        assertFalse(SpecialParentSupport.isParent(null));
    }
}
