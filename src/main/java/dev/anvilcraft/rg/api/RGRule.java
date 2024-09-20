package dev.anvilcraft.rg.api;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public record RGRule<T>(String namespace, Class<T> type, RGEnvironment environment, String[] categories,
                        String serialize, String[] allowed,
                        RGValidator<T> validator, T defaultValue, Field field) {
    @SuppressWarnings("unchecked")
    public static <T> @NotNull RGRule<T> of(String namespace, @NotNull Field field) {
        if (!Modifier.isStatic(field.getModifiers())) throw RGRuleException.notStatic(field.getName());
        if (!Modifier.isPublic(field.getModifiers())) throw RGRuleException.notPublic(field.getName());
        if (Modifier.isFinal(field.getModifiers())) throw RGRuleException.beFinal(field.getName());
        Class<?> type = RGRule.checkType(field);
        Rule rule = field.getAnnotation(Rule.class);
        if (rule == null) throw RGRuleException.notAnnotated(field.getName());
        String serialize = rule.serialize().isEmpty() ? RGRule.caseToSnake(field.getName()) : rule.serialize();
        RGRule.checkSerialize(serialize);
        try {
            return new RGRule<>(
                namespace,
                (Class<T>) type,
                rule.env(),
                rule.categories(),
                serialize,
                rule.allowed(),
                rule.validator().getDeclaredConstructor().newInstance(),
                (T) field.get(null),
                field
            );
        } catch (Exception e) {
            throw RGRuleException.createRuleFailed(field.getName());
        }
    }

    /**
     * 检查并转换字段的类型
     * <p>
     * 本函数的目的是根据提供的字段对象，判断其类型，并返回相应的包装类如果字段的类型是基本数据类型，
     * 则返回对应的包装类（如boolean类型返回Boolean类）如果字段的类型已经是对应的包装类，则直接返回该类
     * 对于不支持的类型，本函数会抛出RuntimeException异常目前支持的类型包括Boolean, Byte, Short, Integer,
     * Long, Float, Double和String
     *
     * @param field 要检查的字段对象，不能为空
     * @return 与字段类型对应的包装类，或者在不支持该类型时抛出RuntimeException异常
     * @throws RuntimeException 当字段的类型不被支持时抛出
     */
    public static Class<?> checkType(@NotNull Field field) {
        return switch (field.getType().getTypeName()) {
            case "boolean", "java.lang.Boolean" -> Boolean.class;
            case "byte", "java.lang.Byte" -> Byte.class;
            case "short", "java.lang.Short" -> Short.class;
            case "int", "java.lang.Integer" -> Integer.class;
            case "long", "java.lang.Long" -> Long.class;
            case "float", "java.lang.Float" -> Float.class;
            case "double", "java.lang.Double" -> Double.class;
            case "java.lang.String" -> String.class;
            default -> throw RGRuleException.unsupportedType(field.getName(), field.getType());
        };
    }


    /**
     * 检查序列化字符串的有效性
     * <p>
     * 此方法用于确保给定的字符串符合特定的格式标准，以保证其可作为序列化字符串安全使用
     * 字符串必须是非空的，并且以小写字母开头，后面可以包含小写字母、数字和下划线
     *
     * @param str 待检查的序列化字符串
     * @throws RuntimeException 如果字符串为空或不符合指定的格式，抛出此异常
     */
    public static void checkSerialize(@NotNull String str) {
        // 检查字符串是否为空或不符合预期的格式
        if (str.isEmpty() || !str.matches("^[a-z][a-z0-9_]*$")) {
            // 如果检查失败，抛出异常，说明字符串无效
            throw new RuntimeException("Invalid serialize string %s".formatted(str));
        }
    }

    /**
     * 将驼峰命名法的字符串转换为蛇形命名法的字符串
     * <p>
     * 此方法通过识别驼峰命名法中每个单词的首字母大写，并在其前添加下划线，然后将所有字符转换为小写来实现转换
     *
     * @param str 驼峰命名法的字符串
     * @return 转换后的蛇形命名法的字符串
     */
    public static @NotNull String caseToSnake(@NotNull String str) {
        // 使用正则表达式匹配驼峰命名法中的大写字母，并在其前添加下划线，然后将整个字符串转换为小写
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * 设置字段的值
     *
     * @param value 要设置的字段值
     * @throws RGRuleException 当值无法被设置时抛出异常
     */
    @SuppressWarnings("unchecked")
    public void setFieldValue(T value) {
        try {
            if (this.validator.validate((T) this.field.get(null), value)) {
                this.field.set(null, value);
            }
        } catch (IllegalAccessException e) {
            throw new RGRuleException("Illegal value: %s", value);
        }
    }

    /**
     * 设置字段的值
     *
     * @param primitive 要设置的字段json值
     * @throws RGRuleException 当值无法被设置时抛出异常
     */
    @SuppressWarnings("unchecked")
    public void setFieldValue(JsonElement primitive) {
        Object value = switch (this.field.getType().getTypeName()) {
            case "boolean", "java.lang.Boolean" -> primitive.getAsBoolean();
            case "byte", "java.lang.Byte" -> primitive.getAsByte();
            case "short", "java.lang.Short" -> primitive.getAsShort();
            case "int", "java.lang.Integer" -> primitive.getAsInt();
            case "long", "java.lang.Long" -> primitive.getAsLong();
            case "float", "java.lang.Float" -> primitive.getAsFloat();
            case "double", "java.lang.Double" -> primitive.getAsDouble();
            case "java.lang.String" -> primitive.getAsString();
            default ->
                throw new RGRuleException("Field %s has unsupported type %s", this.field.getName(), this.field.getType().getTypeName());
        };
        this.setFieldValue((T) value);
    }

    /**
     * 获取规则的名称翻译键
     *
     * @return 返回格式化的名称翻译键字符串，包括命名空间和序列化字段
     */
    public @NotNull String getNameTranslationKey() {
        // 使用formatted方法格式化名称翻译键
        return "%s.rolling_gate.rule.%s".formatted(this.namespace, this.serialize);
    }

    /**
     * 获取规则的描述翻译键
     *
     * @return 返回格式化的描述翻译键字符串，包括命名空间和序列化字段
     */
    public @NotNull String getDescriptionTranslationKey() {
        // 使用String.format方法构建描述翻译键，包含命名空间和序列化值
        return "%s.rolling_gate.rule.%s.desc".formatted(this.namespace, this.serialize);
    }

    @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> getCommandArgumentBuilder() {
        return Commands.argument("value", StringArgumentType.string());
    }
}
