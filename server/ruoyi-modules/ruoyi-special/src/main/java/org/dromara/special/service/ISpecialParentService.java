package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialParentBo;
import org.dromara.special.domain.vo.SpecialParentDetailVo;
import org.dromara.special.domain.vo.SpecialParentVo;

/**
 * 家长 CRM 只读查询
 */
public interface ISpecialParentService {

    /**
     * 分页查询家长列表（手机号脱敏）
     *
     * @param bo        昵称/手机号
     * @param pageQuery 分页
     * @return 家长分页
     */
    PageResult<SpecialParentVo> queryPageList(SpecialParentBo bo, PageQuery pageQuery);

    /**
     * 家长详情与最近预约。非家长抛业务异常。
     *
     * @param userId 用户 ID
     * @return 详情
     */
    SpecialParentDetailVo queryById(Long userId);
}
