package dev.anvilcraft.rg.api;

import com.google.gson.JsonElement;
import dev.anvilcraft.rg.RollingGate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RGRule<T>(String namespace, Class<T> type, RGEnvironment environment, String[] categories,
                        String serialize, String[] allowed,
                        List<RGValidator<T>> validators, T defaultValue, Field field, RGCodec<T> codec) {
    public static final Map<String, RGCodec<?>> CODECS = new HashMap<>() {{
        put("java.lang.Boolean", RGCodec.BOOLEAN);
        put("boolean", RGCodec.BOOLEAN);
        put("java.lang.Byte", RGCodec.BYTE);
        put("byte", RGCodec.BYTE);
        put("java.lang.Short", RGCodec.SHORT);
        put("short", RGCodec.SHORT);
        put("java.lang.Integer", RGCodec.INTEGER);
        put("int", RGCodec.INTEGER);
        put("java.lang.Long", RGCodec.LONG);
        put("long", RGCodec.LONG);
        put("java.lang.Float", RGCodec.FLOAT);
        put("float", RGCodec.FLOAT);
        put("java.lang.Double", RGCodec.DOUBLE);
        put("double", RGCodec.DOUBLE);
        put("java.lang.String", RGCodec.STRING);
    }};

    @SuppressWarnings("unchecked")
    public static <T> @NotNull RGRule<T> of(String namespace, @NotNull Field field) {
        String name = field.getName();
        if (!Modifier.isStatic(field.getModifiers())) throw RGRuleException.notStatic(name);
        if (!Modifier.isPublic(field.getModifiers())) throw RGRuleException.notPublic(name);
        if (Modifier.isFinal(field.getModifiers())) throw RGRuleException.beFinal(name);
        Class<?> type = RGRule.checkType(field);
        Rule rule = field.getAnnotation(Rule.class);
        if (rule == null) throw RGRuleException.notAnnotated(name);
        String serialize = rule.serialize().isEmpty() ? RGRule.caseToSnake(name) : rule.serialize();
        RGRule.checkSerialize(serialize);
        List<RGValidator<T>> validators = new ArrayList<>();
        for (Class<?> validator : rule.validator()) {
            try {
                validators.add((RGValidator<T>) validator.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                RollingGate.LOGGER.error(e.getMessage(), e);
            }
        }
        RGCodec<?> rgCodec = RGRule.CODECS.getOrDefault(type.getTypeName(), null);
        if (rgCodec == null) {
            throw RGRuleException.unsupportedType(name, type);
        } else if (rgCodec.clazz() == Boolean.class) {
            validators.add((RGValidator<T>) new RGValidator.BooleanValidator());
        } else if (rgCodec.clazz() == String.class && validators.isEmpty()) {
            validators.add((RGValidator<T>) new RGValidator.StringValidator());
        }
        try {
            return new RGRule<>(
                namespace,
                (Class<T>) type,
                rule.env(),
                rule.categories(),
                serialize,
                rule.allowed(),
                validators,
                (T) field.get(null),
                field,
                (RGCodec<T>) rgCodec
            );
        } catch (Exception e) {
            throw RGRuleException.createRuleFailed(name);
        }
    }

    public @NotNull String name() {
        return this.field.getName();
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        try {
            return (T) this.field.get(null);
        } catch (IllegalAccessException e) {
            throw RGRuleException.illegalAccess(this.name());
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
        RGCodec<?> rgCodec = CODECS.getOrDefault(field.getType().getTypeName(), null);
        if (rgCodec != null) return rgCodec.clazz();
        throw RGRuleException.unsupportedType(field.getName(), field.getType());
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
    public void setFieldValue(String value) {
        try {
            for (RGValidator<T> validator : this.validators) {
                if (!validator.validate((T) this.field.get(null), value)) {
                    throw new RGRuleException("Illegal value: %s, reason: %s", value, validator.reason());
                }
            }
            this.field.set(null, this.codec.decode(value));
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
    public void setFieldValue(JsonElement primitive) {
        RGCodec<?> rgCodec = CODECS.getOrDefault(this.field.getType().getTypeName(), null);
        if (rgCodec != null) {
            if (primitive.isJsonPrimitive() && primitive.getAsJsonPrimitive().isString()) {
                this.setFieldValue(primitive.getAsString());
            } else this.setFieldValue(primitive.toString());
            return;
        }
        throw new RGRuleException("Field %s has unsupported type %s", this.name(), this.field.getType().getTypeName());
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
}
