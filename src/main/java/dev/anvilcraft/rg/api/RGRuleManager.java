package dev.anvilcraft.rg.api;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class RGRuleManager {
    public static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
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
