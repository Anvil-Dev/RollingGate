package dev.anvilcraft.rg.api;

public enum RGEnvironment {
    CLIENT,
    SERVER;

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }
}
