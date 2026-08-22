package org.dromara.special.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAccountBo;
import org.dromara.special.domain.bo.SpecialAccountRolesBody;
import org.dromara.special.domain.vo.SpecialAccountRoleResult;
import org.dromara.special.domain.vo.SpecialAccountVo;
import org.dromara.special.mapper.SpecialAccountMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialAccountService;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台开关家长 / 老师角色、重置密码。
 */
@RequiredArgsConstructor
@Service
public class SpecialAccountServiceImpl implements ISpecialAccountService {

    private final SpecialAccountMapper accountMapper;
    private final SpecialTeacherMapper teacherMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public PageResult<SpecialAccountVo> queryPageList(SpecialAccountBo bo, PageQuery pageQuery) {
        String keyword = bo == null ? null : bo.getKeyword();
        Page<SpecialAccountVo> page = accountMapper.selectAccountPage(pageQuery.build(), keyword);
        if (page.getRecords() != null) {
            for (SpecialAccountVo row : page.getRecords()) {
                row.setPhone(SpecialParentSupport.maskPhone(row.getPhone()));
                Long userId = parseUserId(row.getUserId());
                if (userId != null) {
                    row.setRoles(specialRoleKeys(roleService.selectRolesByUserId(userId)));
                } else {
                    row.setRoles(List.of());
                }
            }
        }
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecialAccountRoleResult updateRoles(Long userId, SpecialAccountRolesBody body) {
        boolean wantParent = Boolean.TRUE.equals(body == null ? null : body.getParent());
        boolean wantTeacher = Boolean.TRUE.equals(body == null ? null : body.getTeacher());
        try {
            SpecialIdentitySupport.assertKeepAtLeastOneRole(wantParent, wantTeacher);
        } catch (IllegalArgumentException ex) {
            throw new ServiceException(ex.getMessage());
        }
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (wantTeacher) {
            SpecialTeacher profile = teacherMapper.selectOne(
                Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, userId));
            if (profile == null) {
                return SpecialAccountRoleResult.needProfile(user.getPhoneNumber());
            }
        }
        Set<String> owned = roleService.selectRolesByUserId(userId).stream()
            .filter(role -> role != null && StringUtils.isNotBlank(role.getRoleKey()))
            .map(SysRoleVo::getRoleKey)
            .collect(Collectors.toSet());
        syncRole(userId, SpecialIdentitySupport.PARENT_ROLE_KEY, wantParent,
            owned.contains(SpecialIdentitySupport.PARENT_ROLE_KEY));
        syncRole(userId, SpecialIdentitySupport.TEACHER_ROLE_KEY, wantTeacher,
            owned.contains(SpecialIdentitySupport.TEACHER_ROLE_KEY));
        return SpecialAccountRoleResult.ok();
    }

    @Override
    public void resetPassword(Long userId, String password) {
        if (StringUtils.isBlank(password)) {
            throw new ServiceException("请填写密码");
        }
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        userService.resetUserPwd(userId, BCrypt.hashpw(password));
    }

    private void syncRole(Long userId, String roleKey, boolean want, boolean has) {
        if (want == has) {
            return;
        }
        Long roleId = resolveRoleId(roleKey);
        if (want) {
            SysUserRole link = new SysUserRole();
            link.setUserId(userId);
            link.setRoleId(roleId);
            userRoleMapper.insert(link);
            return;
        }
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getUserId, userId)
            .eq(SysUserRole::getRoleId, roleId));
    }

    private Long resolveRoleId(String roleKey) {
        SysRoleVo role = roleService.selectRoleAll().stream()
            .filter(item -> item != null
                && roleKey.equals(item.getRoleKey())
                && SystemConstants.NORMAL.equals(item.getStatus()))
            .findFirst()
            .orElse(null);
        if (role == null || role.getRoleId() == null) {
            throw new ServiceException("角色未初始化，请执行 server/script/sql/ry_special.sql");
        }
        return role.getRoleId();
    }

    private static List<String> specialRoleKeys(List<SysRoleVo> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (SysRoleVo role : roles) {
            if (role == null) {
                continue;
            }
            String key = role.getRoleKey();
            if (SpecialIdentitySupport.PARENT_ROLE_KEY.equals(key)
                || SpecialIdentitySupport.TEACHER_ROLE_KEY.equals(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static Long parseUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
