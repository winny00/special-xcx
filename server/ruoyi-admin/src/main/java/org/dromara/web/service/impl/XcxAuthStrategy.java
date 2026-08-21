package org.dromara.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.helper.DataPermissionHelper;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.api.model.XcxLoginBody;
import org.dromara.system.api.model.XcxLoginUser;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.SysSocial;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysSocialBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysRoleMapper;
import org.dromara.system.mapper.SysSocialMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysUserService;
import org.dromara.web.config.properties.WechatMiniProgramProperties;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小程序认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("xcx" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class XcxAuthStrategy implements IAuthStrategy {

    /**
     * 与 JustAuth 微信小程序 source 保持一致，用于 sys_social 绑定。
     */
    private static final String WECHAT_MINI_SOURCE = "WECHAT_MINI_PROGRAM";

    private static final String PARENT_ROLE_KEY = "special_parent";

    private final SysLoginService loginService;
    private final WechatMiniProgramProperties wechatMiniProgramProperties;
    private final ISysSocialService sysSocialService;
    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysSocialMapper socialMapper;

    /**
     * 执行微信小程序登录，并根据 openid 构建小程序用户登录态。
     *
     * @param body   登录请求体
     * @param client 当前客户端配置
     * @return 登录结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVo login(String body, SysClientVo client) {
        XcxLoginBody loginBody = JsonUtils.parseObject(body, XcxLoginBody.class);
        ValidatorUtils.validate(loginBody);
        String xcxCode = loginBody.getXcxCode();
        String appid = StringUtils.isNotBlank(loginBody.getAppid()) ? loginBody.getAppid() : wechatMiniProgramProperties.getAppId();
        String appSecret = wechatMiniProgramProperties.getAppSecret();
        if (StringUtils.isBlank(appid) || StringUtils.isBlank(appSecret)) {
            throw new ServiceException("请先在 application-dev.yml 配置 special.wechat.app-id 和 app-secret");
        }

        AuthRequest authRequest = new AuthWechatMiniProgramRequest(AuthConfig.builder()
            .clientId(appid).clientSecret(appSecret)
            .ignoreCheckRedirectUri(true).ignoreCheckState(true).build());
        AuthCallback authCallback = new AuthCallback();
        authCallback.setCode(xcxCode);
        AuthResponse<AuthUser> resp = authRequest.login(authCallback);
        String openid;
        String unionId;
        if (resp.ok()) {
            AuthToken token = resp.getData().getToken();
            openid = token.getOpenId();
            unionId = token.getUnionId();
        } else {
            throw new ServiceException(resp.getMsg());
        }

        SysUserVo user = loadUserByOpenid(openid, unionId);
        LoginUser built = DataPermissionHelper.ignore(() -> loginService.buildLoginUser(user));
        XcxLoginUser loginUser = BeanUtil.toBean(built, XcxLoginUser.class);
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        loginUser.setOpenid(openid);

        SaLoginParameter model = IAuthStrategy.buildLoginParameter(client);
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        loginVo.setOpenid(openid);
        return loginVo;
    }

    /**
     * 按 openid 查询绑定用户；未绑定时自动注册家长账号。
     */
    private SysUserVo loadUserByOpenid(String openid, String unionId) {
        SysSocialVo binding = findSocialByOpenid(openid);
        if (binding != null) {
            return loadExistingUser(binding.getUserId(), openid);
        }
        return registerParentByOpenid(openid, unionId);
    }

    private SysSocialVo findSocialByOpenid(String openid) {
        List<SysSocialVo> list = socialMapper.lambda()
            .eq(SysSocial::getOpenId, openid)
            .eq(SysSocial::getSource, WECHAT_MINI_SOURCE)
            .voList();
        if (CollUtil.isNotEmpty(list)) {
            return list.getFirst();
        }
        List<SysSocialVo> byAuthId = sysSocialService.selectByAuthId(buildAuthId(openid));
        return CollUtil.isNotEmpty(byAuthId) ? byAuthId.getFirst() : null;
    }

    private SysUserVo loadExistingUser(Long userId, String openid) {
        SysUserVo user = userMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            log.warn("openid {} 绑定的用户 {} 不存在", openid, userId);
            throw new ServiceException("账号绑定异常，请联系管理员");
        }
        if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", openid);
            throw new UserException("user.blocked", user.getUserName());
        }
        return user;
    }

    private SysUserVo registerParentByOpenid(String openid, String unionId) {
        return DataPermissionHelper.ignore(() -> {
            Long parentRoleId = resolveParentRoleId();
            String suffix = openid.length() > 8 ? openid.substring(openid.length() - 8) : openid;
            String username = "wx_" + suffix;
            while (userMapper.lambda().eq(SysUser::getUserName, username).exists()) {
                username = "wx_" + suffix + "_" + RandomUtil.randomNumbers(4);
            }

            SysUserBo userBo = new SysUserBo();
            userBo.setUserName(username);
            userBo.setNickName("微信用户" + suffix);
            userBo.setUserType(UserType.SYS_USER.getUserType());
            userBo.setPassword(BCrypt.hashpw(RandomUtil.randomString(16)));
            userBo.setStatus(SystemConstants.NORMAL);
            userBo.setRoleIds(new Long[]{parentRoleId});

            userService.insertUser(userBo);

            SysSocialBo socialBo = new SysSocialBo();
            socialBo.setUserId(userBo.getUserId());
            socialBo.setAuthId(buildAuthId(openid));
            socialBo.setSource(WECHAT_MINI_SOURCE);
            socialBo.setAccessToken("xcx");
            socialBo.setOpenId(openid);
            socialBo.setUnionId(unionId);
            socialBo.setUserName(username);
            socialBo.setNickName(userBo.getNickName());
            sysSocialService.insertByBo(socialBo);

            SysUserVo created = userMapper.selectVoById(userBo.getUserId());
            log.info("微信小程序首次登录，已自动注册家长账号：{}", username);
            return created;
        });
    }

    private Long resolveParentRoleId() {
        SysRoleVo role = roleMapper.lambda()
            .eq(SysRole::getRoleKey, PARENT_ROLE_KEY)
            .eq(SysRole::getStatus, SystemConstants.NORMAL)
            .voOne();
        if (role == null || role.getRoleId() == null) {
            throw new ServiceException("家长角色未初始化，请执行 server/script/sql/ry_special.sql");
        }
        return role.getRoleId();
    }

    private static String buildAuthId(String openid) {
        return WECHAT_MINI_SOURCE + openid;
    }

}
