package org.dromara.special.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量审核
 */
@Data
public class SpecialAuditBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "审核对象不能为空")
    private List<Long> ids;

    /**
     * 机构/老师：0待审 1通过 2拒绝；资源：0草稿 1发布 2下架
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String remark;
}
