package dev.anvilcraft.rg.api;

public class RGRuleException extends RuntimeException {
    public RGRuleException(String msg, Object... args) {
        super(String.format(msg, args));
    }

    public RGRuleException(String msg, Throwable e) {
        super(msg, e);
    }
}
