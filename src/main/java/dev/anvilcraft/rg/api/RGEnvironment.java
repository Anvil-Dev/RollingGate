package dev.anvilcraft.rg.api;

/**
 * 枚举 RGEnvironment 表示模块可以运行的不同环境
 * 它包括两种类型：客户端和服务器。此枚举提供了确定当前环境的方法
 */
public enum RGEnvironment {
    /**
     * 表示客户端环境
     */
    CLIENT,
    /**
     * 表示服务器端环境
     */
    SERVER;

    /**
     * 检查当前环境是否为客户端.
     *
     * @return 如果当前环境是客户端，则为true，否则为false
     */
    public boolean isClient() {
        return this == CLIENT;
    }

    /**
     * 检查当前环境是否为服务器端
     *
     * @return 如果当前环境是服务器端，则为true，否则为false
     */
    public boolean isServer() {
        return this == SERVER;
    }
}
