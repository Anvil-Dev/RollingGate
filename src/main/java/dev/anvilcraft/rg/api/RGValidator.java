package dev.anvilcraft.rg.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * RGValidator 接口用于定义验证操作的规范
 * 它允许实现类针对特定数据类型进行有效性检查
 *
 * @param <T> 要验证的旧值的类型
 */
@SuppressWarnings("unused")
public interface RGValidator<T> {
    /**
     * 验证新值是否有效
     *
     * @param oldValue 旧值，用于比较或参考
     * @param newValue 新值，需要进行验证
     * @return 如果新值有效则返回true，否则返回false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean validate(@NotNull T oldValue, @NotNull String newValue);

    /**
     * 输入值非法的原因
     *
     * @return 输入值非法的原因
     */
    default String reason() {
        return "The input value is illegal!";
    }

    /**
     * StringValidator 类实现了 RGValidator 接口，专门用于字符串类型的验证
     */
    class StringValidator implements RGValidator<String> {
        @Override
        public boolean validate(@NotNull String oldValue, @NotNull String newValue) {
            return !newValue.isEmpty();
        }

        @Override
        public String reason() {
            return "The input value must not be empty!";
        }
    }

    class StringInSetValidator extends StringValidator {
        /**
         * 获取包含在范围内的字符串集合
         *
         * @return 包含在范围内的字符串集合
         */
        public Set<String> getSet() {
            return Set.of();
        }

        @Override
        public boolean validate(@NotNull String oldValue, @NotNull String newValue) {
            return super.validate(oldValue, newValue) && getSet().contains(newValue);
        }

        @Override
        public String reason() {
            return "The input value must be in the set: %s!".formatted(getSet().toString());
        }
    }

    /**
     * BooleanValidator 类实现了 RGValidator 接口，专门用于布尔类型的验证
     */
    class BooleanValidator implements RGValidator<Boolean> {
        @Override
        public boolean validate(@NotNull Boolean oldValue, @NotNull String newValue) {
            return newValue.equals("true") || newValue.equals("false");
        }

        @Override
        public String reason() {
            return "The input value must be true or false!";
        }
    }

    /**
     * NumberValidator抽象类实现了RGValidator接口，为数字类型提供了一个通用的验证框架
     * 它要求子类提供具体的数字解析和范围获取实现
     *
     * @param <T> 具体数字类型，如 Integer、Float 等
     */
    abstract class NumberValidator<T extends Number> implements RGValidator<T> {
        /**
         * 获取数字的有效范围
         *
         * @return 包含最小值和最大值的Map.Entry对象
         */
        public abstract @NotNull Map.Entry<T, T> getRange();

        /**
         * 获取数字是否包含在范围内
         *
         * @return 包含范围信息的Map.Entry对象，第一个元素表示最小值是否包含在内，第二个元素表示最大值是否包含在内
         */
        public Map.Entry<Boolean, Boolean> containsRange() {
            return Map.entry(true, true);
        }

        @Override
        public boolean validate(@NotNull T oldValue, @NotNull String newValue) {
            try {
                T value = parse(newValue);
                boolean flag1 = containsRange().getKey() ? value.doubleValue() >= getRange().getKey().doubleValue() : value.doubleValue() > getRange().getKey().doubleValue();
                boolean flag2 = containsRange().getValue() ? value.doubleValue() <= getRange().getValue().doubleValue() : value.doubleValue() < getRange().getValue().doubleValue();
                return flag1 && flag2;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public String reason() {
            return "The input value must be between " + getRange().getKey().toString() + " and " + getRange().getValue().toString() + "!";
        }

        /**
         * 解析字符串为指定的数字类型
         *
         * @param newValue 待解析的字符串
         * @return 解析后的数字
         */
        protected abstract T parse(@NotNull String newValue);
    }

    /**
     * ByteValidator 抽象类继承自 NumberValidator ，用于 byte 类型的数字验证
     */
    abstract class ByteValidator extends NumberValidator<Byte> {
        protected Byte parse(@NotNull String newValue) {
            return Byte.parseByte(newValue);
        }
    }

    /**
     * ShortValidator 抽象类继承自 NumberValidator ，用于 short 类型的数字验证
     */
    abstract class ShortValidator extends NumberValidator<Short> {
        protected Short parse(@NotNull String newValue) {
            return Short.parseShort(newValue);
        }
    }

    /**
     * IntegerValidator 抽象类继承自 NumberValidator ，用于 int 类型的数字验证
     */
    abstract class IntegerValidator extends NumberValidator<Integer> {
        protected Integer parse(@NotNull String newValue) {
            return Integer.parseInt(newValue);
        }
    }

    /**
     * LongValidator 抽象类继承自 NumberValidator ，用于 long 类型的数字验证
     */
    abstract class LongValidator extends NumberValidator<Long> {
        protected Long parse(@NotNull String newValue) {
            return Long.parseLong(newValue);
        }
    }

    /**
     * FloatValidator 抽象类继承自 NumberValidator ，用于 float 类型的数字验证
     */
    abstract class FloatValidator extends NumberValidator<Float> {
        protected Float parse(@NotNull String newValue) {
            return Float.parseFloat(newValue);
        }
    }

    /**
     * DoubleValidator 抽象类继承自 NumberValidator ，用于 double 类型的数字验证
     */
    abstract class DoubleValidator extends NumberValidator<Double> {
        protected Double parse(@NotNull String newValue) {
            return Double.parseDouble(newValue);
        }
    }

    /**
     * CommandRuleValidator类继承自StringInSetValidator，用于验证命令规则的合法性
     * 它通过限定一组特定的字符串来确保命令规则符合预期的权限级别或布尔值
     */
    class CommandRuleValidator extends StringInSetValidator {
        /**
         * 重写getSet方法，返回一组预定义的字符串，这些字符串代表了有效的命令规则
         * 包括操作者权限级别（ops）、布尔值（true/false）和数字表示的权限级别（1-4）
         *
         * @return 一组有效的命令规则字符串
         */
        @Override
        public Set<String> getSet() {
            return Set.of("ops", "true", "false", "1", "2", "3", "4");
        }

        /**
         * 获取权限级别数组，提供了静态方法访问权限级别的字符串数组
         *
         * @return 权限级别字符串数组
         */
        public static String @NotNull [] getPermissionLevels() {
            return new String[]{"ops", "true", "false", "0", "1", "2", "3", "4"};
        }

        /**
         * 检查给定的供应商提供的命令规则是否满足特定的权限级别
         * 此方法通过比较供应商提供的命令规则与预定义的权限级别来确定权限
         *
         * @param supplier 一个供应商对象，用于获取命令规则字符串
         * @param stack    命令源堆栈，用于检查权限级别
         * @return 如果供应商提供的命令规则满足权限级别，则返回true，否则返回false
         */
        public static boolean hasPermission(@NotNull Supplier<String> supplier, @NotNull CommandSourceStack stack) {
            String s = supplier.get();
            return switch (s) {
                case "0" -> stack.hasPermission(Commands.LEVEL_ALL);
                case "1" -> stack.hasPermission(Commands.LEVEL_MODERATORS);
                case "ops", "2" -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS);
                case "3" -> stack.hasPermission(Commands.LEVEL_ADMINS);
                case "4" -> stack.hasPermission(Commands.LEVEL_OWNERS);
                case "true" -> true;
                default -> false;
            };
        }
    }
}
