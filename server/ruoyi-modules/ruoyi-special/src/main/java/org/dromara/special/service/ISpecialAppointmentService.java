package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAppointmentBo;
import org.dromara.special.domain.vo.SpecialAppointmentVo;

import java.util.Collection;
import java.util.List;

/**
 * 特教预约Service接口
 *
 * @author special
 */
public interface ISpecialAppointmentService {

    /**
     * 查询特教预约详情
     *
     * @param id 主键
     * @return 特教预约视图对象
     */
    SpecialAppointmentVo queryById(Long id);

    /**
     * 分页查询特教预约列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<SpecialAppointmentVo> queryPageList(SpecialAppointmentBo bo, PageQuery pageQuery);

    /**
     * 查询特教预约列表
     *
     * @param bo 查询条件
     * @return 结果列表
     */
    List<SpecialAppointmentVo> queryList(SpecialAppointmentBo bo);

    /**
     * 新增特教预约
     *
     * @param bo 业务对象
     * @return 是否新增成功
     */
    Boolean insertByBo(SpecialAppointmentBo bo);

    /**
     * 移动端创建预约（须登录且已绑定手机号）
     *
     * @param bo 业务对象
     * @return 是否新增成功
     */
    Boolean createMobileAppointment(SpecialAppointmentBo bo);

    /**
     * 修改特教预约
     *
     * @param bo 业务对象
     * @return 是否修改成功
     */
    Boolean updateByBo(SpecialAppointmentBo bo);

    /**
     * 校验并删除数据
     *
     * @param ids     主键集合
     * @param isValid 是否校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
