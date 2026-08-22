package org.dromara.special.util;

import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialWxPhoneParserTest {

    /**
     * Official-shaped WeChat getuserphonenumber payload.
     * phoneNumber includes country code; production must use purePhoneNumber.
     */
    private static final String SAMPLE_WECHAT_PAYLOAD = """
        {
          "errcode": 0,
          "errmsg": "ok",
          "phone_info": {
            "phoneNumber": "8613800138000",
            "purePhoneNumber": "13800138000",
            "countryCode": 86,
            "watermark": {
              "timestamp": 1637744274,
              "appid": "wxd8c7f6c8b7e6a5b4"
            }
          }
        }
        """;

    @Test
    void parsesPurePhoneNumberFromWechatSamplePayload() {
        assertEquals("13800138000", SpecialWxPhoneParser.parse(SAMPLE_WECHAT_PAYLOAD));
    }

    @Test
    void rejectsMissingPhoneInfo() {
        ServiceException ex = assertThrows(ServiceException.class,
            () -> SpecialWxPhoneParser.parse("{\"errcode\":0,\"errmsg\":\"ok\"}"));
        assertEquals("微信手机号解析失败", ex.getMessage());
    }

    @Test
    void rejectsWechatErrorPayload() {
        ServiceException ex = assertThrows(ServiceException.class,
            () -> SpecialWxPhoneParser.parse("{\"errcode\":40029,\"errmsg\":\"invalid code\"}"));
        assertEquals("微信手机号获取失败", ex.getMessage());
    }
}
