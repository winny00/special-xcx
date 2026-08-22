package org.dromara.web.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.special.service.SpecialWxPhoneLookup;
import org.dromara.special.util.SpecialWxPhoneParser;
import org.dromara.web.config.properties.WechatMiniProgramProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 调用微信 phonenumber.getPhoneNumber，实现 special 侧 {@link SpecialWxPhoneLookup}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniProgramPhoneLookup implements SpecialWxPhoneLookup {

    private static final String TOKEN_URL =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String PHONE_URL =
        "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    private final WechatMiniProgramProperties properties;

    private volatile String cachedAccessToken;
    private volatile long tokenExpireAtMillis;

    @Override
    public String resolvePhone(String wxPhoneCode) {
        if (StringUtils.isBlank(wxPhoneCode)) {
            throw new ServiceException("微信手机号授权失败");
        }
        String appId = properties.getAppId();
        String appSecret = properties.getAppSecret();
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
            throw new ServiceException("请先配置 special.wechat.app-id 和 app-secret");
        }
        String accessToken = accessToken(appId, appSecret);
        String response = HttpUtil.post(
            String.format(PHONE_URL, accessToken),
            JSONUtil.toJsonStr(Map.of("code", wxPhoneCode)));
        JSONObject root = JSONUtil.parseObj(response);
        log.info("WeChat getPhoneNumber errcode={}", root.getInt("errcode"));
        return SpecialWxPhoneParser.parse(response);
    }

    private String accessToken(String appId, String appSecret) {
        long now = System.currentTimeMillis();
        String cached = cachedAccessToken;
        if (StringUtils.isNotBlank(cached) && now < tokenExpireAtMillis) {
            return cached;
        }
        String response = HttpUtil.get(String.format(TOKEN_URL, appId, appSecret));
        JSONObject root = JSONUtil.parseObj(response);
        String token = root.getStr("access_token");
        if (StringUtils.isBlank(token)) {
            log.warn("WeChat access token failed, errcode={}", root.getInt("errcode"));
            throw new ServiceException("微信手机号获取失败");
        }
        int expiresIn = root.getInt("expires_in", 7200);
        cachedAccessToken = token;
        tokenExpireAtMillis = now + Math.max(60, expiresIn - 300) * 1000L;
        return token;
    }
}
