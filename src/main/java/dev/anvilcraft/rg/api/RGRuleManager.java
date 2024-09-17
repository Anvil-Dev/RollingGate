package dev.anvilcraft.rg.api;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RGRuleManager {
    private final Map<String, RGRule<?>> rules;
    private String namespace = "rolling_gate";

    public RGRuleManager() {
        this.rules = new HashMap<>();
    }

    public void reInit(@NotNull JsonObject config) {
        config.entrySet().forEach((entry) -> {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule != null) rule.setFieldValue(entry.getValue());
        });
    }

    public void register(Class<?> rules) {
        RGRuleManager.of(this.namespace, rules).forEach(rule -> this.rules.put(rule.serialize(), rule));
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
        List<RGRule<?>> ruleList = new ArrayList<>();
        for (Field field : rules.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (Modifier.isFinal(field.getModifiers())) continue;
            RGRule.checkType(field);
            ruleList.add(RGRule.of(namespace, field));
        }
        return ruleList;
    }
}
