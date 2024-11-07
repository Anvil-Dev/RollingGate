package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    abstract class ValidatorFactory<T> {
        public abstract Class<? extends RGValidator<T>> get();


        private static class ByteValidatorFactory extends ValidatorFactory<Byte> {
            private final byte min;
            private final byte max;

            private ByteValidatorFactory(byte min, byte max) {
                this.min = min;
                this.max = max;
            }

            public Class<? extends RGValidator<Byte>> get() {
                return RangeValidator.class;
            }

            public class RangeValidator implements RGValidator<Byte> {
                @Override
                public boolean validate(@NotNull Byte oldValue, @NotNull String newValue) {
                    try {
                        byte value = Byte.parseByte(newValue);
                        return value >= min && value <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                @Override
                public String reason() {
                    return "The input value must be between " + min + " and " + max + "!";
                }
            }
        }


        private static class ShortValidatorFactory extends ValidatorFactory<Short> {
            private final short min;
            private final short max;

            private ShortValidatorFactory(short min, short max) {
                this.min = min;
                this.max = max;
            }

            public Class<? extends RGValidator<Short>> get() {
                return RangeValidator.class;
            }

            public class RangeValidator implements RGValidator<Short> {
                @Override
                public boolean validate(@NotNull Short oldValue, @NotNull String newValue) {
                    try {
                        short value = Short.parseShort(newValue);
                        return value >= min && value <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                @Override
                public String reason() {
                    return "The input value must be between " + min + " and " + max + "!";
                }
            }
        }

        private static class IntValidatorFactory extends ValidatorFactory<Integer> {
            private final int min;
            private final int max;

            private IntValidatorFactory(int min, int max) {
                this.min = min;
                this.max = max;
            }

            public Class<? extends RGValidator<Integer>> get() {
                return RangeValidator.class;
            }

            public class RangeValidator implements RGValidator<Integer> {
                @Override
                public boolean validate(@NotNull Integer oldValue, @NotNull String newValue) {
                    try {
                        int value = Integer.parseInt(newValue);
                        return value >= min && value <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                @Override
                public String reason() {
                    return "The input value must be between " + min + " and " + max + "!";
                }
            }
        }


        private static class LongValidatorFactory extends ValidatorFactory<Long> {
            private final long min;
            private final long max;

            private LongValidatorFactory(long min, long max) {
                this.min = min;
                this.max = max;
            }

            public Class<? extends RGValidator<Long>> get() {
                return RangeValidator.class;
            }

            public class RangeValidator implements RGValidator<Long> {
                @Override
                public boolean validate(@NotNull Long oldValue, @NotNull String newValue) {
                    try {
                        long value = Long.parseLong(newValue);
                        return value >= min && value <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                @Override
                public String reason() {
                    return "The input value must be between " + min + " and " + max + "!";
                }
            }
        }


        private static class StringValidatorFactory extends ValidatorFactory<String> {
            private final List<String> accept;

            private StringValidatorFactory(String... accept) {
                this.accept = Arrays.asList(accept);
            }

            private StringValidatorFactory(Collection<String> accept) {
                this.accept = new ArrayList<>(accept);
            }

            public Class<? extends RGValidator<String>> get() {
                return RangeValidator.class;
            }

            public class RangeValidator implements RGValidator<String> {
                @Override
                public boolean validate(@NotNull String oldValue, @NotNull String newValue) {
                    if (newValue.isEmpty()) return false;
                    return accept.contains(newValue);
                }

                @Override
                public String reason() {
                    return "The input value must be one of %s!".formatted(accept);
                }
            }
        }

        public static @NotNull Class<? extends RGValidator<Byte>> rangeByte(byte min, byte max) {
            return new ByteValidatorFactory(min, max).get();
        }

        public static @NotNull Class<? extends RGValidator<Short>> rangeShort(short min, short max) {
            return new ShortValidatorFactory(min, max).get();
        }

        public static @NotNull Class<? extends RGValidator<Integer>> rangeInt(int min, int max) {
            return new IntValidatorFactory(min, max).get();
        }

        public static @NotNull Class<? extends RGValidator<Long>> rangeLong(long min, long max) {
            return new LongValidatorFactory(min, max).get();
        }

        public static @NotNull Class<? extends RGValidator<String>> rangeString(String... accept) {
            return new StringValidatorFactory(accept).get();
        }

        public static @NotNull Class<? extends RGValidator<String>> rangeString(Collection<String> accept) {
            return new StringValidatorFactory(accept).get();
        }
    }
}
