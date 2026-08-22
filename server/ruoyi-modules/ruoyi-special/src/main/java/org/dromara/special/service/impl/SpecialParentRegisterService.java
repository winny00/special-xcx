package org.dromara.special.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.special.service.SpecialWxPhoneLookup;
import org.dromara.special.util.SpecialIdentitySupport;
import org.dromara.special.util.SpecialParentSupport;
import org.dromara.system.api.model.RegisterBody;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.SysUserRoleMapper;
import org.dromara.system.service.ISysRoleService;
import org.dromara.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Function;

/**
 * 小程序家长手机号注册：一人一号，可叠 special_parent。
 */
@Service
public class SpecialParentRegisterService {

    private final SysUserMapper userMapper;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;
    private final Function<String, String> smsCodeLookup;
    private final SpecialWxPhoneLookup wxPhoneLookup;

    @Autowired
    public SpecialParentRegisterService(
        SysUserMapper userMapper,
        ISysUserService userService,
        ISysRoleService roleService,
        SysUserRoleMapper userRoleMapper,
        @Autowired(required = false) SpecialWxPhoneLookup wxPhoneLookup
    ) {
        this(userMapper, userService, roleService, userRoleMapper,
            SpecialParentRegisterService::readAndConsumeSmsCode, wxPhoneLookup);
    }

    SpecialParentRegisterService(
        SysUserMapper userMapper,
        ISysUserService userService,
        ISysRoleService roleService,
        SysUserRoleMapper userRoleMapper,
        Function<String, String> smsCodeLookup,
        SpecialWxPhoneLookup wxPhoneLookup
    ) {
        this.userMapper = Objects.requireNonNull(userMapper);
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleMapper = userRoleMapper;
        this.smsCodeLookup = smsCodeLookup;
        this.wxPhoneLookup = wxPhoneLookup;
    }

    /**
     * 按手机号注册或叠加家长角色。
     *
     * @param body username=手机号，password=密码，code=短信验证码
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterBody body) {
        String phone = resolveRegisterPhone(body);
        DataPermissionHelper.ignore(() -> doRegister(body, phone));
    }

    private String resolveRegisterPhone(RegisterBody body) {
        String wxPhoneCode = body == null ? null : StringUtils.trim(body.getWxPhoneCode());
        if (StringUtils.isNotBlank(wxPhoneCode)) {
            if (wxPhoneLookup == null) {
                throw new ServiceException("微信手机号未配置");
            }
            String phone = wxPhoneLookup.resolvePhone(wxPhoneCode);
            if (!SpecialIdentitySupport.isPhoneLogin(phone)) {
                throw new ServiceException("请输入正确的手机号");
            }
            return phone;
        }
        String phone = body == null ? null : body.getUsername();
        if (!SpecialIdentitySupport.isPhoneLogin(phone)) {
            throw new ServiceException("请输入正确的手机号");
        }
        String expected = smsCodeLookup.apply(phone);
        if (!SpecialIdentitySupport.smsCodeMatches(expected, body.getCode())) {
            throw new ServiceException("验证码无效");
        }
        return phone;
    }

    private void doRegister(RegisterBody body, String phone) {
        SysUserVo existing = userService.selectUserByPhoneNumber(phone);
        if (existing == null) {
            createParent(phone, body.getPassword());
            return;
        }
        if (SpecialParentSupport.isParent(roleService.selectRolesByUserId(existing.getUserId()))) {
            throw new ServiceException("账号已注册，请直接登录");
        }
        SysUserRole link = new SysUserRole();
        link.setUserId(existing.getUserId());
        link.setRoleId(resolveParentRoleId());
        userRoleMapper.insert(link);
        if (StringUtils.isNotBlank(body.getPassword())) {
            userService.resetUserPwd(existing.getUserId(), BCrypt.hashpw(body.getPassword()));
        }
    }

    private void createParent(String phone, String password) {
        SysUserBo userBo = new SysUserBo();
        userBo.setUserName(phone);
        userBo.setNickName(phone);
        userBo.setPhoneNumber(phone);
        userBo.setUserType(UserType.SYS_USER.getUserType());
        userBo.setPassword(BCrypt.hashpw(password));
        userBo.setStatus(SystemConstants.NORMAL);
        userBo.setRoleIds(new Long[]{resolveParentRoleId()});
        userService.insertUser(userBo);
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

    private static String readAndConsumeSmsCode(String phone) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phone;
        String cached = RedisUtils.getCacheObject(key);
        RedisUtils.deleteObject(key);
        return cached;
    }
}
