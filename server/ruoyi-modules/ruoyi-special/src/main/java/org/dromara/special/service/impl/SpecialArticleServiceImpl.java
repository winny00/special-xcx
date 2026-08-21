package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.SpecialArticle;
import org.dromara.special.domain.bo.SpecialArticleBo;
import org.dromara.special.domain.vo.SpecialArticleVo;
import org.dromara.special.mapper.SpecialArticleMapper;
import org.dromara.special.service.ISpecialArticleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 特教资讯Service业务层处理
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialArticleServiceImpl implements ISpecialArticleService {

    private final SpecialArticleMapper baseMapper;

    @Override
    public SpecialArticleVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public PageResult<SpecialArticleVo> queryPageList(SpecialArticleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialArticle> lqw = buildQueryWrapper(bo);
        Page<SpecialArticleVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialArticleVo> queryList(SpecialArticleBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PageResult<SpecialArticleVo> queryPublishedPageList(SpecialArticleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialArticle> lqw = buildQueryWrapper(bo);
        lqw.eq(SpecialArticle::getStatus, 1);
        lqw.orderByDesc(SpecialArticle::getPublishTime);
        Page<SpecialArticleVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public SpecialArticleVo queryPublishedById(Long id) {
        SpecialArticle article = baseMapper.selectById(id);
        if (article == null || !Integer.valueOf(1).equals(article.getStatus())) {
            throw new ServiceException("资讯不存在或未发布");
        }
        SpecialArticle update = new SpecialArticle();
        update.setId(id);
        update.setViewCount(article.getViewCount() == null ? 1L : article.getViewCount() + 1);
        baseMapper.updateById(update);
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(SpecialArticleBo bo) {
        SpecialArticle add = MapstructUtils.convert(bo, SpecialArticle.class);
        validEntityBeforeSave(add);
        if (add.getViewCount() == null) {
            add.setViewCount(0L);
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SpecialArticleBo bo) {
        SpecialArticle update = MapstructUtils.convert(bo, SpecialArticle.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private LambdaQueryWrapper<SpecialArticle> buildQueryWrapper(SpecialArticleBo bo) {
        LambdaQueryWrapper<SpecialArticle> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), SpecialArticle::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), SpecialArticle::getCategory, bo.getCategory());
        lqw.eq(bo.getStatus() != null, SpecialArticle::getStatus, bo.getStatus());
        lqw.orderByDesc(SpecialArticle::getPublishTime);
        lqw.orderByDesc(SpecialArticle::getCreateTime);
        return lqw;
    }

    private void validEntityBeforeSave(SpecialArticle entity) {
        if (Integer.valueOf(1).equals(entity.getStatus()) && entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            List<SpecialArticle> list = baseMapper.selectByIds(ids);
            if (list.size() != ids.size()) {
                throw new ServiceException("部分数据不存在，无法删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

}
