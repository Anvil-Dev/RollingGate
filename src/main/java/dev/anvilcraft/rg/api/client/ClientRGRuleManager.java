package dev.anvilcraft.rg.api.client;

import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.api.RGEnvironment;
import dev.anvilcraft.rg.api.RGRule;
import dev.anvilcraft.rg.api.RGRuleManager;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import lombok.Getter;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.lang.annotation.ElementType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ClientRGRuleManager类是RGRuleManager的子类，专门用于客户端环境
 * 它通过继承RGRuleManager类并指定环境为客户端，来管理客户端特定的规则
 */
public class ClientRGRuleManager extends RGRuleManager {

    public static final String ANNOTATION_NAME = "L" + RGClientRules.class.getName().replace(".", "/") + ";";

    @Getter
    private final Map<RGRule<?>, ModConfigSpec.ConfigValue<?>> configValueMap = new LinkedHashMap<>();

    @Getter
    private final Map<String, ModConfigSpec> specMap = new LinkedHashMap<>();

    /**
     * 初始化ClientRGRuleManager对象，设置命名空间和环境
     *
     * @param namespace 命名空间，用于区分不同的规则管理器实例
     */
    public ClientRGRuleManager(String namespace) {
        super(namespace, RGEnvironment.CLIENT);
    }

    public void compileContent() throws ClassNotFoundException {
        ProgressMeter meter = StartupNotificationManager.addProgressBar("Load Server Rules", LoadingModList.get().getModFiles().size());
        for (ModFileInfo modFile : LoadingModList.get().getModFiles()) {
            meter.increment();
            @SuppressWarnings("UnstableApiUsage") ModFileScanData scanData = modFile.getFile().getScanResult();
            for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {
                if (annotation.annotationType().getDescriptor().equals(ANNOTATION_NAME) && annotation.targetType() == ElementType.TYPE) {
                    String memberName = annotation.memberName();
                    Class<?> clazz = Class.forName(memberName);
                    String modId = (String) annotation.annotationData().get("value");
                    if (modId == null) modId = RollingGate.MODID;
                    this.register(clazz, modId);
                }
            }
        }
        StartupNotificationManager.popBar(meter);
        this.initModConfigSpecs();
    }

    public <V extends Comparable<? super V>> void initModConfigSpecs() {
        Map<String, ModConfigSpec.Builder> builders = new LinkedHashMap<>();
        for (Map.Entry<String, RGRule<?>> entry : this.rules.entrySet()) {
            String key = entry.getKey();
            RGRule<?> rule = entry.getValue();
            String namespace = rule.namespace();
            ModConfigSpec.Builder builder = builders.getOrDefault(namespace, new ModConfigSpec.Builder());
            ModConfigSpec.ConfigValue<?> spec;
            Class<?> type = rule.codec().clazz();
            if (rule.categories().length >= 1) {
                builder.push(rule.categories()[0]);
            }
            if (type == Boolean.class) {
                spec = builder.define(rule.name(), (boolean) rule.defaultValue());
            } else if (rule.onlyAllowed()) {
                spec = builder.defineInList(key, rule.defaultValue(), rule.getTypedAllowed());
            } else if (Number.class.isAssignableFrom(type)) {
                Object min = RGRule.getRuleMin(rule.min(), rule.codec());
                Object max = RGRule.getRuleMax(rule.max(), rule.codec());
                //noinspection unchecked
                spec = builder.defineInRange(rule.name(), (V) rule.defaultValue(), (V) min, (V) max, (Class<V>) type);
            } else {
                spec = builder.define(key, rule.defaultValue());
            }
            if (rule.categories().length >= 1) {
                builder.pop();
            }
            configValueMap.put(rule, spec);
            builders.put(namespace, builder);
        }
        for (Map.Entry<String, ModConfigSpec.Builder> entry : builders.entrySet()) {
            ModConfigSpec.Builder builder = entry.getValue();
            ModConfigSpec spec = builder.build();
            specMap.put(entry.getKey(), spec);
        }
    }

    @Override
    public void reInit() {
        for (Map.Entry<RGRule<?>, ModConfigSpec.ConfigValue<?>> entry : this.configValueMap.entrySet()) {
            RGRule<?> key = entry.getKey();
            ModConfigSpec.ConfigValue<?> value = entry.getValue();
            key.setFieldValue(value.get().toString());
        }
    }

    @SuppressWarnings(
        {
            "unchecked",
            "unused"
        }
    )
    public <T> void onRuleChange(RGRule<?> rule, Object oldValue, Object newValue) {
        ModConfigSpec.ConfigValue<T> value = (ModConfigSpec.ConfigValue<T>) this.configValueMap.get(rule);
        if (value == null) return;
        T value1 = (T) newValue;
        if (!value.get().equals(value1)) value.set(value1);
    }

    public <T> void loadSuccess() {
        for (RGRule<?> rule : this.configValueMap.keySet()) {
            @SuppressWarnings("unchecked")
            RGRuleChangeEvent<T> event = new RGRuleChangeEvent.Client<>((RGRule<T>) rule, (T) rule.defaultValue(), (T) rule.getValue());
            NeoForge.EVENT_BUS.post(event);
        }
    }
}

