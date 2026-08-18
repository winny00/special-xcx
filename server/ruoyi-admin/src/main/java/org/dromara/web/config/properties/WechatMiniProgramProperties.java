package org.dromara.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "special.wechat")
public class WechatMiniProgramProperties {

    /**
     * 小程序 AppID
     */
    private String appId = "wxf70d043d359a1586";

    /**
     * 小程序 AppSecret
     */
    private String appSecret = "b5de3db805f9b6f14880197b5bd18696";

}
