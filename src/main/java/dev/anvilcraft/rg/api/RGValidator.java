package dev.anvilcraft.rg.api;

public interface RGValidator<T> {
    boolean validate(T oldValue, T newValue);

    class DefaultValidator implements RGValidator<Object> {
        @Override
        public boolean validate(Object oldValue, Object newValue) {
            return true;
        }
    }
}
