package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * RGValidator接口用于定义验证操作的规范
 * 它允许实现类针对特定数据类型进行有效性检查
 * @param <T> 要验证的旧值的类型
 */
@SuppressWarnings("unused")
public interface RGValidator<T> {
    /**
     * 验证新值是否有效
     * @param oldValue 旧值，用于比较或参考
     * @param newValue 新值，需要进行验证
     * @return 如果新值有效则返回true，否则返回false
     */
    boolean validate(@NotNull T oldValue, @NotNull String newValue);

    /**
     * 输入值非法的原因
     * @return 输入值非法的原因
     */
    default String reason() {
        return "The input value is illegal!";
    }

    /**
     * StringValidator类实现了RGValidator接口，专门用于字符串类型的验证
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

    /**
     * BooleanValidator类实现了RGValidator接口，专门用于布尔类型的验证
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
     * @param <T> 具体数字类型，如Integer、Float等
     */
    abstract class NumberValidator<T extends Number> implements RGValidator<Integer> {
        /**
         * 获取数字的有效范围
         * @return 包含最小值和最大值的Map.Entry对象
         */
        public abstract @NotNull Map.Entry<T, T> getRange();

        @Override
        public boolean validate(@NotNull Integer oldValue, @NotNull String newValue) {
            try {
                T value = parse(newValue);
                return value.doubleValue() >= getRange().getKey().doubleValue() && value.doubleValue() <= getRange().getValue().doubleValue();
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
         * @param newValue 待解析的字符串
         * @return 解析后的数字
         */
        protected abstract T parse(@NotNull String newValue);
    }

    /**
     * ByteValidator抽象类继承自NumberValidator，用于byte类型的数字验证
     */
    abstract class ByteValidator extends NumberValidator<Byte> {
        protected Byte parse(@NotNull String newValue) {
            return Byte.parseByte(newValue);
        }
    }

    /**
     * ShortValidator抽象类继承自NumberValidator，用于short类型的数字验证
     */
    abstract class ShortValidator extends NumberValidator<Short> {
        protected Short parse(@NotNull String newValue) {
            return Short.parseShort(newValue);
        }
    }

    /**
     * IntegerValidator抽象类继承自NumberValidator，用于int类型的数字验证
     */
    abstract class IntegerValidator extends NumberValidator<Integer> {
        protected Integer parse(@NotNull String newValue) {
            return Integer.parseInt(newValue);
        }
    }

    /**
     * LongValidator抽象类继承自NumberValidator，用于long类型的数字验证
     */
    abstract class LongValidator extends NumberValidator<Long> {
        protected Long parse(@NotNull String newValue) {
            return Long.parseLong(newValue);
        }
    }

    /**
     * FloatValidator抽象类继承自NumberValidator，用于float类型的数字验证
     */
    abstract class FloatValidator extends NumberValidator<Float> {
        protected Float parse(@NotNull String newValue) {
            return Float.parseFloat(newValue);
        }
    }

    /**
     * DoubleValidator抽象类继承自NumberValidator，用于double类型的数字验证
     */
    abstract class DoubleValidator extends NumberValidator<Double> {
        protected Double parse(@NotNull String newValue) {
            return Double.parseDouble(newValue);
        }
    }
}
