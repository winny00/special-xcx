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
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialResource;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialResourceBo;
import org.dromara.special.domain.vo.SpecialResourceVo;
import org.dromara.special.mapper.SpecialResourceMapper;
import org.dromara.special.service.ISpecialResourceService;
import org.dromara.special.util.SpecialAuditSupport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 特教资源Service业务层处理
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialResourceServiceImpl implements ISpecialResourceService {

    private final SpecialResourceMapper baseMapper;

    @Override
    public SpecialResourceVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public PageResult<SpecialResourceVo> queryPageList(SpecialResourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialResource> lqw = buildQueryWrapper(bo);
        Page<SpecialResourceVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialResourceVo> queryList(SpecialResourceBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PageResult<SpecialResourceVo> queryPublishedPageList(SpecialResourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialResource> lqw = buildQueryWrapper(bo);
        lqw.eq(SpecialResource::getStatus, 1);
        Page<SpecialResourceVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public SpecialResourceVo queryPublishedById(Long id) {
        SpecialResource resource = baseMapper.selectById(id);
        if (resource == null || !Integer.valueOf(1).equals(resource.getStatus())) {
            throw new ServiceException("资源不存在或未发布");
        }
        SpecialResource update = new SpecialResource();
        update.setId(id);
        update.setViewCount(resource.getViewCount() == null ? 1L : resource.getViewCount() + 1);
        baseMapper.updateById(update);
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(SpecialResourceBo bo) {
        SpecialResource add = MapstructUtils.convert(bo, SpecialResource.class);
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
    public Boolean updateByBo(SpecialResourceBo bo) {
        SpecialResource update = MapstructUtils.convert(bo, SpecialResource.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private LambdaQueryWrapper<SpecialResource> buildQueryWrapper(SpecialResourceBo bo) {
        LambdaQueryWrapper<SpecialResource> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), SpecialResource::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getResourceType()), SpecialResource::getResourceType, bo.getResourceType());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), SpecialResource::getCategory, bo.getCategory());
        lqw.eq(bo.getOrgId() != null, SpecialResource::getOrgId, bo.getOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getRegion()), SpecialResource::getRegion, bo.getRegion());
        lqw.eq(bo.getStatus() != null, SpecialResource::getStatus, bo.getStatus());
        lqw.orderByDesc(SpecialResource::getCreateTime);
        return lqw;
    }

    private void validEntityBeforeSave(SpecialResource entity) {
        // 预留业务校验
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            List<SpecialResource> list = baseMapper.selectByIds(ids);
            if (list.size() != ids.size()) {
                throw new ServiceException("部分数据不存在，无法删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public Boolean audit(SpecialAuditBo bo) {
        SpecialAuditSupport.requireRemarkWhenReject(bo.getStatus(), bo.getRemark());
        Long auditor = LoginHelper.getUserId();
        LocalDateTime now = LocalDateTime.now();
        for (Long id : bo.getIds()) {
            if (baseMapper.selectById(id) == null) {
                throw new ServiceException("资源不存在");
            }
            SpecialResource update = new SpecialResource();
            update.setId(id);
            update.setStatus(bo.getStatus());
            update.setAuditRemark(bo.getRemark());
            update.setAuditBy(auditor);
            update.setAuditTime(now);
            baseMapper.updateById(update);
        }
        return true;
    }

}
