package dev.anvilcraft.rg.api;

import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import net.neoforged.fml.IExtensionPoint;

/**
 * RGAdditional接口扩展了IExtensionPoint接口，旨在提供一个扩展点，
 * 用于在服务器和客户端加载特定规则。这个接口允许实现类
 * 在需要时自定义规则加载逻辑
 */
public interface RGAdditional extends IExtensionPoint {

    /**
     * 为服务器端加载规则提供了一个默认方法。这个方法在服务器规则管理器中
     * 提供了一个接入点，实现类可以覆盖此方法以加载自定义的服务器规则
     *
     * @param manager 服务器规则管理器，用于操作和管理服务器规则
     */
    default void loadServerRules(ServerRGRuleManager manager) {
    }

    /**
     * 为客户端加载规则提供了一个默认方法。这个方法在客户端规则管理器中
     * 提供了一个接入点，实现类可以覆盖此方法以加载自定义的客户端规则
     *
     * @param manager 客户端规则管理器，用于操作和管理客户端规则
     */
    default void loadClientRules(ClientRGRuleManager manager) {
    }
}

