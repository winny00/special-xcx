package org.dromara.special.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.dromara.common.core.domain.model.LoginBody;
import org.dromara.common.core.validate.RegisterGroup;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.system.api.model.RegisterBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class RegisterBodyValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerWithoutGrantTypePassesWhenRequiredFieldsPresent() {
        RegisterBody body = new RegisterBody();
        body.setUsername("13800138000");
        body.setPassword("pass123");
        body.setCode("1234");
        body.setClientId(SpecialIdentitySupport.XCX_CLIENT_ID);

        Set<ConstraintViolation<RegisterBody>> violations = validator.validate(body, RegisterGroup.class);

        assertTrue(violations.isEmpty(), () -> "unexpected: " + paths(violations));
        assertFalse(paths(violations).contains("grantType"));
    }

    @Test
    void registerStillRequiresUsernamePasswordAndClientId() {
        Set<String> paths = paths(validator.validate(new RegisterBody(), RegisterGroup.class));

        assertTrue(paths.contains("username"));
        assertTrue(paths.contains("password"));
        assertTrue(paths.contains("clientId"));
        assertFalse(paths.contains("grantType"));
    }

    @Test
    void loginWithoutGrantTypeFails() {
        LoginBody body = new LoginBody();
        body.setClientId(SpecialIdentitySupport.XCX_CLIENT_ID);

        Set<String> paths = paths(validator.validate(body));

        assertTrue(paths.contains("grantType"));
    }

    private static Set<String> paths(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
            .map(v -> v.getPropertyPath().toString())
            .collect(Collectors.toSet());
    }
}
