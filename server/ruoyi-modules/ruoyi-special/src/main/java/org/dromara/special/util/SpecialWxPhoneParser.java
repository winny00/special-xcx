package org.dromara.special.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

/**
 * 解析微信 getuserphonenumber JSON，只取 phone_info.purePhoneNumber。
 */
public final class SpecialWxPhoneParser {

    private SpecialWxPhoneParser() {
    }

    public static String parse(String json) {
        if (StringUtils.isBlank(json)) {
            throw new ServiceException("微信手机号解析失败");
        }
        JSONObject root = JSONUtil.parseObj(json);
        Integer errcode = root.getInt("errcode");
        if (errcode != null && errcode != 0) {
            throw new ServiceException("微信手机号获取失败");
        }
        JSONObject phoneInfo = root.getJSONObject("phone_info");
        if (phoneInfo == null) {
            throw new ServiceException("微信手机号解析失败");
        }
        String phone = phoneInfo.getStr("purePhoneNumber");
        if (!SpecialIdentitySupport.isPhoneLogin(phone)) {
            throw new ServiceException("微信手机号解析失败");
        }
        return phone;
    }
}
