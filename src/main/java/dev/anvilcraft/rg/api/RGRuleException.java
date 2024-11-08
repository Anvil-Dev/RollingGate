package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

/**
 * 自定义运行时异常类，用于表示规则引擎中的规则相关错误
 */
public class RGRuleException extends RuntimeException {
    /**
     * 构造函数，用于格式化异常消息
     *
     * @param msg 格式化的消息字符串
     * @param args 消息中的参数
     */
    public RGRuleException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    /**
     * 构造函数，用于处理异常消息和原因
     *
     * @param msg 异常消息
     * @param e 异常原因
     */
    public RGRuleException(String msg, Throwable e) {
        super(msg, e);
    }

    /**
     * 静态工厂方法，用于创建表示非法访问错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException illegalAccess(@NotNull String name) {
        return new RGRuleException("Field %s has illegal access", name);
    }

    /**
     * 静态工厂方法，用于创建表示字段非静态错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException notStatic(@NotNull String name) {
        return new RGRuleException("Field %s is not static", name);
    }

    /**
     * 静态工厂方法，用于创建表示字段非公共错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException notPublic(@NotNull String name) {
        return new RGRuleException("Field %s is not public", name);
    }

    /**
     * 静态工厂方法，用于创建表示字段为final错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException beFinal(@NotNull String name) {
        return new RGRuleException("Field %s can't be final", name);
    }

    /**
     * 静态工厂方法，用于创建表示字段未标注@Rule错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException notAnnotated(@NotNull String name) {
        return new RGRuleException("Field %s is not annotated with @Rule", name);
    }

    /**
     * 静态工厂方法，用于创建表示创建规则失败错误的异常
     *
     * @param name 字段名
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException createRuleFailed(@NotNull String name) {
        return new RGRuleException("Failed to create rule for field %s", name);
    }

    /**
     * 静态工厂方法，用于创建表示字段类型不受支持错误的异常
     *
     * @param name 字段名
     * @param type 字段类型
     * @return 创建的异常实例
     */
    public static @NotNull RGRuleException unsupportedType(@NotNull String name, @NotNull Class<?> type) {
        return new RGRuleException("Field %s has unsupported type, this type can only be boolean, byte, int, long, float, double, String, but got %s", name, type.getTypeName());
    }
}
