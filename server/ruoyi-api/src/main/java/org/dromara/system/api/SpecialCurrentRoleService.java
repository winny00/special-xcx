package org.dromara.system.api;

import java.util.Set;

/**
 * 特教当前身份会话。由 ruoyi-special 实现，避免 system 模块循环依赖 special。
 */
public interface SpecialCurrentRoleService {

    /**
     * 读取会话 currentRole；为空则按身份规则填回会话。
     *
     * @param rolePermission 当前用户角色键
     * @return 当前身份，超管无家长/老师时可为 null
     */
    String readOrFill(Set<String> rolePermission);

}
