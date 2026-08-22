package org.dromara.special.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 后台用户角色列表行。userId 固定字符串，避免雪花精度丢失。
 */
@Data
public class SpecialAccountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String userId;

    /**
     * 列表已脱敏
     */
    private String phone;

    private String nickname;

    private List<String> roles;

    /**
     * sys_user.status：0 正常 / 1 停用
     */
    private String status;
}
