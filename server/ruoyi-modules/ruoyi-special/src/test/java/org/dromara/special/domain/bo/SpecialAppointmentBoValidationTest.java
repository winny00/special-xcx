package org.dromara.special.domain.bo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialAppointmentBoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void statusOnlyEditDoesNotRequireResourceId() {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setId(1L);
        bo.setAppointStatus(1);
        Set<ConstraintViolation<SpecialAppointmentBo>> violations = validator.validate(bo, EditGroup.class);
        assertTrue(violations.isEmpty(), () -> violations.iterator().next().getMessage());
    }

    @Test
    void addWithOnlyTeacherIdIsValid() {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setTeacherId(9001L);
        bo.setContactName("aaa");
        bo.setContactPhone("18878787878");
        Set<ConstraintViolation<SpecialAppointmentBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty(), () -> violations.iterator().next().getMessage());
    }

    @Test
    void addWithOnlyResourceIdIsValid() {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setResourceId(8001L);
        bo.setContactName("aaa");
        bo.setContactPhone("18878787878");
        Set<ConstraintViolation<SpecialAppointmentBo>> violations = validator.validate(bo, AddGroup.class);
        assertTrue(violations.isEmpty(), () -> violations.iterator().next().getMessage());
    }

    @Test
    void addRejectsWhenBothResourceIdAndTeacherIdMissing() {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setContactName("aaa");
        bo.setContactPhone("18878787878");
        Set<ConstraintViolation<SpecialAppointmentBo>> violations = validator.validate(bo, AddGroup.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "资源ID和老师ID不能同时为空".equals(v.getMessage())));
    }
}
