package dev.anvilcraft.rg.api.server;

import com.google.gson.Gson;
import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.RollingGateServerRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 翻译实用程序类，用于处理语言翻译相关功能
 */
public class TranslationUtil {
    /**
     * Gson实例用于JSON解析
     */
    public static final Gson GSON = new Gson();
    /**
     * 存储所有语言及其相应的翻译
     */
    public static final Map<String, Map<String, String>> LANGUAGES = new HashMap<>();

    /**
     * 将密钥转换为相应的文本，并可选择格式替换
     *
     * @param key 翻译键
     * @param args 可选格式替换参数
     * @return 翻译和格式化的文本
     */
    public static @NotNull MutableComponent trans(String key, Object... args) {
        return Component.translatableWithFallback(
            key,
            LANGUAGES.getOrDefault(RollingGateServerRules.language, new HashMap<>())
                .getOrDefault(key, key)
                .formatted(args),
            args
        );
    }

    /**
     * 为指定语言添加一组翻译
     *
     * @param language 语言代码
     * @param translations 包含翻译的Map
     */
    public static void addLanguage(String language, Map<String, String> translations) {
        Map<String, String> languages = LANGUAGES.getOrDefault(language, new HashMap<>());
        languages.putAll(translations);
        LANGUAGES.putIfAbsent(language, languages);
    }

    /**
     * 从指定的命名空间和语言代码加载语言文件
     *
     * @param clazz 用于加载资源的类
     * @param namespace 资源命名空间
     * @param language 语言代码
     */
    @SuppressWarnings("unchecked")
    public static void loadLanguage(Class<?> clazz, String namespace, String language) {
        try (
            InputStream stream = clazz.getResourceAsStream("/assets/%s/lang/%s.json".formatted(namespace, language))
        ) {
            if (stream == null) {
                RollingGate.LOGGER.error("Can't find language {}/{}.", namespace, language);
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                TranslationUtil.addLanguage("zh_cn", (Map<String, String>) TranslationUtil.GSON.fromJson(reader, Map.class));
                RollingGate.LOGGER.info("Loaded {} language file.", language);
            }
        } catch (IOException e) {
            RollingGate.LOGGER.error("Failed to load %s language file.".formatted(language), e);
        }
    }
}
