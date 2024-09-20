package dev.anvilcraft.rg.util;

import com.google.gson.Gson;
import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.RollingGateRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class TranslationUtil {
    public static final Gson GSON = new Gson();
    public static final Map<String, Map<String, String>> LANGUAGES = new HashMap<>();

    public static @NotNull MutableComponent trans(String key, Object... args) {
        return Component.translatableWithFallback(
            key,
            LANGUAGES.getOrDefault(RollingGateRules.language, new HashMap<>())
                .getOrDefault(key, key)
                .formatted(args),
            args
        );
    }

    public static void addLanguage(String language, Map<String, String> translations) {
        Map<String, String> languages = LANGUAGES.getOrDefault(language, new HashMap<>());
        languages.putAll(translations);
        LANGUAGES.putIfAbsent(language, languages);
    }

    @SuppressWarnings("unchecked")
    public static void loadLanguage(Class<?> clazz, String namespace, String language) {
        try (
            InputStream stream = clazz.getResourceAsStream("/assets/%s/lang/%s.json".formatted(namespace, language));
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
