package org.dromara.special.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台重置账号密码
 */
@Data
public class SpecialAccountPasswordBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String password;
}
