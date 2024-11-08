package dev.anvilcraft.rg.api.event;

import dev.anvilcraft.rg.api.RGRule;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class RGRuleChangeEvent<T> extends Event implements ICancellableEvent {
    private final RGRule<T> rule;
    private final T oldValue;
    private T newValue;

    public RGRuleChangeEvent(RGRule<T> rule, T oldValue, T newValue) {
        this.rule = rule;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public RGRule<T> getRule() {
        return rule;
    }

    public T getNewValue() {
        return newValue;
    }

    public T getOldValue() {
        return oldValue;
    }

    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    public static class Server<T> extends RGRuleChangeEvent<T> {
        private final MinecraftServer server;

        public Server(RGRule<T> rule, T oldValue, T newValue, MinecraftServer server) {
            super(rule, oldValue, newValue);
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class Client<T> extends RGRuleChangeEvent<T> {
        public Client(RGRule<T> rule, T oldValue, T newValue) {
            super(rule, oldValue, newValue);
        }
    }
}
