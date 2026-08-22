package org.dromara.special.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.bo.SpecialParentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialParentDetailVo;
import org.dromara.special.domain.vo.SpecialParentVo;
import org.dromara.special.mapper.SpecialParentMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.service.ISpecialParentService;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 家长 CRM 只读查询
 */
@RequiredArgsConstructor
@Service
public class SpecialParentServiceImpl implements ISpecialParentService {

    private static final int RECENT_APPOINTMENT_LIMIT = 20;

    private final SpecialParentMapper parentMapper;
    private final ISysUserService userService;
    private final ISpecialAppointmentService appointmentService;

    @Override
    public PageResult<SpecialParentVo> queryPageList(SpecialParentBo bo, PageQuery pageQuery) {
        String keyword = bo == null ? null : bo.getKeyword();
        Page<SpecialParentVo> page = parentMapper.selectParentPage(pageQuery.build(), keyword);
        if (page.getRecords() != null) {
            for (SpecialParentVo row : page.getRecords()) {
                row.setPhone(SpecialParentSupport.maskPhone(row.getPhone()));
            }
        }
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public SpecialParentDetailVo queryById(Long userId) {
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (!SpecialParentSupport.isParent(user.getRoles())) {
            throw new ServiceException("该用户不是家长");
        }

        SpecialAppointmentBo appointmentBo = new SpecialAppointmentBo();
        appointmentBo.setUserId(userId);
        PageQuery recentQuery = new PageQuery();
        recentQuery.setPageNum(1);
        recentQuery.setPageSize(RECENT_APPOINTMENT_LIMIT);
        PageResult<SpecialAppointmentVo> appointments = appointmentService.queryPageList(appointmentBo, recentQuery);

        SpecialParentDetailVo detail = new SpecialParentDetailVo();
        detail.setUserId(user.getUserId());
        detail.setNickName(StringUtils.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUserName());
        detail.setAvatar(user.getAvatarUrl());
        detail.setPhone(user.getPhoneNumber());
        detail.setCreateTime(user.getCreateTime());
        detail.setAppointmentCount(appointments.getTotal());
        detail.setAppointments(appointments.getRows() == null
            ? List.of()
            : new ArrayList<>(appointments.getRows()));
        return detail;
    }
}
