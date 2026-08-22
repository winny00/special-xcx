package org.dromara.special.service.impl;

import org.dromara.special.util.SpecialCurrentRoleStore;
import org.dromara.system.api.SpecialCurrentRoleService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * {@link SpecialCurrentRoleStore} 的 Spring 适配，供 system 模块 getInfo 调用。
 */
@Service
public class SpecialCurrentRoleServiceImpl implements SpecialCurrentRoleService {

    @Override
    public String readOrFill(Set<String> rolePermission) {
        return SpecialCurrentRoleStore.readOrFill(rolePermission);
    }

}
