package org.dromara.special.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台用户角色查询条件
 */
@Data
public class SpecialAccountBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 昵称或手机号
     */
    private String keyword;
}
