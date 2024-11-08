package dev.anvilcraft.rg.api.client;

import dev.anvilcraft.rg.api.RGEnvironment;
import dev.anvilcraft.rg.api.RGRuleManager;

/**
 * ClientRGRuleManager类是RGRuleManager的子类，专门用于客户端环境
 * 它通过继承RGRuleManager类并指定环境为客户端，来管理客户端特定的规则
 */
public class ClientRGRuleManager extends RGRuleManager {
    /**
     * 初始化ClientRGRuleManager对象，设置命名空间和环境
     *
     * @param namespace 命名空间，用于区分不同的规则管理器实例
     */
    public ClientRGRuleManager(String namespace) {
        super(namespace, RGEnvironment.CLIENT);
    }
}

