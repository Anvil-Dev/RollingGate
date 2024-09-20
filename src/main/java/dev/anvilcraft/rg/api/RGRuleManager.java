package dev.anvilcraft.rg.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.util.ConfigUtil;
import dev.anvilcraft.rg.util.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RGRuleManager {
    private final Path globalConfigPath;
    private final LevelResource worldConfigPath;
    private final Map<String, RGRule<?>> rules;
    private String namespace = "rolling_gate";
    private final String managerNamespace;
    private final List<String> categories = new ArrayList<>();

    public RGRuleManager(String namespace, @NotNull RGEnvironment environment) {
        this.rules = new HashMap<>();
        this.globalConfigPath = FMLPaths.CONFIGDIR.get().resolve("%s%s.json".formatted(namespace, environment.isClient() ? "_client" : ""));
        this.worldConfigPath = new LevelResource("%s%s.json".formatted(namespace, environment.isClient() ? "_client" : ""));
        this.managerNamespace = namespace;
    }

    public void reInit(@NotNull MinecraftServer server) {
        this.setSaveRules(ConfigUtil.getOrCreateContent(globalConfigPath));
        Path path = server.getWorldPath(worldConfigPath);
        this.setSaveRules(ConfigUtil.getOrCreateContent(path));
    }

    private void setSaveRules(@NotNull JsonObject config) {
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            rule.setFieldValue(entry.getValue());
        }
    }

    public void addRule(@NotNull RGRule<?> rule) {
        this.rules.put(rule.serialize(), rule);
        this.categories.addAll(Arrays.asList(rule.categories()));
    }

    public void register(Class<?> rules) {
        RGRuleManager.of(this.namespace, rules).forEach(this::addRule);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    private static @NotNull List<RGRule<?>> of(String namespace, @NotNull Class<?> rules) {
        List<RGRule<?>> ruleList = new ArrayList<>();
        for (Field field : rules.getDeclaredFields()) {
            dev.anvilcraft.rg.api.RGRule.checkType(field);
            ruleList.add(dev.anvilcraft.rg.api.RGRule.of(namespace, field));
        }
        return ruleList;
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

    public void generateCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        root.executes(this::menuCommand);

        root.then(Commands.literal("reload")
            .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(ctx -> { // 重新加载配置文件
                this.reInit(ctx.getSource().getServer());
                ctx.getSource().sendSuccess(() -> TranslationUtil.trans("rolling_gate.reload.success").withStyle(ChatFormatting.GREEN), false);
                return 1;
            }));

        LiteralArgumentBuilder<CommandSourceStack> list = Commands.literal("list").executes(this::listCommand); // 列出所有修改过的规则
        for (Map.Entry<String, RGRule<?>> entry : this.rules.entrySet()) {
            list.then(Commands.literal(entry.getKey()).executes(context -> this.listRuleCommand(context, entry.getValue()))); // 查看规则的详细信息
        }
        root.then(list);

        LiteralArgumentBuilder<CommandSourceStack> category = Commands.literal("category");
        List<String> categories = new ArrayList<>();
        for (Map.Entry<String, RGRule<?>> entry : this.rules.entrySet()) {
            categories.addAll(Arrays.asList(entry.getValue().categories()));
        }
        for (String s : categories) {
            category.then(
                Commands.literal(s).executes(context -> this.categoryCommand(context, s)) // 查看分组下的所有规则
            );
        }
        root.then(category);

        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set")
            .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Map.Entry<String, RGRule<?>> entry : this.rules.entrySet()) {
            set.then(
                Commands.literal(entry.getKey())
                    .then(
                        Commands.argument("value", StringArgumentType.string())
                            .executes(context -> this.setRuleCommand(context, entry.getValue(), StringArgumentType.getString(context, "value"))) // 设置规则的值
                    )
            );
        }
        root.then(set);

        LiteralArgumentBuilder<CommandSourceStack> setDefault = Commands.literal("set")
            .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Map.Entry<String, RGRule<?>> entry : this.rules.entrySet()) {
            setDefault.then(
                Commands.literal(entry.getKey())
                    .then(
                        Commands.argument("value", StringArgumentType.string())
                            .executes(context -> this.defaultRuleCommand(context, entry.getValue(), StringArgumentType.getString(context, "value"))) // 设置规则的值
                    )
            );
        }
        root.then(setDefault);
    }

    int menuCommand(@NotNull CommandContext<CommandSourceStack> context) {
        String command = context.getInput();
        Optional<? extends ModContainer> container = ModList.get().getModContainerById(this.managerNamespace);
        if (container.isPresent()) {
            IModInfo info = container.get().getModInfo();
            context.getSource().sendSuccess(() -> Component.literal(info.getDisplayName()).withStyle(ChatFormatting.DARK_BLUE), false);
            context.getSource().sendSuccess(() -> TranslationUtil.trans("rolling_gate.command.root.version", info.getVersion().toString()).withStyle(ChatFormatting.GRAY), false);
            MutableComponent categories = Component.empty();
            for (String category : this.categories) {
                MutableComponent categoryComponent = Component.empty();
                categoryComponent.append("[");
                categoryComponent.append(TranslationUtil.trans(this.getDescriptionCategoryKey(category)));
                categoryComponent.append("] ");
                categories.append(categoryComponent.withStyle(ChatFormatting.BLUE));
                categories.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s category %s".formatted(command, category))));
            }
            context.getSource().sendSuccess(() -> categories, false);
            return 1;
        }
        context.getSource().sendFailure(TranslationUtil.trans("rolling_gate.command.root.not_found", this.managerNamespace).withStyle(ChatFormatting.RED));
        return 0;
    }

    int listCommand(CommandContext<CommandSourceStack> context) {
        return 1;
    }

    int listRuleCommand(CommandContext<CommandSourceStack> context, RGRule<?> rule) {
        return 1;
    }

    int categoryCommand(CommandContext<CommandSourceStack> context, String category) {
        this.rules.values().stream().filter(rule -> Arrays.asList(rule.categories()).contains(category)).forEach(rule -> {
            context.getSource().sendSuccess(() -> TranslationUtil.trans(rule.getNameTranslationKey()), false);
        });
        return 1;
    }

    int setRuleCommand(CommandContext<CommandSourceStack> context, RGRule<?> rule, Object value) {
        return 1;
    }

    int defaultRuleCommand(CommandContext<CommandSourceStack> context, RGRule<?> rule, Object value) {
        return 1;
    }
}
