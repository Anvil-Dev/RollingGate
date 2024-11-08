package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("unused")
public interface RGValidator<T> {
    boolean validate(@NotNull T oldValue, @NotNull String newValue);

    default String reason() {
        return "The input value is illegal!";
    }

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

    abstract class NumberValidator<T extends Number> implements RGValidator<Integer> {
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

        protected abstract T parse(@NotNull String newValue);
    }

    abstract class ByteValidator extends NumberValidator<Byte> {
        protected Byte parse(@NotNull String newValue) {
            return Byte.parseByte(newValue);
        }
    }

    abstract class ShortValidator extends NumberValidator<Short> {
        protected Short parse(@NotNull String newValue) {
            return Short.parseShort(newValue);
        }
    }

    abstract class IntegerValidator extends NumberValidator<Integer> {
        protected Integer parse(@NotNull String newValue) {
            return Integer.parseInt(newValue);
        }
    }

    abstract class LongValidator extends NumberValidator<Long> {
        protected Long parse(@NotNull String newValue) {
            return Long.parseLong(newValue);
        }
    }

    abstract class FloatValidator extends NumberValidator<Float> {
        protected Float parse(@NotNull String newValue) {
            return Float.parseFloat(newValue);
        }
    }

    abstract class DoubleValidator extends NumberValidator<Double> {
        protected Double parse(@NotNull String newValue) {
            return Double.parseDouble(newValue);
        }
    }
}
