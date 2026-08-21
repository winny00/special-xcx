package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialArticleBo;
import org.dromara.special.domain.vo.SpecialArticleVo;

import java.util.Collection;
import java.util.List;

/**
 * 特教资讯Service接口
 *
 * @author special
 */
public interface ISpecialArticleService {

    /**
     * 查询资讯详情
     */
    SpecialArticleVo queryById(Long id);

    /**
     * 分页查询资讯列表（管理端）
     */
    PageResult<SpecialArticleVo> queryPageList(SpecialArticleBo bo, PageQuery pageQuery);

    /**
     * 查询资讯列表
     */
    List<SpecialArticleVo> queryList(SpecialArticleBo bo);

    /**
     * 分页查询已发布资讯（移动端）
     */
    PageResult<SpecialArticleVo> queryPublishedPageList(SpecialArticleBo bo, PageQuery pageQuery);

    /**
     * 查询已发布资讯详情并增加浏览次数
     */
    SpecialArticleVo queryPublishedById(Long id);

    /**
     * 新增资讯
     */
    Boolean insertByBo(SpecialArticleBo bo);

    /**
     * 修改资讯
     */
    Boolean updateByBo(SpecialArticleBo bo);

    /**
     * 校验并删除数据
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
