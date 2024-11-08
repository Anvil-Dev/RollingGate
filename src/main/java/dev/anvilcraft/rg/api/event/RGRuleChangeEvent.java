package dev.anvilcraft.rg.api.event;

import dev.anvilcraft.rg.api.RGRule;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 当规则发生变化时触发的事件。
 * 此事件允许服务器和客户端监听和响应区域规则的更改。
 *
 * @param <T> 规则值的类型。
 */
public class RGRuleChangeEvent<T> extends Event implements ICancellableEvent {
    /**
     * 发生变更的规则。
     */
    private final RGRule<T> rule;
    /**
     * 规则的旧值。
     */
    private final T oldValue;
    /**
     * 规则的新值。
     */
    private T newValue;

    /**
     * 构造一个新的规则更改事件。
     *
     * @param rule     发生变更的规则。
     * @param oldValue 规则的旧值。
     * @param newValue 规则的新值。
     */
    public RGRuleChangeEvent(RGRule<T> rule, T oldValue, T newValue) {
        this.rule = rule;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * 获取发生变更的规则。
     *
     * @return 发生变更的规则。
     */
    public RGRule<T> getRule() {
        return rule;
    }

    /**
     * 获取规则的新值。
     *
     * @return 规则的新值。
     */
    public T getNewValue() {
        return newValue;
    }

    /**
     * 获取规则的旧值。
     *
     * @return 规则的旧值。
     */
    public T getOldValue() {
        return oldValue;
    }

    /**
     * 设置规则的新值。
     * 这允许在事件处理过程中更改规则的值。
     *
     * @param newValue 规则的新值。
     */
    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    /**
     * 针对服务器端的规则更改事件。
     * 包含对服务器实例的引用。
     *
     * @param <T> 规则值的类型。
     */
    public static class Server<T> extends RGRuleChangeEvent<T> {
        /**
         * 服务器实例。
         */
        private final MinecraftServer server;

        /**
         * 构造一个新的服务器端规则更改事件。
         *
         * @param rule     发生变更的规则。
         * @param oldValue 规则的旧值。
         * @param newValue 规则的新值。
         * @param server   服务器实例。
         */
        public Server(RGRule<T> rule, T oldValue, T newValue, MinecraftServer server) {
            super(rule, oldValue, newValue);
            this.server = server;
        }

        /**
         * 获取服务器实例。
         *
         * @return 服务器实例。
         */
        public MinecraftServer getServer() {
            return server;
        }
    }

    /**
     * 针对客户端的规则更改事件。
     *
     * @param <T> 规则值的类型。
     */
    public static class Client<T> extends RGRuleChangeEvent<T> {
        /**
         * 构造一个新的客户端规则更改事件。
         *
         * @param rule     发生变更的规则。
         * @param oldValue 规则的旧值。
         * @param newValue 规则的新值。
         */
        public Client(RGRule<T> rule, T oldValue, T newValue) {
            super(rule, oldValue, newValue);
        }
    }
}
