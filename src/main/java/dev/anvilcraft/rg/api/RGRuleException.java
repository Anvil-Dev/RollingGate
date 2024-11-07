package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

public class RGRuleException extends RuntimeException {
    public RGRuleException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    public RGRuleException(String msg, Throwable e) {
        super(msg, e);
    }

    public static @NotNull RGRuleException illegalAccess(@NotNull String name) {
        return new RGRuleException("Field %s has illegal access", name);
    }

    public static @NotNull RGRuleException notStatic(@NotNull String name) {
        return new RGRuleException("Field %s is not static", name);
    }

    public static @NotNull RGRuleException notPublic(@NotNull String name) {
        return new RGRuleException("Field %s is not public", name);
    }

    public static @NotNull RGRuleException beFinal(@NotNull String name) {
        return new RGRuleException("Field %s can't be final", name);
    }

    public static @NotNull RGRuleException notAnnotated(@NotNull String name) {
        return new RGRuleException("Field %s is not annotated with @Rule", name);
    }

    public static @NotNull RGRuleException createRuleFailed(@NotNull String name) {
        return new RGRuleException("Failed to create rule for field %s", name);
    }

    public static @NotNull RGRuleException unsupportedType(@NotNull String name, @NotNull Class<?> type) {
        return new RGRuleException("Field %s has unsupported type, this type can only be boolean, byte, int, long, float, double, String, but got %s", name, type.getTypeName());
    }
}
