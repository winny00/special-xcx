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
import org.dromara.special.domain.SpecialOrganization;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialOrganizationBo;
import org.dromara.special.domain.vo.SpecialOrganizationVo;
import org.dromara.special.mapper.SpecialOrganizationMapper;
import org.dromara.special.service.ISpecialOrganizationService;
import org.dromara.special.util.SpecialAuditSupport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 特教机构Service业务层处理
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialOrganizationServiceImpl implements ISpecialOrganizationService {

    private final SpecialOrganizationMapper baseMapper;

    @Override
    public SpecialOrganizationVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public PageResult<SpecialOrganizationVo> queryPageList(SpecialOrganizationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialOrganization> lqw = buildQueryWrapper(bo);
        Page<SpecialOrganizationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialOrganizationVo> queryList(SpecialOrganizationBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PageResult<SpecialOrganizationVo> queryApprovedPageList(SpecialOrganizationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialOrganization> lqw = buildQueryWrapper(bo);
        lqw.eq(SpecialOrganization::getAuditStatus, 1);
        lqw.eq(SpecialOrganization::getStatus, 1);
        Page<SpecialOrganizationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public Boolean insertByBo(SpecialOrganizationBo bo) {
        SpecialOrganization add = MapstructUtils.convert(bo, SpecialOrganization.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SpecialOrganizationBo bo) {
        SpecialOrganization update = MapstructUtils.convert(bo, SpecialOrganization.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private LambdaQueryWrapper<SpecialOrganization> buildQueryWrapper(SpecialOrganizationBo bo) {
        LambdaQueryWrapper<SpecialOrganization> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SpecialOrganization::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getOrgType()), SpecialOrganization::getOrgType, bo.getOrgType());
        lqw.eq(StringUtils.isNotBlank(bo.getRegion()), SpecialOrganization::getRegion, bo.getRegion());
        lqw.eq(bo.getAuditStatus() != null, SpecialOrganization::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getStatus() != null, SpecialOrganization::getStatus, bo.getStatus());
        lqw.orderByDesc(SpecialOrganization::getCreateTime);
        return lqw;
    }

    private void validEntityBeforeSave(SpecialOrganization entity) {
        // 预留业务校验
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            List<SpecialOrganization> list = baseMapper.selectByIds(ids);
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
                throw new ServiceException("机构不存在");
            }
            SpecialOrganization update = new SpecialOrganization();
            update.setId(id);
            update.setAuditStatus(bo.getStatus());
            update.setAuditRemark(bo.getRemark());
            update.setAuditBy(auditor);
            update.setAuditTime(now);
            baseMapper.updateById(update);
        }
        return true;
    }

}
