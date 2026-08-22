package org.dromara.special.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家长 CRM 列表行
 */
@Data
public class SpecialParentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String nickName;

    /**
     * 列表接口已脱敏
     */
    private String phone;

    private LocalDateTime createTime;

    private Long appointmentCount;
}
