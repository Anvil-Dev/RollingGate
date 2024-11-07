package dev.anvilcraft.rg.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.anvilcraft.rg.RollingGate;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RGRuleManager {
    public static final Gson GSON;
    protected final RGEnvironment environment;
    protected final Map<String, RGRule<?>> rules = new HashMap<>();
    protected final String managerNamespace;
    protected final Path globalConfigPath;
    protected final Map<RGRule<?>, Object> globalConfig = new HashMap<>();
    protected String namespace = "rolling_gate";
    protected final List<String> categories = new ArrayList<>();

    static {
        GsonBuilder builder = new GsonBuilder();
        for (Map.Entry<String, RGCodec<?>> entry : RGRule.CODECS.entrySet()) {
            RGCodec<?> codec = entry.getValue();
            if (codec.isLock()) continue;
            builder.registerTypeAdapter(codec.clazz(), codec);
        }
        GSON = builder.setPrettyPrinting().create();
    }

    public RGRuleManager(String namespace, @NotNull RGEnvironment environment) {
        this.managerNamespace = namespace;
        this.environment = environment;
        this.globalConfigPath = FMLPaths.CONFIGDIR.get().resolve("%s%s.json".formatted(namespace, this.environment.isClient() ? "_client" : ""));
    }

    protected @NotNull Map<RGRule<?>, Object> setSaveRules(@NotNull JsonObject config) {
        Map<RGRule<?>, Object> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            rule.setFieldValue(entry.getValue());
            result.put(rule, rule.getValue());
        }
        return result;
    }

    protected @NotNull Map<String, Object> getSerializedConfig(@NotNull Map<RGRule<?>, Object> configs) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<RGRule<?>, Object> entry : configs.entrySet()) {
            RGRule<?> key = entry.getKey();
            result.put(key.serialize(), entry.getValue());
        }
        return result;
    }

    public void reInit() {
        this.globalConfig.clear();
        this.globalConfig.putAll(this.setSaveRules(ConfigUtil.getOrCreateContent(globalConfigPath)));
    }

    public void addRule(@NotNull RGRule<?> rule) {
        this.rules.put(rule.serialize(), rule);
        this.categories.addAll(Arrays.asList(rule.categories()));
    }

    private static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
        List<RGRule<?>> ruleList = new ArrayList<>();
        for (Field field : rules.getDeclaredFields()) {
            RGRule.checkType(field);
            ruleList.add(RGRule.of(namespace, field));
        }
        return ruleList;
    }

    public void register(Class<?> rules) {
        RGRuleManager.of(this.namespace, rules).forEach(this::addRule);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 获取分组翻译键
     *
     * @return 返回格式化的分组翻译键字符串
     */
    public @NotNull String getDescriptionCategoryKey(String category) {
        // 使用String.format方法构建描述翻译键，包含命名空间和序列化值
        return "rolling_gate.category.%s".formatted(category);
    }
}
