package org.dromara.special.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.special.domain.SpecialAppointment;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.bo.SpecialMobileProfileBo;
import org.dromara.special.domain.bo.BindPhoneBody;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialBindPhoneVo;
import org.dromara.special.domain.vo.SpecialMobileProfileVo;
import org.dromara.special.domain.SpecialTeacher;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;
import org.dromara.special.mapper.SpecialAppointmentMapper;
import org.dromara.special.mapper.SpecialTeacherMapper;
import org.dromara.special.service.ISpecialAppointmentService;
import org.dromara.special.service.ISpecialMobileMeService;
import org.dromara.special.util.SpecialBindPhonePlanner;
import org.dromara.special.util.SpecialCurrentRoleStore;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.api.domain.RoleDTO;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.api.model.XcxLoginUser;
import org.dromara.system.domain.SysSocial;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysSocialMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysPermissionService;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 移动端家长中心 Service 实现
 *
 * @author special
 */
@Service
public class SpecialMobileMeServiceImpl implements ISpecialMobileMeService {

    private static final String WECHAT_MINI_SOURCE = "WECHAT_MINI_PROGRAM";

    private final ISysUserService userService;
    private final ISpecialAppointmentService appointmentService;
    private final SpecialAppointmentMapper appointmentMapper;
    private final SysSocialMapper socialMapper;
    private final ISysSocialService socialService;
    private final ISysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;
    private final ISysClientService clientService;
    private final ISysPermissionService permissionService;
    private final SpecialTeacherMapper teacherMapper;
    private final Function<String, String> smsCodeLookup;

    @Autowired
    public SpecialMobileMeServiceImpl(
        ISysUserService userService,
        ISpecialAppointmentService appointmentService,
        SpecialAppointmentMapper appointmentMapper,
        SysSocialMapper socialMapper,
        ISysSocialService socialService,
        ISysRoleService roleService,
        SysUserRoleMapper userRoleMapper,
        ISysClientService clientService,
        ISysPermissionService permissionService,
        SpecialTeacherMapper teacherMapper
    ) {
        this(userService, appointmentService, appointmentMapper, socialMapper, socialService,
            roleService, userRoleMapper, clientService, permissionService, teacherMapper,
            SpecialMobileMeServiceImpl::readAndConsumeSmsCode);
    }

    SpecialMobileMeServiceImpl(
        ISysUserService userService,
        ISpecialAppointmentService appointmentService,
        SpecialAppointmentMapper appointmentMapper,
        SysSocialMapper socialMapper,
        ISysSocialService socialService,
        ISysRoleService roleService,
        SysUserRoleMapper userRoleMapper,
        ISysClientService clientService,
        ISysPermissionService permissionService,
        SpecialTeacherMapper teacherMapper,
        Function<String, String> smsCodeLookup
    ) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.appointmentMapper = appointmentMapper;
        this.socialMapper = socialMapper;
        this.socialService = socialService;
        this.roleService = roleService;
        this.userRoleMapper = userRoleMapper;
        this.clientService = clientService;
        this.permissionService = permissionService;
        this.teacherMapper = teacherMapper;
        this.smsCodeLookup = Objects.requireNonNull(smsCodeLookup);
    }

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
        vo.setPhoneBound(StringUtils.isNotBlank(user.getPhoneNumber()));
        if (StringUtils.isNotBlank(user.getPhoneNumber())) {
            vo.setPhone(DesensitizedUtil.mobilePhone(user.getPhoneNumber()));
        }

        String currentRole = SpecialCurrentRoleStore.read();
        vo.setCurrentRole(currentRole);
        if (loginUser != null && loginUser.getRolePermission() != null) {
            vo.setRoles(new ArrayList<>(loginUser.getRolePermission()));
        }
        RoleDTO role = matchRole(loginUser, currentRole);
        if (role != null) {
            vo.setRoleKey(role.getRoleKey());
            vo.setRoleName(role.getRoleName());
        } else {
            vo.setRoleKey(currentRole);
        }
        return vo;
    }

    @Override
    public SpecialTeacherVo getMyTeacherProfile() {
        requireTeacherIdentity();
        SpecialTeacherVo vo = teacherMapper.selectVoOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, LoginHelper.getUserId()),
            false);
        if (vo == null) {
            throw new ServiceException("老师档案不存在");
        }
        return vo;
    }

    @Override
    public Boolean updateMyTeacherProfile(SpecialTeacherBo bo) {
        requireTeacherIdentity();
        SpecialTeacher existing = teacherMapper.selectOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, LoginHelper.getUserId()));
        if (existing == null) {
            throw new ServiceException("老师档案不存在");
        }
        SpecialTeacher update = new SpecialTeacher();
        update.setId(existing.getId());
        if (bo != null) {
            update.setName(bo.getName());
            update.setTitle(bo.getTitle());
            update.setSpecialties(bo.getSpecialties());
            update.setQualification(bo.getQualification());
            update.setCertImageUrl(bo.getCertImageUrl());
            update.setAvatarUrl(bo.getAvatarUrl());
            update.setIntro(bo.getIntro());
        }
        return teacherMapper.updateById(update) > 0;
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
    public PageResult<SpecialAppointmentVo> listMyAppointments(PageQuery pageQuery, Integer appointStatus) {
        SpecialAppointmentBo bo = new SpecialAppointmentBo();
        if (appointStatus != null) {
            bo.setAppointStatus(appointStatus);
        }
        if (!applyMyAppointmentScope(bo)) {
            return PageResult.build(List.of(), 0);
        }
        return appointmentService.queryPageList(bo, pageQuery);
    }

    @Override
    public SpecialAppointmentVo getMyAppointment(Long id) {
        SpecialAppointmentVo vo = appointmentService.queryById(id);
        if (vo == null) {
            throw new ServiceException("预约不存在");
        }
        if (!canViewMyAppointment(vo)) {
            throw new ServiceException("无权查看该预约");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecialBindPhoneVo bindPhone(BindPhoneBody body) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("用户不存在");
        }
        String phone = body == null ? null : body.getPhone();
        if (!SpecialIdentitySupport.isPhoneLogin(phone)) {
            throw new ServiceException("请输入正确的手机号");
        }
        String expected = smsCodeLookup.apply(phone);
        if (!SpecialIdentitySupport.smsCodeMatches(expected, body.getSmsCode())) {
            throw new ServiceException("验证码无效");
        }
        return DataPermissionHelper.ignore(() -> doBindPhone(currentUserId, phone));
    }

    private SpecialBindPhoneVo doBindPhone(Long currentUserId, String phone) {
        SysUserVo current = userService.selectUserById(currentUserId);
        if (current == null) {
            throw new ServiceException("用户不存在");
        }
        SysUserVo phoneOwner = userService.selectUserByPhoneNumber(phone);
        Long phoneOwnerId = phoneOwner == null ? null : phoneOwner.getUserId();
        boolean openidBoundToOther = isOpenidBoundToOther(currentUserId);
        SpecialBindPhonePlanner.BindPlan plan = SpecialBindPhonePlanner.plan(
            currentUserId, current.getPhoneNumber(), phoneOwnerId, openidBoundToOther);
        if (plan.action() == SpecialBindPhonePlanner.BindAction.REJECT) {
            throw new ServiceException(plan.message());
        }
        if (plan.action() == SpecialBindPhonePlanner.BindAction.WRITE_PHONE) {
            writePhone(currentUserId, phone);
            ensureParent(currentUserId);
            return currentToken();
        }
        if (SystemConstants.DISABLE.equals(phoneOwner.getStatus())) {
            throw new UserException("user.blocked", phoneOwner.getUserName());
        }
        mergeToKeep(plan.keepUserId(), plan.disableUserId());
        return issueKeepToken(plan.keepUserId());
    }

    private void writePhone(Long userId, String phone) {
        SysUserBo user = new SysUserBo();
        user.setUserId(userId);
        user.setPhoneNumber(phone);
        userService.updateUserProfile(user);
    }

    private void mergeToKeep(Long keepUserId, Long disableUserId) {
        List<SysSocialVo> socials = socialService.queryListByUserId(disableUserId);
        if (socials != null) {
            for (SysSocialVo social : socials) {
                SysSocial update = new SysSocial();
                update.setId(social.getId());
                update.setUserId(keepUserId);
                socialMapper.updateById(update);
            }
        }
        List<SpecialAppointment> appointments = appointmentMapper.selectList(
            Wrappers.<SpecialAppointment>lambdaQuery().eq(SpecialAppointment::getUserId, disableUserId));
        if (appointments != null) {
            for (SpecialAppointment appointment : appointments) {
                appointment.setUserId(keepUserId);
                appointmentMapper.updateById(appointment);
            }
        }
        ensureParent(keepUserId);
        userService.updateUserStatus(disableUserId, SystemConstants.DISABLE);
    }

    private void ensureParent(Long userId) {
        if (SpecialParentSupport.isParent(roleService.selectRolesByUserId(userId))) {
            return;
        }
        SysUserRole link = new SysUserRole();
        link.setUserId(userId);
        link.setRoleId(resolveParentRoleId());
        userRoleMapper.insert(link);
    }

    private Long resolveParentRoleId() {
        SysRoleVo role = roleService.selectRoleAll().stream()
            .filter(item -> item != null
                && SpecialIdentitySupport.PARENT_ROLE_KEY.equals(item.getRoleKey())
                && SystemConstants.NORMAL.equals(item.getStatus()))
            .findFirst()
            .orElse(null);
        if (role == null || role.getRoleId() == null) {
            throw new ServiceException("家长角色未初始化，请执行 server/script/sql/ry_special.sql");
        }
        return role.getRoleId();
    }

    private boolean isOpenidBoundToOther(Long currentUserId) {
        String sessionOpenid = sessionWechatOpenid();
        if (StringUtils.isNotBlank(sessionOpenid) && wechatOpenidBoundToOther(sessionOpenid, currentUserId)) {
            return true;
        }
        List<SysSocialVo> mine = socialService.queryListByUserId(currentUserId);
        if (mine == null || mine.isEmpty()) {
            return false;
        }
        for (SysSocialVo social : mine) {
            if (social == null
                || !WECHAT_MINI_SOURCE.equals(social.getSource())
                || StringUtils.isBlank(social.getOpenId())) {
                continue;
            }
            if (wechatOpenidBoundToOther(social.getOpenId(), currentUserId)) {
                return true;
            }
        }
        return false;
    }

    private String sessionWechatOpenid() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser instanceof XcxLoginUser xcx && StringUtils.isNotBlank(xcx.getOpenid())) {
            return xcx.getOpenid();
        }
        return null;
    }

    private boolean wechatOpenidBoundToOther(String openid, Long currentUserId) {
        List<SysSocialVo> bindings = socialService.selectByAuthId(WECHAT_MINI_SOURCE + openid);
        if (bindings == null) {
            return false;
        }
        for (SysSocialVo binding : bindings) {
            if (binding != null && binding.getUserId() != null && !currentUserId.equals(binding.getUserId())) {
                return true;
            }
        }
        return false;
    }

    private SpecialBindPhoneVo currentToken() {
        SpecialBindPhoneVo vo = new SpecialBindPhoneVo();
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(currentClientId());
        return vo;
    }

    private SpecialBindPhoneVo issueKeepToken(Long keepUserId) {
        String clientId = currentClientId();
        SysClientVo client = clientService.queryByClientId(clientId);
        SysUserVo keep = userService.selectUserById(keepUserId);
        if (keep == null) {
            throw new ServiceException("用户不存在");
        }
        if (SystemConstants.DISABLE.equals(keep.getStatus())) {
            throw new UserException("user.blocked", keep.getUserName());
        }
        LoginUser loginUser = buildKeepLoginUser(keep, client, movedOpenid(keepUserId));
        SpecialBindPhoneVo vo = new SpecialBindPhoneVo();
        runAfterCommit(() -> switchToKeepUser(loginUser, client, clientId, vo));
        return vo;
    }

    private void switchToKeepUser(LoginUser loginUser, SysClientVo client, String clientId, SpecialBindPhoneVo vo) {
        String oldToken = StpUtil.getTokenValue();
        LoginHelper.login(loginUser, loginParameter(client, clientId));
        SpecialCurrentRoleStore.write(
            SpecialCurrentRoleStore.requireRoleForLogin(clientId, loginUser.getRolePermission()));
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(clientId);
        revokeReplacedToken(oldToken, vo.getAccessToken());
    }

    private void revokeReplacedToken(String oldToken, String newToken) {
        if (StringUtils.isBlank(oldToken) || oldToken.equals(newToken)) {
            return;
        }
        try {
            StpUtil.logoutByTokenValue(oldToken);
        } catch (NotLoginException ignored) {
            // Old session may already be gone; keep-user token is already issued.
        }
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    private LoginUser buildKeepLoginUser(SysUserVo user, SysClientVo client, String openid) {
        LoginUser loginUser = new LoginUser();
        Long userId = user.getUserId();
        loginUser.setUserId(userId);
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setNickname(user.getNickName());
        loginUser.setUserType(user.getUserType());
        if (client != null) {
            loginUser.setClientKey(client.getClientKey());
            loginUser.setDeviceType(client.getDeviceType());
        }
        if (permissionService != null) {
            loginUser.setMenuPermission(permissionService.getMenuPermission(userId));
            loginUser.setRolePermission(permissionService.getRolePermission(userId));
            List<SysRoleVo> roles = roleService.selectRolesByUserId(userId);
            loginUser.setRoles(BeanUtil.copyToList(roles, RoleDTO.class));
            loginUser.setDataScopeRoleMap(permissionService.getDataScopeRoleMap(loginUser.getRoles()));
        }
        if (StringUtils.isNotBlank(openid)) {
            XcxLoginUser xcx = BeanUtil.toBean(loginUser, XcxLoginUser.class);
            xcx.setOpenid(openid);
            return xcx;
        }
        return loginUser;
    }

    private SaLoginParameter loginParameter(SysClientVo client, String clientId) {
        SaLoginParameter model = new SaLoginParameter();
        if (client != null) {
            model.setDeviceType(client.getDeviceType());
            model.setTimeout(client.getTimeout());
            model.setActiveTimeout(client.getActiveTimeout());
            model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
            model.setExtra(LoginHelper.CLIENT_ACCESS_PATH_KEY, client.getAccessPath());
            model.setExtra(LoginHelper.CLIENT_IP_WHITELIST_KEY, client.getIpWhitelist());
        } else {
            model.setExtra(LoginHelper.CLIENT_KEY, clientId);
        }
        return model;
    }

    private String movedOpenid(Long keepUserId) {
        List<SysSocialVo> socials = socialService.queryListByUserId(keepUserId);
        if (socials == null) {
            return null;
        }
        for (SysSocialVo social : socials) {
            if (social != null && WECHAT_MINI_SOURCE.equals(social.getSource()) && StringUtils.isNotBlank(social.getOpenId())) {
                return social.getOpenId();
            }
        }
        return null;
    }

    private String currentClientId() {
        try {
            Object extra = StpUtil.getExtra(LoginHelper.CLIENT_KEY);
            if (extra != null && StringUtils.isNotBlank(extra.toString())) {
                return extra.toString();
            }
        } catch (Exception ignored) {
            // token extra 在登出后不可用，回退到请求头
        }
        return SpecialIdentitySupport.XCX_CLIENT_ID;
    }

    private void requireTeacherIdentity() {
        if (!SpecialIdentitySupport.TEACHER_ROLE_KEY.equals(SpecialCurrentRoleStore.read())) {
            throw new ServiceException("当前账号没有该身份");
        }
    }

    /**
     * @return false when teacher identity has no archive row (empty list, do not invent)
     */
    private boolean applyMyAppointmentScope(SpecialAppointmentBo bo) {
        if (SpecialIdentitySupport.TEACHER_ROLE_KEY.equals(SpecialCurrentRoleStore.read())) {
            SpecialTeacher teacher = findOwnTeacher();
            if (teacher == null) {
                return false;
            }
            bo.setTeacherId(teacher.getId());
            return true;
        }
        bo.setUserId(LoginHelper.getUserId());
        return true;
    }

    private boolean canViewMyAppointment(SpecialAppointmentVo vo) {
        if (SpecialIdentitySupport.TEACHER_ROLE_KEY.equals(SpecialCurrentRoleStore.read())) {
            SpecialTeacher teacher = findOwnTeacher();
            return teacher != null && teacher.getId() != null && teacher.getId().equals(vo.getTeacherId());
        }
        Long userId = LoginHelper.getUserId();
        return vo.getUserId() != null && userId.equals(vo.getUserId());
    }

    private SpecialTeacher findOwnTeacher() {
        return teacherMapper.selectOne(
            Wrappers.<SpecialTeacher>lambdaQuery().eq(SpecialTeacher::getUserId, LoginHelper.getUserId()));
    }

    private RoleDTO matchRole(LoginUser loginUser, String roleKey) {
        if (loginUser == null || loginUser.getRoles() == null || StringUtils.isBlank(roleKey)) {
            return null;
        }
        for (RoleDTO role : loginUser.getRoles()) {
            if (role != null && roleKey.equals(role.getRoleKey())) {
                return role;
            }
        }
        return null;
    }

    private static String readAndConsumeSmsCode(String phone) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phone;
        String cached = RedisUtils.getCacheObject(key);
        RedisUtils.deleteObject(key);
        return cached;
    }

}
