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
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialTeacherService;
import org.dromara.special.util.SpecialAuditSupport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 特教老师档案
 */
@RequiredArgsConstructor
@Service
public class SpecialTeacherServiceImpl implements ISpecialTeacherService {

    private final SpecialTeacherMapper baseMapper;

    @Override
    public SpecialTeacherVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public PageResult<SpecialTeacherVo> queryPageList(SpecialTeacherBo bo, PageQuery pageQuery) {
        Page<SpecialTeacherVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialTeacherVo> queryList(SpecialTeacherBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PageResult<SpecialTeacherVo> queryApprovedPageList(SpecialTeacherBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialTeacher> lqw = buildQueryWrapper(bo);
        lqw.eq(SpecialTeacher::getStatus, SpecialAuditSupport.APPROVED);
        Page<SpecialTeacherVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public SpecialTeacherVo queryApprovedById(Long id) {
        SpecialTeacher teacher = baseMapper.selectById(id);
        if (teacher == null || !Integer.valueOf(SpecialAuditSupport.APPROVED).equals(teacher.getStatus())) {
            throw new ServiceException("老师不存在或未通过审核");
        }
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(SpecialTeacherBo bo) {
        SpecialTeacher add = MapstructUtils.convert(bo, SpecialTeacher.class);
        if (add.getStatus() == null) {
            add.setStatus(SpecialAuditSupport.PENDING);
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SpecialTeacherBo bo) {
        SpecialTeacher update = MapstructUtils.convert(bo, SpecialTeacher.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            List<SpecialTeacher> list = baseMapper.selectByIds(ids);
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
                throw new ServiceException("老师不存在");
            }
            SpecialTeacher update = new SpecialTeacher();
            update.setId(id);
            update.setStatus(bo.getStatus());
            update.setAuditRemark(bo.getRemark());
            update.setAuditBy(auditor);
            update.setAuditTime(now);
            baseMapper.updateById(update);
        }
        return true;
    }

    private LambdaQueryWrapper<SpecialTeacher> buildQueryWrapper(SpecialTeacherBo bo) {
        LambdaQueryWrapper<SpecialTeacher> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SpecialTeacher::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getSpecialties()), SpecialTeacher::getSpecialties, bo.getSpecialties());
        lqw.eq(bo.getOrgId() != null, SpecialTeacher::getOrgId, bo.getOrgId());
        lqw.eq(bo.getStatus() != null, SpecialTeacher::getStatus, bo.getStatus());
        lqw.orderByDesc(SpecialTeacher::getCreateTime);
        return lqw;
    }
}
