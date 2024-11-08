package dev.anvilcraft.rg.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rule注解用于定义配置项的规则和约束条件
 * 它提供了关于配置项如何在不同环境中处理、如何序列化、允许的值、所属类别以及验证器的信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    /**
     * 指定配置项适用的环境，默认为服务器环境
     * 这有助于在不同的运行环境下正确地应用配置规则
     *
     * @return RGEnvironment枚举值，表示配置项适用的环境
     */
    RGEnvironment env() default RGEnvironment.SERVER;

    /**
     * 指定配置项的序列化，默认为空字符串
     * 这允许开发者自定义配置项的序列化名称
     *
     * @return 字符串，表示配置项的序列化名称
     */
    String serialize() default "";

    /**
     * 定义配置项允许的值列表，默认为空数组
     * 这有助于限制配置项的可能值，确保配置的有效性和一致性
     *
     * @return 字符串数组，表示配置项允许的值
     */
    String[] allowed() default {};

    /**
     * 指定配置项所属的类别列表，默认为空数组
     * 这允许对配置项进行分类管理，便于理解和维护
     *
     * @return 字符串数组，表示配置项所属的类别
     */
    String[] categories() default {};

    /**
     * 定义配置项使用的验证器类列表，默认为空数组
     * 验证器用于在运行时验证配置项的值，确保其符合预期的规则和约束
     *
     * @return RGValidator的类数组，表示配置项使用的验证器
     */
    Class<? extends RGValidator>[] validator() default {};
}
