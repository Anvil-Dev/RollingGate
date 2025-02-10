package dev.anvilcraft.rg.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.anvilcraft.rg.RollingGate;
import lombok.Setter;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RollingGate规则管理器，负责处理和存储规则配置
 */
public class RGRuleManager {
    // Gson实例，用于JSON序列化和反序列化
    public static final Gson GSON;
    // 环境对象，用于确定当前环境（客户端或服务器）
    protected final RGEnvironment environment;
    // 存储规则的映射表
    protected final Map<String, RGRule<?>> rules = new HashMap<>();
    // 管理器的命名空间
    protected final String managerNamespace;
    // 全局配置文件路径
    protected final Path globalConfigPath;
    // 全局配置映射表
    protected final Map<RGRule<?>, Object> globalConfig = new HashMap<>();
    // 默认命名空间
    @Setter
    protected String namespace = "rolling_gate";
    // 存储规则类别的列表
    protected final Set<String> categories = new HashSet<>();

    // 静态代码块，初始化Gson实例
    static {
        GsonBuilder builder = new GsonBuilder();
        // 注册自定义的Gson类型适配器
        for (Map.Entry<String, RGCodec<?>> entry : RGRule.CODECS.entrySet()) {
            RGCodec<?> codec = entry.getValue();
            if (codec.isBuiltIn()) continue;
            builder.registerTypeAdapter(codec.clazz(), codec);
        }
        GSON = builder.setPrettyPrinting().create();
    }

    /**
     * 构造函数，初始化规则管理器
     *
     * @param namespace 命名空间
     * @param environment 环境对象
     */
    public RGRuleManager(String namespace, @NotNull RGEnvironment environment) {
        this.managerNamespace = namespace;
        this.environment = environment;
        // 根据命名空间和环境确定全局配置文件路径
        this.globalConfigPath = FMLPaths.CONFIGDIR.get().resolve("%s%s.json".formatted(namespace, this.environment.isClient() ? "_client" : ""));
    }

    /**
     * 从配置文件中设置规则，并返回设置的结果
     *
     * @param config 配置文件内容
     * @return 返回设置后的规则映射表
     */
    protected @NotNull Map<RGRule<?>, Object> setSaveRules(@NotNull JsonObject config) {
        Map<RGRule<?>, Object> result = new HashMap<>();
        // 遍历配置文件中的每个规则
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            // 设置规则的字段值
            rule.setFieldValue(entry.getValue());
            result.put(rule, rule.getValue());
        }
        return result;
    }

    /**
     * 序列化配置映射表
     *
     * @param configs 配置映射表
     * @return 返回序列化后的配置映射表
     */
    protected @NotNull Map<String, Object> getSerializedConfig(@NotNull Map<RGRule<?>, Object> configs) {
        Map<String, Object> result = new HashMap<>();
        // 遍历配置映射表，序列化每个规则
        for (Map.Entry<RGRule<?>, Object> entry : configs.entrySet()) {
            RGRule<?> key = entry.getKey();
            result.put(key.serialize(), entry.getValue());
        }
        return result;
    }

    /**
     * 重新初始化全局配置
     */
    public void reInit() {
        this.globalConfig.clear();
        // 从配置文件中重新加载并设置规则
        this.globalConfig.putAll(this.setSaveRules(ConfigUtil.getOrCreateContent(globalConfigPath)));
    }

    /**
     * 添加规则到管理器
     *
     * @param rule 要添加的规则
     */
    public void addRule(@NotNull RGRule<?> rule) {
        this.rules.put(rule.serialize(), rule);
        // 添加规则的类别到类别列表
        this.categories.addAll(Arrays.asList(rule.categories()));
    }

    /**
     * 根据命名空间和规则类创建规则列表
     *
     * @param namespace 命名空间
     * @param rules 规则类
     * @return 返回创建的规则列表
     */
    private static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
        List<RGRule<?>> ruleList = new ArrayList<>();
        // 遍历规则类的每个字段
        for (Field field : rules.getDeclaredFields()) {
            RGRule.checkType(field);
            ruleList.add(RGRule.of(namespace, field));
        }
        return ruleList;
    }

    /**
     * 注册规则类
     *
     * @param rules 规则类
     */
    public void register(Class<?> rules) {
        // 创建并添加规则到管理器
        RGRuleManager.of(this.namespace, rules).forEach(this::addRule);
    }

    /**
     * 获取分组翻译键
     *
     * @param category 分组名称
     * @return 返回格式化的分组翻译键字符串
     */
    public @NotNull String getDescriptionCategoryKey(String category) {
        // 使用String.format方法构建描述翻译键，包含命名空间和序列化值
        return "rolling_gate.category.%s".formatted(category);
    }
}
