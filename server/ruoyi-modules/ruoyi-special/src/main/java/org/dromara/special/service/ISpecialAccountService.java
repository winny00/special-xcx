package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAccountBo;
import org.dromara.special.domain.bo.SpecialAccountRolesBody;
import org.dromara.special.domain.vo.SpecialAccountRoleResult;
import org.dromara.special.domain.vo.SpecialAccountVo;

/**
 * 后台用户角色（家长 / 老师开关）
 */
public interface ISpecialAccountService {

    PageResult<SpecialAccountVo> queryPageList(SpecialAccountBo bo, PageQuery pageQuery);

    SpecialAccountRoleResult updateRoles(Long userId, SpecialAccountRolesBody body);

    void resetPassword(Long userId, String password);
}
