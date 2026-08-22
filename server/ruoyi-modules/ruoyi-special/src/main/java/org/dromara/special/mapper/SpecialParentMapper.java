package org.dromara.special.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.special.domain.vo.SpecialParentVo;

/**
 * 家长 CRM 只读查询
 */
public interface SpecialParentMapper {

    /**
     * 分页查询角色为 special_parent 的用户，附带预约次数。
     *
     * @param page    分页
     * @param keyword 昵称或手机号，可空
     * @return 家长分页
     */
    Page<SpecialParentVo> selectParentPage(Page<SpecialParentVo> page, @Param("keyword") String keyword);
}
