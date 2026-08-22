package org.dromara.special.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 绑手机结果，字段与 LoginVo 对齐，便于前端换 token。
 */
@Data
public class SpecialBindPhoneVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expire_in")
    private Long expireIn;

    @JsonProperty("client_id")
    private String clientId;

}
