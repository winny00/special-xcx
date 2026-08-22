package org.dromara.special.service;

/**
 * 用微信 getPhoneNumber code 换真实手机号。实现放在 ruoyi-admin，避免 special 依赖 admin。
 */
@FunctionalInterface
public interface SpecialWxPhoneLookup {

    String resolvePhone(String wxPhoneCode);
}
