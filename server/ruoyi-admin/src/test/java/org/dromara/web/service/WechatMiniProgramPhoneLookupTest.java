package org.dromara.web.service;

import cn.hutool.http.HttpRequest;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.web.config.properties.WechatMiniProgramProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class WechatMiniProgramPhoneLookupTest {

    @Test
    void transportFailureBecomesServiceException() {
        WechatMiniProgramPhoneLookup lookup = new WechatMiniProgramPhoneLookup(
            configured(),
            (method, url, body) -> {
                throw new RuntimeException("connection reset");
            });

        ServiceException ex = assertThrows(ServiceException.class, () -> lookup.resolvePhone("wx-code"));
        assertEquals("微信手机号获取失败", ex.getMessage());
    }

    @Test
    void malformedJsonBecomesServiceException() {
        WechatMiniProgramPhoneLookup lookup = new WechatMiniProgramPhoneLookup(
            configured(),
            (method, url, body) -> "<html>502 Bad Gateway</html>");

        ServiceException ex = assertThrows(ServiceException.class, () -> lookup.resolvePhone("wx-code"));
        assertEquals("微信手机号获取失败", ex.getMessage());
    }

    @Test
    void timedRequestUsesFiniteConnectAndReadTimeouts() throws Exception {
        HttpRequest request = WechatMiniProgramPhoneLookup.timedRequest("GET", "https://example.invalid", null);
        Object config = fieldValue(request, "config");
        assertEquals(WechatMiniProgramPhoneLookup.CONNECT_TIMEOUT_MS, fieldValue(config, "connectionTimeout"));
        assertEquals(WechatMiniProgramPhoneLookup.READ_TIMEOUT_MS, fieldValue(config, "readTimeout"));
    }

    private static Object fieldValue(Object target, String name) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static WechatMiniProgramProperties configured() {
        WechatMiniProgramProperties properties = new WechatMiniProgramProperties();
        properties.setAppId("wx-test-app");
        properties.setAppSecret("wx-test-secret");
        return properties;
    }
}
