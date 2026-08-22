package org.dromara.special.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.special.domain.vo.SpecialAccountVo;

/**
 * 后台用户角色列表
 */
public interface SpecialAccountMapper {

    /**
     * 分页查询同时拥有家长或老师角色的用户。
     *
     * @param page    分页
     * @param keyword 昵称或手机号，可空
     * @return 账号分页
     */
    Page<SpecialAccountVo> selectAccountPage(Page<SpecialAccountVo> page, @Param("keyword") String keyword);
}
