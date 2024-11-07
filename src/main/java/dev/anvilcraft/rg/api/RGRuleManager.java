package dev.anvilcraft.rg.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.anvilcraft.rg.RollingGate;
import dev.anvilcraft.rg.util.ConfigUtil;
import dev.anvilcraft.rg.util.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RGRuleManager {
    public static final Gson GSON;
    private final Path globalConfigPath;
    private final LevelResource worldConfigPath;
    private final Map<String, RGRule<?>> rules;
    private final Map<RGRule<?>, Object> worldConfig = new HashMap<>();
    private final Map<RGRule<?>, Object> globalConfig = new HashMap<>();
    private String namespace = "rolling_gate";
    private final String managerNamespace;
    private final List<String> categories = new ArrayList<>();

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
        this.rules = new HashMap<>();
        this.globalConfigPath = FMLPaths.CONFIGDIR.get().resolve("%s%s.json".formatted(namespace, environment.isClient() ? "_client" : ""));
        this.worldConfigPath = new LevelResource("%s%s.json".formatted(namespace, environment.isClient() ? "_client" : ""));
        this.managerNamespace = namespace;
    }

    private @NotNull Map<String, Object> getWorldConfigSerialized() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<RGRule<?>, Object> entry : this.worldConfig.entrySet()) {
            RGRule<?> key = entry.getKey();
            result.put(key.serialize(), entry.getValue());
        }
        return result;
    }

    public <T> void setWorldConfig(@NotNull MinecraftServer server, @NotNull RGRule<T> rule, T value) {
        this.worldConfig.put(rule, value);
        ConfigUtil.writeContent(server.getWorldPath(worldConfigPath), GSON.toJson(this.getWorldConfigSerialized()));
    }

    public void reInit(@NotNull MinecraftServer server) {
        this.globalConfig.clear();
        this.worldConfig.clear();
        this.globalConfig.putAll(this.setSaveRules(ConfigUtil.getOrCreateContent(globalConfigPath)));
        Map<RGRule<?>, Object> world = this.setSaveRules(ConfigUtil.getOrCreateContent(server.getWorldPath(worldConfigPath)));
        for (Map.Entry<RGRule<?>, Object> entry : world.entrySet()) {
            if (entry.getValue().equals(this.globalConfig.get(entry.getKey()))) continue;
            this.worldConfig.put(entry.getKey(), entry.getValue());
        }
    }

    private @NotNull Map<RGRule<?>, Object> setSaveRules(@NotNull JsonObject config) {
        Map<RGRule<?>, Object> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            RGRule<?> rule = this.rules.get(entry.getKey());
            if (rule == null) {
                RollingGate.LOGGER.warn("{}({}) not exist.", entry.getKey(), entry.getValue());
                continue;
            }
            rule.setFieldValue(entry.getValue());
            result.put(rule, rule.codec().decode(entry.getValue().toString()));
        }
        return result;
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
            RGRule.checkType(field);
            ruleList.add(RGRule.of(namespace, field));
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

    @SuppressWarnings("unused")
    public void generateCommand(CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String literal) {
        this.generateCommand(dispatcher, literal, null);
    }

    @SuppressWarnings("unused")
    public void generateCommand(CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String literal, String redirect) {
        new Command(dispatcher, literal, redirect).generateCommand();
    }

    private class Command {
        @NotNull CommandDispatcher<CommandSourceStack> dispatcher;
        @NotNull String literal;
        String redirect;

        private Command(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String literal, String redirect) {
            this.dispatcher = dispatcher;
            this.literal = literal;
            this.redirect = redirect;
        }

        public void generateCommand() {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal)
                .requires(this::checkPermission)
                .executes(this::listCommand)
                .then(
                    Commands.literal("reload")
                        .executes(this::reloadCommand)
                )
                .then(
                    Commands.literal("category")
                        .then(
                            Commands.argument("category", StringArgumentType.word())
                                .suggests(this::suggestRuleCategories)
                                .executes(this::categoryCommand)
                        )
                );
            LiteralArgumentBuilder<CommandSourceStack> aDefault = Commands.literal("default");
            listCommand(aDefault, this::defaultRuleCommand, false);
            listCommand(root, this::setRuleCommand, true);
            root.then(aDefault);
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(root);
            if (redirect != null) dispatcher.register(
                Commands.literal(redirect)
                    .requires(this::checkPermission)
                    .executes(this::listCommand)
                    .redirect(register)
            );
        }

        private boolean checkPermission(@NotNull CommandSourceStack source) {
            if (source.hasPermission(Commands.LEVEL_GAMEMASTERS)) return true;
            if (!source.isPlayer()) return false;
            ServerPlayer player = source.getPlayer();
            if (player == null) return false;
            if (!source.getServer().isSingleplayer()) return false;
            return source.getServer().isSingleplayerOwner(player.getGameProfile());
        }

        private @NotNull CompletableFuture<Suggestions> suggestRuleCategories(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggest(categories, builder);
        }

        private void listCommand(LiteralArgumentBuilder<CommandSourceStack> builder, TriFunction<CommandContext<CommandSourceStack>, RGRule<?>, String, Integer> execute, boolean list) {
            for (Map.Entry<String, RGRule<?>> entry : rules.entrySet()) {
                RGRule<?> rgRule = entry.getValue();
                LiteralArgumentBuilder<CommandSourceStack> keyNode = Commands.literal(entry.getKey());
                if (list) keyNode.executes(ctx -> this.ruleInfoCommand(ctx, rgRule));
                for (String value : rgRule.allowed()) {
                    keyNode.then(Commands.literal(value).executes(ctx -> execute.apply(ctx, rgRule, value)));
                }
                builder.then(keyNode);
            }
        }

        private int reloadCommand(@NotNull CommandContext<CommandSourceStack> context) {
            reInit(context.getSource().getServer());
            context.getSource().sendSuccess(() -> TranslationUtil.trans("rolling_gate.command.reload.success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }

        private int listCommand(@NotNull CommandContext<CommandSourceStack> context) {
            Optional<? extends ModContainer> container = ModList.get().getModContainerById(managerNamespace);
            if (container.isPresent()) {
                IModInfo info = container.get().getModInfo();
                context.getSource().sendSuccess(() -> Component.literal(info.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE), false);
                context.getSource().sendSuccess(() -> TranslationUtil.trans("rolling_gate.command.root.version", info.getVersion().toString()).withStyle(ChatFormatting.GRAY), false);
                MutableComponent categoriesComponent = Component.empty();
                for (String category : categories) {
                    MutableComponent categoryComponent = Component.empty();
                    categoryComponent.append("[");
                    categoryComponent.append(TranslationUtil.trans(getDescriptionCategoryKey(category)));
                    categoryComponent.append("] ");
                    categoriesComponent.append(categoryComponent.withStyle(ChatFormatting.AQUA));
                    categoriesComponent.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s category %s".formatted(literal, category))));
                }
                context.getSource().sendSuccess(() -> categoriesComponent, false);
                return 1;
            }
            context.getSource().sendFailure(TranslationUtil.trans("rolling_gate.command.root.not_found", managerNamespace).withStyle(ChatFormatting.RED));
            return 0;
        }

        private <T> int ruleInfoCommand(@NotNull CommandContext<CommandSourceStack> context, @NotNull RGRule<T> rule) {
            CommandSourceStack source = context.getSource();
            source.sendSuccess(() -> TranslationUtil.trans(rule.getNameTranslationKey()), false);
            source.sendSuccess(() -> TranslationUtil.trans(rule.getDescriptionTranslationKey()), false);
            source.sendSuccess(() -> this.getValues(rule), false);
            return 1;
        }

        private <T> @NotNull MutableComponent getValues(@NotNull RGRule<T> rule) {
            MutableComponent result = Component.empty();
            for (String string : rule.allowed()) {
                if (!string.equals(rule.allowed()[0])) result.append(" ");
                //noinspection unchecked
                boolean isGlobalDefault = string.equals(rule.codec().encode((T) worldConfig.get(rule)));
                boolean isSelect = string.equals(rule.codec().encode(rule.getValue()));
                MutableComponent component = Component.literal("[%s]".formatted(string));
                Style style = Style.EMPTY;
                if (isSelect) {
                    style = style.withColor(ChatFormatting.GREEN);
                } else if (isGlobalDefault) {
                    style = style.withColor(ChatFormatting.BLUE);
                } else {
                    style = style.withColor(ChatFormatting.GRAY);
                }
                if (!isSelect) {
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TranslationUtil.trans("rolling_gate.command.rule.select.hover")));
                    style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s %s %s".formatted(literal, rule.name(), string)));
                }
                result.append(component.withStyle(style));
            }
            return result;
        }

        private int categoryCommand(@NotNull CommandContext<CommandSourceStack> context) {
            String category = StringArgumentType.getString(context, "category");
            for (RGRule<?> rule : rules.values()) {
                if (Arrays.stream(rule.categories()).noneMatch(s -> s.equals(category))) continue;
                MutableComponent component = Component.literal("- ");
                MutableComponent name = TranslationUtil.trans(rule.getNameTranslationKey());
                component.append(name);
                MutableComponent hover = TranslationUtil.trans(rule.getDescriptionTranslationKey());
                name.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
                MutableComponent values = this.getValues(rule);
                component.append(" ").append(values);
                context.getSource().sendSuccess(() -> component, false);
            }
            return 1;
        }

        private <T> int setRuleCommand(@NotNull CommandContext<CommandSourceStack> context, @NotNull RGRule<T> rule, String value) {
            rule.setFieldValue(value);
            MutableComponent result = TranslationUtil
                .trans("rolling_gate.command.rule.set", rule.name(), value)
                .withStyle(ChatFormatting.GRAY);
            MutableComponent setDefault = Component.literal("[")
                .append(TranslationUtil
                    .trans("rolling_gate.command.rule.set.default.button", rule.name(), value))
                .append("]")
                .withStyle(Style.EMPTY
                    .applyFormat(ChatFormatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/%s default %s %s".formatted(literal, rule.name(), value)))
                );
            result.append(" ").append(setDefault);
            context.getSource().sendSuccess(() -> result, false);
            return 1;
        }

        private <T> int defaultRuleCommand(@NotNull CommandContext<CommandSourceStack> context, @NotNull RGRule<T> rule, String value) {
            setWorldConfig(context.getSource().getServer(), rule, rule.codec().decode(value));
            MutableComponent result = TranslationUtil
                .trans("rolling_gate.command.rule.set.default", rule.name(), value)
                .withStyle(ChatFormatting.GRAY);
            context.getSource().sendSuccess(() -> result, false);
            return 1;
        }
    }
}
