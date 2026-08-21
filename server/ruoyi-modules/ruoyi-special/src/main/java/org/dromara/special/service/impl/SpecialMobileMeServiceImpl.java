package org.dromara.special.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.bo.SpecialMobileProfileBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialMobileProfileVo;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.service.ISpecialMobileMeService;
import org.dromara.system.api.domain.RoleDTO;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 移动端家长中心 Service 实现
 *
 * @author special
 */
@RequiredArgsConstructor
@Service
public class SpecialMobileMeServiceImpl implements ISpecialMobileMeService {

    private final ISysUserService userService;
    private final ISpecialAppointmentService appointmentService;

    @Override
    public SpecialMobileProfileVo getProfile() {
        Long userId = LoginHelper.getUserId();
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        LoginUser loginUser = LoginHelper.getLoginUser();

        SpecialMobileProfileVo vo = new SpecialMobileProfileVo();
        vo.setUserId(userId);
        vo.setNickname(StringUtils.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUserName());
        vo.setAvatar(user.getAvatarUrl());
        if (StringUtils.isNotBlank(user.getPhoneNumber())) {
            vo.setPhone(DesensitizedUtil.mobilePhone(user.getPhoneNumber()));
        }

        RoleDTO role = resolvePrimaryRole(loginUser);
        if (role != null) {
            vo.setRoleKey(role.getRoleKey());
            vo.setRoleName(role.getRoleName());
        }
        else if (loginUser != null && loginUser.getRolePermission() != null && !loginUser.getRolePermission().isEmpty()) {
            String roleKey = loginUser.getRolePermission().iterator().next();
            vo.setRoleKey(roleKey);
            vo.setRoleName(roleKey);
        }
        return vo;
    }

    @Override
    public Boolean updateProfile(SpecialMobileProfileBo bo) {
        Long userId = LoginHelper.getUserId();
        SysUserBo user = new SysUserBo();
        user.setUserId(userId);
        if (StringUtils.isNotBlank(bo.getNickname())) {
            user.setNickName(bo.getNickname());
        }
        if (StringUtils.isNotBlank(bo.getPhone())) {
            user.setPhoneNumber(bo.getPhone());
        }
        if (StringUtils.isNotBlank(user.getPhoneNumber()) && !userService.checkPhoneUnique(user)) {
            throw new ServiceException("手机号码已存在");
        }
        return DataPermissionHelper.ignore(() -> userService.updateUserProfile(user)) > 0;
    }

    @Override
    public PageResult<SpecialAppointmentVo> listMyAppointments(PageQuery pageQuery) {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        bo.setUserId(LoginHelper.getUserId());
        return appointmentService.queryPageList(bo, pageQuery);
    }

    @Override
    public SpecialAppointmentVo getMyAppointment(Long id) {
        SpecialAppointmentVo vo = appointmentService.queryById(id);
        if (vo == null) {
            throw new ServiceException("预约不存在");
        }
        Long userId = LoginHelper.getUserId();
        if (vo.getUserId() == null || !userId.equals(vo.getUserId())) {
            throw new ServiceException("无权查看该预约");
        }
        return vo;
    }

    private RoleDTO resolvePrimaryRole(LoginUser loginUser) {
        if (loginUser == null || loginUser.getRoles() == null || loginUser.getRoles().isEmpty()) {
            return null;
        }
        for (RoleDTO role : loginUser.getRoles()) {
            if ("special_parent".equals(role.getRoleKey())) {
                return role;
            }
        }
        return loginUser.getRoles().getFirst();
    }

}
