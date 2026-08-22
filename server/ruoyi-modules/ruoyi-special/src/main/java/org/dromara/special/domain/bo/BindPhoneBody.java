package org.dromara.special.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 移动端绑手机请求。wxPhoneCode 非空时走微信真号并跳过短信。
 */
@Data
public class BindPhoneBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String phone;

    private String smsCode;

    /**
     * 微信 getPhoneNumber 的 code。
     */
    private String wxPhoneCode;

}
