package org.dromara.special.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台开关家长 / 老师角色
 */
@Data
public class SpecialAccountRolesBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean parent;

    private Boolean teacher;
}
