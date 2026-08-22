package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.BindPhoneBody;
import org.dromara.special.domain.bo.SpecialMobileProfileBo;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;
import org.dromara.special.domain.vo.SpecialBindPhoneVo;
import org.dromara.special.domain.vo.SpecialMobileProfileVo;
import org.dromara.special.domain.vo.SpecialTeacherVo;

/**
 * 移动端家长中心 Service
 *
 * @author special
 */
public interface ISpecialMobileMeService {

    /**
     * 获取当前登录用户资料
     */
    SpecialMobileProfileVo getProfile();

    /**
     * 更新当前登录用户资料
     */
    Boolean updateProfile(SpecialMobileProfileBo bo);

    /**
     * 分页查询当前用户预约。老师身份按档案 teacherId；可按 appointStatus 过滤。
     */
    PageResult<SpecialAppointmentVo> listMyAppointments(PageQuery pageQuery, Integer appointStatus);

    /**
     * 查询当前用户预约详情
     */
    SpecialAppointmentVo getMyAppointment(Long id);

    /**
     * 绑定手机号：写号或合并微信临时用户，返回可供前端替换的 token。
     */
    SpecialBindPhoneVo bindPhone(BindPhoneBody body);

    /**
     * 当前登录用户自己的老师档案（按 userId）。
     */
    SpecialTeacherVo getMyTeacherProfile();

    /**
     * 更新自己的老师档案；不可改审核状态。
     */
    Boolean updateMyTeacherProfile(SpecialTeacherBo bo);

}
