package dev.anvilcraft.rg.api;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * ConfigUtil类提供文件操作实用程序，包括读取和写入JSON配置文件
 */
public class ConfigUtil {

    /**
     * 从指定的文件路径检索JSON对象内容。如果文件不存在或是目录，
     * 它创建一个包含空JSON对象内容的新文件，并返回一个空JsonObject
     *
     * @param path 文件路径，不得为空
     * @return 返回从文件读取的JSON对象，如果文件不存在，则返回一个新的空JsonObject
     * @throws RGRuleException 如果读取文件失败，则抛出异常并说明原因
     */
    public static @NotNull JsonObject getOrCreateContent(@NotNull Path path) {
        File file = path.toFile();
        try {
            if (!file.exists() || file.isDirectory()) {
                // 将空JSON对象写入不存在的或目录文件
                FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
                return new JsonObject();
            }
            // 读取文件内容并将其解析为JsonObject
            String value = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
            return GsonHelper.parse(value);
        } catch (IOException e) {
            // 如果读取文件失败，则抛出自定义异常
            throw new RGRuleException("Failed to read rolling gate config file", e);
        }
    }

    /**
     * 将内容写入指定的文件路径
     *
     * @param path 文件路径，不得为空
     * @param content 要写入的内容，不能为空
     * @throws RGRuleException 如果写入文件失败，则抛出异常并说明原因
     */
    public static void writeContent(@NotNull Path path, @NotNull String content) {
        try {
            // 将指定内容写入文件
            FileUtils.writeStringToFile(path.toFile(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 如果写入文件失败，则抛出自定义异常
            throw new RGRuleException("Failed to write rolling gate config file", e);
        }
    }
}

