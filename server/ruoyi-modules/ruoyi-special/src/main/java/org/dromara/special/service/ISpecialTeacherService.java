package org.dromara.special.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.special.domain.bo.SpecialAuditBo;
import org.dromara.special.domain.bo.SpecialTeacherBo;
import org.dromara.special.domain.vo.SpecialTeacherVo;

import java.util.Collection;
import java.util.List;

/**
 * 特教老师档案
 */
public interface ISpecialTeacherService {

    SpecialTeacherVo queryById(Long id);

    PageResult<SpecialTeacherVo> queryPageList(SpecialTeacherBo bo, PageQuery pageQuery);

    List<SpecialTeacherVo> queryList(SpecialTeacherBo bo);

    PageResult<SpecialTeacherVo> queryApprovedPageList(SpecialTeacherBo bo, PageQuery pageQuery);

    SpecialTeacherVo queryApprovedById(Long id);

    Boolean insertByBo(SpecialTeacherBo bo);

    Boolean updateByBo(SpecialTeacherBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    Boolean audit(SpecialAuditBo bo);
}
