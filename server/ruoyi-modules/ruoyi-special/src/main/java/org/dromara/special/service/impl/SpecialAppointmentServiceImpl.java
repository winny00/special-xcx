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
import org.dromara.special.domain.SpecialAppointment;
import org.dromara.special.domain.SpecialResource;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialResourceMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.util.SpecialAuditSupport;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 特教预约Service业务层处理
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialAppointmentServiceImpl implements ISpecialAppointmentService {

    private final SpecialAppointmentMapper baseMapper;
    private final SpecialResourceMapper resourceMapper;
    private final SpecialTeacherMapper teacherMapper;
    private final ISysUserService userService;

    @Override
    public SpecialAppointmentVo queryById(Long id) {
        SpecialAppointmentVo vo = baseMapper.selectVoById(id);
        assertOwnsAppointment(vo == null ? null : vo.getTeacherId());
        return vo;
    }

    @Override
    public PageResult<SpecialAppointmentVo> queryPageList(SpecialAppointmentBo bo, PageQuery pageQuery) {
        applySelfTeacherId(bo);
        LambdaQueryWrapper<SpecialAppointment> lqw = buildQueryWrapper(bo);
        Page<SpecialAppointmentVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialAppointmentVo> queryList(SpecialAppointmentBo bo) {
        applySelfTeacherId(bo);
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public Boolean insertByBo(SpecialAppointmentBo bo) {
        SpecialAppointment add = MapstructUtils.convert(bo, SpecialAppointment.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean createMobileAppointment(SpecialAppointmentBo bo) {
        requirePhoneBound();
        if (bo.getTeacherId() != null) {
            SpecialTeacher teacher = teacherMapper.selectById(bo.getTeacherId());
            if (teacher == null || !Integer.valueOf(SpecialAuditSupport.APPROVED).equals(teacher.getStatus())) {
                throw new ServiceException("老师不存在或未通过审核");
            }
            if (StringUtils.isBlank(bo.getResourceTitle())) {
                bo.setResourceTitle(teacher.getName());
            }
        }
        if (bo.getResourceId() != null) {
            SpecialResource resource = resourceMapper.selectById(bo.getResourceId());
            if (resource == null || !Integer.valueOf(1).equals(resource.getStatus())) {
                throw new ServiceException("资源不存在或未发布，无法预约");
            }
            if (StringUtils.isBlank(bo.getResourceTitle())) {
                bo.setResourceTitle(resource.getTitle());
            }
        }
        if (LoginHelper.isLogin()) {
            bo.setUserId(LoginHelper.getUserId());
        }
        if (bo.getAppointStatus() == null) {
            bo.setAppointStatus(0);
        }
        return insertByBo(bo);
    }

    @Override
    public Boolean updateByBo(SpecialAppointmentBo bo) {
        SpecialAppointment current = bo.getId() == null ? null : baseMapper.selectById(bo.getId());
        assertOwnsAppointment(current == null ? null : current.getTeacherId());
        SpecialAppointment update = SpecialIdentitySupport.currentUserIsTeacherOnly()
            ? teacherProcessingUpdate(current, bo)
            : MapstructUtils.convert(bo, SpecialAppointment.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 老师只能改处理状态/备注/处理人，不能改家长身份、资源、联系人或备注。
     */
    private SpecialAppointment teacherProcessingUpdate(SpecialAppointment current, SpecialAppointmentBo bo) {
        SpecialAppointment update = new SpecialAppointment();
        update.setId(current.getId());
        if (bo.getAppointStatus() != null) {
            update.setAppointStatus(bo.getAppointStatus());
        }
        if (bo.getHandlerRemark() != null) {
            update.setHandlerRemark(bo.getHandlerRemark());
        }
        if (bo.getHandlerId() != null) {
            update.setHandlerId(bo.getHandlerId());
        }
        return update;
    }

    private LambdaQueryWrapper<SpecialAppointment> buildQueryWrapper(SpecialAppointmentBo bo) {
        LambdaQueryWrapper<SpecialAppointment> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getResourceId() != null, SpecialAppointment::getResourceId, bo.getResourceId());
        lqw.like(StringUtils.isNotBlank(bo.getResourceTitle()), SpecialAppointment::getResourceTitle, bo.getResourceTitle());
        lqw.eq(bo.getUserId() != null, SpecialAppointment::getUserId, bo.getUserId());
        lqw.eq(bo.getTeacherId() != null, SpecialAppointment::getTeacherId, bo.getTeacherId());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), SpecialAppointment::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), SpecialAppointment::getContactPhone, bo.getContactPhone());
        lqw.eq(bo.getAppointStatus() != null, SpecialAppointment::getAppointStatus, bo.getAppointStatus());
        lqw.eq(bo.getHandlerId() != null, SpecialAppointment::getHandlerId, bo.getHandlerId());
        lqw.orderByDesc(SpecialAppointment::getCreateTime);
        return lqw;
    }

    private void requirePhoneBound() {
        if (!LoginHelper.isLogin()) {
            throw new ServiceException("请先绑定手机号");
        }
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        if (user == null || StringUtils.isBlank(user.getPhoneNumber())) {
            throw new ServiceException("请先绑定手机号");
        }
    }

    private void validEntityBeforeSave(SpecialAppointment entity) {
        // 预留业务校验
    }

    private void applySelfTeacherId(SpecialAppointmentBo bo) {
        if (!SpecialIdentitySupport.currentUserIsTeacherOnly()) {
            return;
        }
        Long ownTeacherId = resolveOwnTeacherId();
        bo.setTeacherId(ownTeacherId == null ? -1L : ownTeacherId);
    }

    private Long resolveOwnTeacherId() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return null;
        }
        SpecialTeacher own = teacherMapper.selectOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, userId));
        return own == null ? null : own.getId();
    }

    private void assertOwnsAppointment(Long rowTeacherId) {
        boolean teacherOnly = SpecialIdentitySupport.currentUserIsTeacherOnly();
        Long ownTeacherId = teacherOnly ? resolveOwnTeacherId() : null;
        boolean owning = ownTeacherId != null && ownTeacherId.equals(rowTeacherId);
        SpecialIdentitySupport.assertTeacherOwns(teacherOnly, owning);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            List<SpecialAppointment> list = baseMapper.selectByIds(ids);
            if (list.size() != ids.size()) {
                throw new ServiceException("部分数据不存在，无法删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

}
