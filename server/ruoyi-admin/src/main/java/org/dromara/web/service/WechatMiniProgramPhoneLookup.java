package org.dromara.web.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.special.service.SpecialWxPhoneLookup;
import org.dromara.special.util.SpecialWxPhoneParser;
import org.dromara.web.config.properties.WechatMiniProgramProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 调用微信 phonenumber.getPhoneNumber，实现 special 侧 {@link SpecialWxPhoneLookup}。
 */
@Slf4j
@Component
public class WechatMiniProgramPhoneLookup implements SpecialWxPhoneLookup {

    static final int CONNECT_TIMEOUT_MS = 3000;
    static final int READ_TIMEOUT_MS = 5000;

    private static final String TOKEN_URL =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String PHONE_URL =
        "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    @FunctionalInterface
    interface WechatHttpExchange {
        String exchange(String method, String url, String body);
    }

    private final WechatMiniProgramProperties properties;
    private final WechatHttpExchange http;

    private volatile String cachedAccessToken;
    private volatile long tokenExpireAtMillis;

    @Autowired
    public WechatMiniProgramPhoneLookup(WechatMiniProgramProperties properties) {
        this(properties, WechatMiniProgramPhoneLookup::exchange);
    }

    WechatMiniProgramPhoneLookup(WechatMiniProgramProperties properties, WechatHttpExchange http) {
        this.properties = properties;
        this.http = http;
    }

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
        String response = callWechat("POST", String.format(PHONE_URL, accessToken),
            JSONUtil.toJsonStr(Map.of("code", wxPhoneCode)));
        JSONObject root = parseResponse(response);
        log.info("WeChat getPhoneNumber errcode={}", root.getInt("errcode"));
        return SpecialWxPhoneParser.parse(response);
    }

    private String accessToken(String appId, String appSecret) {
        long now = System.currentTimeMillis();
        String cached = cachedAccessToken;
        if (StringUtils.isNotBlank(cached) && now < tokenExpireAtMillis) {
            return cached;
        }
        String response = callWechat("GET", String.format(TOKEN_URL, appId, appSecret), null);
        JSONObject root = parseResponse(response);
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

    private String callWechat(String method, String url, String body) {
        try {
            return http.exchange(method, url, body);
        } catch (ServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("WeChat HTTP failed: {}", ex.getClass().getSimpleName());
            throw new ServiceException("微信手机号获取失败");
        }
    }

    private JSONObject parseResponse(String response) {
        try {
            return JSONUtil.parseObj(response);
        } catch (RuntimeException ex) {
            log.warn("WeChat JSON parse failed: {}", ex.getClass().getSimpleName());
            throw new ServiceException("微信手机号获取失败");
        }
    }

    static HttpRequest timedRequest(String method, String url, String body) {
        HttpRequest request = "POST".equals(method)
            ? HttpRequest.post(url).body(body == null ? "" : body)
            : HttpRequest.get(url);
        return request.setConnectionTimeout(CONNECT_TIMEOUT_MS).setReadTimeout(READ_TIMEOUT_MS);
    }

    static String exchange(String method, String url, String body) {
        return timedRequest(method, url, body).execute().body();
    }
}
