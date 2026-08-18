package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialResourceBo;
import org.dromara.special.domain.vo.SpecialResourceVo;

import java.util.Collection;
import java.util.List;

/**
 * 特教资源Service接口
 *
 * @author special
 */
public interface ISpecialResourceService {

    /**
     * 查询特教资源详情
     *
     * @param id 主键
     * @return 特教资源视图对象
     */
    SpecialResourceVo queryById(Long id);

    /**
     * 分页查询特教资源列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SpecialResourceVo> queryPageList(SpecialResourceBo bo, PageQuery pageQuery);

    /**
     * 查询特教资源列表
     *
     * @param bo 查询条件
     * @return 结果列表
     */
    List<SpecialResourceVo> queryList(SpecialResourceBo bo);

    /**
     * 分页查询已发布资源（移动端）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SpecialResourceVo> queryPublishedPageList(SpecialResourceBo bo, PageQuery pageQuery);

    /**
     * 查询已发布资源详情并增加浏览次数
     *
     * @param id 主键
     * @return 特教资源视图对象
     */
    SpecialResourceVo queryPublishedById(Long id);

    /**
     * 新增特教资源
     *
     * @param bo 业务对象
     * @return 是否新增成功
     */
    Boolean insertByBo(SpecialResourceBo bo);

    /**
     * 修改特教资源
     *
     * @param bo 业务对象
     * @return 是否修改成功
     */
    Boolean updateByBo(SpecialResourceBo bo);

    /**
     * 校验并删除数据
     *
     * @param ids     主键集合
     * @param isValid 是否校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
