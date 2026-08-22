package org.dromara.special.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 移动端绑手机请求。本任务只处理 phone + smsCode；wxPhoneCode 留给 Task 11。
 */
@Data
public class BindPhoneBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String phone;

    private String smsCode;

    /**
     * 微信 getPhoneNumber 的 code，本任务不使用。
     */
    private String wxPhoneCode;

}
