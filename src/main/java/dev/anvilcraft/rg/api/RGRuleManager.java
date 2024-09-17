package dev.anvilcraft.rg.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.util.ConfigUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RGRuleManager {
    private static final Path GlobalConfigPath = FMLPaths.CONFIGDIR.get().resolve("rolling_gate.json");
    private static final LevelResource WorldConfigPath = new LevelResource("rolling_gate.json");
    private final Map<String, RGRule<?>> rules;
    private String namespace = "rolling_gate";

    public RGRuleManager() {
        this.rules = new HashMap<>();
    }

    public void reInit(@NotNull MinecraftServer server) {
        this.setSaveRules(ConfigUtil.getOrCreateContent(GlobalConfigPath));
        Path path = server.getWorldPath(WorldConfigPath);
        this.setSaveRules(ConfigUtil.getOrCreateContent(path));
    }

    private void setSaveRules(JsonObject config) {
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            rule.setFieldValue(entry.getValue());
        }
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
