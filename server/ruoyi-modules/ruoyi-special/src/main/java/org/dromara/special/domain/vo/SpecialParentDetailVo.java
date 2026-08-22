package org.dromara.special.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 家长 CRM 详情（完整手机号 + 最近预约）
 */
@Data
public class SpecialParentDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String nickName;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 完整手机号，供运营回拨
     */
    private String phone;

    private LocalDateTime createTime;

    private Long appointmentCount;

    private List<SpecialAppointmentVo> appointments;
}
