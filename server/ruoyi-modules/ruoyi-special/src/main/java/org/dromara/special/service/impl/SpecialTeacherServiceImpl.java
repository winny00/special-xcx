package org.dromara.special.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialTeacherService;
import org.dromara.special.util.SpecialAuditSupport;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 特教老师档案
 */
@RequiredArgsConstructor
@Service
public class SpecialTeacherServiceImpl implements ISpecialTeacherService {

    private final SpecialTeacherMapper baseMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public SpecialTeacherVo queryById(Long id) {
        SpecialTeacherVo vo = baseMapper.selectVoById(id);
        assertOwnsTeacher(vo == null ? null : vo.getUserId());
        fillBoundPhones(vo == null ? List.of() : List.of(vo));
        return vo;
    }

    @Override
    public SpecialTeacherVo queryOwn() {
        Long userId = LoginHelper.getUserId();
        SpecialTeacherVo vo = baseMapper.selectVoOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, userId),
            false);
        if (vo == null) {
            throw new ServiceException("老师档案不存在");
        }
        assertOwnsTeacher(vo.getUserId());
        fillBoundPhones(List.of(vo));
        return vo;
    }

    @Override
    public PageResult<SpecialTeacherVo> queryPageList(SpecialTeacherBo bo, PageQuery pageQuery) {
        applySelfUserId(bo);
        Page<SpecialTeacherVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        fillBoundPhones(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public List<SpecialTeacherVo> queryList(SpecialTeacherBo bo) {
        applySelfUserId(bo);
        List<SpecialTeacherVo> rows = baseMapper.selectVoList(buildQueryWrapper(bo));
        fillBoundPhones(rows);
        return rows;
    }

    @Override
    public PageResult<SpecialTeacherVo> queryApprovedPageList(SpecialTeacherBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialTeacher> lqw = buildQueryWrapper(bo);
        lqw.eq(SpecialTeacher::getStatus, SpecialAuditSupport.APPROVED);
        Page<SpecialTeacherVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        omitLoginPhones(result.getRecords());
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    public SpecialTeacherVo queryApprovedById(Long id) {
        SpecialTeacher teacher = baseMapper.selectById(id);
        if (teacher == null || !Integer.valueOf(SpecialAuditSupport.APPROVED).equals(teacher.getStatus())) {
            throw new ServiceException("老师不存在或未通过审核");
        }
        SpecialTeacherVo vo = baseMapper.selectVoById(id);
        omitLoginPhones(vo == null ? List.of() : List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(SpecialTeacherBo bo) {
        rejectTeacherOnlyMutation();
        SpecialTeacher add = MapstructUtils.convert(bo, SpecialTeacher.class);
        if (add.getStatus() == null) {
            add.setStatus(SpecialAuditSupport.PENDING);
        }
        add.setUserId(bindAccountByPhone(bo, null));
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SpecialTeacherBo bo) {
        SpecialTeacher current = bo.getId() == null ? null : baseMapper.selectById(bo.getId());
        assertOwnsTeacher(current == null ? null : current.getUserId());
        rejectTeacherAuditChange(current, bo);
        SpecialTeacher update = MapstructUtils.convert(bo, SpecialTeacher.class);
        Long previousUserId = current == null ? null : current.getUserId();
        Long boundUserId = bindAccountByPhone(bo, bo.getId(), previousUserId);
        if (boundUserId != null) {
            update.setUserId(boundUserId);
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        rejectTeacherOnlyMutation();
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
        rejectTeacherOnlyMutation();
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

    /**
     * 按手机号合并或创建老师登录账号，写入 user_id。无手机号则不建号。
     */
    Long bindAccountByPhone(SpecialTeacherBo bo, Long currentTeacherId) {
        return bindAccountByPhone(bo, currentTeacherId, null);
    }

    Long bindAccountByPhone(SpecialTeacherBo bo, Long currentTeacherId, Long previousBoundUserId) {
        if (bo == null || StringUtils.isBlank(bo.getPhone())) {
            return null;
        }
        String phone = bo.getPhone().trim();
        if (!SpecialIdentitySupport.isPhoneLogin(phone)) {
            throw new ServiceException("请输入正确的手机号");
        }
        Long userId = DataPermissionHelper.ignore(
            () -> doBindAccount(phone, bo.getInitPassword(), bo.getName(), currentTeacherId));
        revokeTeacherRoleIfUserMoved(previousBoundUserId, userId);
        return userId;
    }

    private Long doBindAccount(String phone, String initPassword, String nickName, Long currentTeacherId) {
        SysUserVo existing = userService.selectUserByPhoneNumber(phone);
        if (existing == null) {
            if (StringUtils.isBlank(initPassword)) {
                throw new ServiceException("请填写初始密码");
            }
            return createTeacherUser(phone, initPassword, nickName);
        }
        Long userId = existing.getUserId();
        SpecialTeacher boundRow = baseMapper.selectOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, userId));
        boolean sameRow = boundRow != null && currentTeacherId != null && currentTeacherId.equals(boundRow.getId());
        boolean alreadyTeacher = hasTeacherRole(userId);
        if ((alreadyTeacher || boundRow != null) && !sameRow) {
            throw new ServiceException("该老师账号已存在");
        }
        if (!alreadyTeacher) {
            SysUserRole link = new SysUserRole();
            link.setUserId(userId);
            link.setRoleId(resolveTeacherRoleId());
            userRoleMapper.insert(link);
        }
        return userId;
    }

    private Long createTeacherUser(String phone, String initPassword, String nickName) {
        SysUserBo userBo = new SysUserBo();
        userBo.setUserName(phone);
        userBo.setNickName(StringUtils.isNotBlank(nickName) ? nickName : phone);
        userBo.setPhoneNumber(phone);
        userBo.setUserType(UserType.SYS_USER.getUserType());
        userBo.setPassword(BCrypt.hashpw(initPassword));
        userBo.setStatus(SystemConstants.NORMAL);
        userBo.setRoleIds(new Long[]{resolveTeacherRoleId()});
        userService.insertUser(userBo);
        return userBo.getUserId();
    }

    private boolean hasTeacherRole(Long userId) {
        return hasRole(userId, SpecialIdentitySupport.TEACHER_ROLE_KEY);
    }

    private boolean hasParentRole(Long userId) {
        return hasRole(userId, SpecialIdentitySupport.PARENT_ROLE_KEY);
    }

    private boolean hasRole(Long userId, String roleKey) {
        List<SysRoleVo> roles = roleService.selectRolesByUserId(userId);
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(role -> role != null && roleKey.equals(role.getRoleKey()));
    }

    /**
     * user_id 换绑后收回旧账号的老师角色，避免无档案的老师登录。
     * 旧账号仍有家长或超管身份时保持启用；仅老师则撤角色并停用。
     */
    private void revokeTeacherRoleIfUserMoved(Long previousUserId, Long newUserId) {
        if (previousUserId == null || newUserId == null || previousUserId.equals(newUserId)) {
            return;
        }
        if (!hasTeacherRole(previousUserId)) {
            return;
        }
        boolean keepEnabled = hasParentRole(previousUserId)
            || hasRole(previousUserId, SpecialIdentitySupport.SUPERADMIN_ROLE_KEY);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getUserId, previousUserId)
            .eq(SysUserRole::getRoleId, resolveTeacherRoleId()));
        if (!keepEnabled) {
            userService.updateUserStatus(previousUserId, SystemConstants.DISABLE);
        }
    }

    private Long resolveTeacherRoleId() {
        SysRoleVo role = roleService.selectRoleAll().stream()
            .filter(item -> item != null
                && SpecialIdentitySupport.TEACHER_ROLE_KEY.equals(item.getRoleKey())
                && SystemConstants.NORMAL.equals(item.getStatus()))
            .findFirst()
            .orElse(null);
        if (role == null || role.getRoleId() == null) {
            throw new ServiceException("老师角色未初始化，请执行 server/script/sql/ry_special.sql");
        }
        return role.getRoleId();
    }

    private void fillBoundPhones(List<SpecialTeacherVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Set<Long> userIds = rows.stream()
            .map(SpecialTeacherVo::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        List<SysUserVo> users = userService.selectUserByIds(userIds, null);
        if (users == null || users.isEmpty()) {
            return;
        }
        Map<Long, String> phones = new HashMap<>();
        for (SysUserVo user : users) {
            if (user != null && user.getUserId() != null) {
                phones.put(user.getUserId(), user.getPhoneNumber());
            }
        }
        for (SpecialTeacherVo row : rows) {
            if (row.getUserId() != null) {
                row.setPhone(SpecialParentSupport.maskPhone(phones.get(row.getUserId())));
            }
        }
    }

    private void omitLoginPhones(List<SpecialTeacherVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (SpecialTeacherVo row : rows) {
            row.setPhone(null);
        }
    }

    private LambdaQueryWrapper<SpecialTeacher> buildQueryWrapper(SpecialTeacherBo bo) {
        LambdaQueryWrapper<SpecialTeacher> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SpecialTeacher::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getSpecialties()), SpecialTeacher::getSpecialties, bo.getSpecialties());
        lqw.eq(bo.getUserId() != null, SpecialTeacher::getUserId, bo.getUserId());
        lqw.eq(bo.getOrgId() != null, SpecialTeacher::getOrgId, bo.getOrgId());
        lqw.eq(bo.getStatus() != null, SpecialTeacher::getStatus, bo.getStatus());
        lqw.orderByDesc(SpecialTeacher::getCreateTime);
        return lqw;
    }

    private void applySelfUserId(SpecialTeacherBo bo) {
        if (SpecialIdentitySupport.currentUserIsTeacherOnly()) {
            bo.setUserId(LoginHelper.getUserId());
        }
    }

    private void assertOwnsTeacher(Long rowUserId) {
        boolean teacherOnly = SpecialIdentitySupport.currentUserIsTeacherOnly();
        boolean owning = rowUserId != null && rowUserId.equals(LoginHelper.getUserId());
        SpecialIdentitySupport.assertTeacherOwns(teacherOnly, owning);
    }

    private void rejectTeacherAuditChange(SpecialTeacher current, SpecialTeacherBo bo) {
        if (!SpecialIdentitySupport.currentUserIsTeacherOnly() || current == null || bo == null) {
            return;
        }
        if (bo.getStatus() != null && !bo.getStatus().equals(current.getStatus())) {
            throw new ServiceException("没有权限访问");
        }
        bo.setStatus(current.getStatus());
        bo.setUserId(current.getUserId());
    }

    private void rejectTeacherOnlyMutation() {
        if (SpecialIdentitySupport.currentUserIsTeacherOnly()) {
            throw new ServiceException("没有权限访问");
        }
    }
}
