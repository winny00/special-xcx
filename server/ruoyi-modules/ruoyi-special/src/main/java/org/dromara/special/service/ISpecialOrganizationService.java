package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialOrganizationBo;
import org.dromara.special.domain.vo.SpecialOrganizationVo;

import java.util.Collection;
import java.util.List;

/**
 * 特教机构Service接口
 *
 * @author special
 */
public interface ISpecialOrganizationService {

    /**
     * 查询特教机构详情
     *
     * @param id 主键
     * @return 特教机构视图对象
     */
    SpecialOrganizationVo queryById(Long id);

    /**
     * 分页查询特教机构列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SpecialOrganizationVo> queryPageList(SpecialOrganizationBo bo, PageQuery pageQuery);

    /**
     * 查询特教机构列表
     *
     * @param bo 查询条件
     * @return 结果列表
     */
    List<SpecialOrganizationVo> queryList(SpecialOrganizationBo bo);

    /**
     * 分页查询已审核通过机构（移动端）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SpecialOrganizationVo> queryApprovedPageList(SpecialOrganizationBo bo, PageQuery pageQuery);

    /**
     * 新增特教机构
     *
     * @param bo 业务对象
     * @return 是否新增成功
     */
    Boolean insertByBo(SpecialOrganizationBo bo);

    /**
     * 修改特教机构
     *
     * @param bo 业务对象
     * @return 是否修改成功
     */
    Boolean updateByBo(SpecialOrganizationBo bo);

    /**
     * 校验并删除数据
     *
     * @param ids     主键集合
     * @param isValid 是否校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    Boolean audit(SpecialAuditBo bo);

}
