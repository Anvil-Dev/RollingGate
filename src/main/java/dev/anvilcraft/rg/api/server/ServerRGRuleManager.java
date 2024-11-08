package dev.anvilcraft.rg.api.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.anvilcraft.rg.api.ConfigUtil;
import dev.anvilcraft.rg.api.RGEnvironment;
import dev.anvilcraft.rg.api.RGRule;
import dev.anvilcraft.rg.api.RGRuleManager;
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
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 服务器端RGRule管理器类，继承自RGRuleManager
 * 用于管理服务器端的规则，包括规则的设置、重新初始化以及命令生成
 */
public class ServerRGRuleManager extends RGRuleManager {
    // 世界配置文件路径
    private final LevelResource worldConfigPath;
    // 用于存储世界特定规则配置的映射
    private final Map<RGRule<?>, Object> worldConfig = new HashMap<>();

    /**
     * 构造函数
     * 初始化ServerRGRuleManager实例，设置其命名空间和环境为服务器端
     *
     * @param namespace 命名空间，用于标识规则管理器
     */
    public ServerRGRuleManager(String namespace) {
        super(namespace, RGEnvironment.SERVER);
        this.worldConfigPath = new LevelResource("%s.json".formatted(namespace));
    }

    /**
     * 设置世界配置
     * 将指定规则的值存储到世界配置中，并更新配置文件
     *
     * @param server 服务器实例，用于访问世界路径
     * @param rule   要设置的规则
     * @param value  规则的值
     * @param <T>    规则值的类型
     */
    public <T> void setWorldConfig(@NotNull MinecraftServer server, @NotNull RGRule<T> rule, T value) {
        this.worldConfig.put(rule, value);
        ConfigUtil.writeContent(server.getWorldPath(worldConfigPath), GSON.toJson(this.getSerializedConfig(this.worldConfig)));
    }

    /**
     * 重新初始化世界配置
     * 清空当前世界配置，并从配置文件中重新加载配置
     *
     * @param server 服务器实例，用于访问世界路径
     */
    public void reInit(@NotNull MinecraftServer server) {
        super.reInit();
        this.worldConfig.clear();
        Map<RGRule<?>, Object> world = this.setSaveRules(ConfigUtil.getOrCreateContent(server.getWorldPath(worldConfigPath)));
        for (Map.Entry<RGRule<?>, Object> entry : world.entrySet()) {
            if (entry.getValue().equals(this.globalConfig.get(entry.getKey()))) continue;
            this.worldConfig.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 生成命令
     * 根据提供的字面量在命令调度器中注册命令
     *
     * @param dispatcher 命令调度器，用于注册命令
     * @param literal    命令的字面量
     */
    @SuppressWarnings("unused")
    public void generateCommand(CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String literal) {
        this.generateCommand(dispatcher, literal, null);
    }

    /**
     * 生成命令
     * 根据提供的字面量和重定向路径在命令调度器中注册命令
     *
     * @param dispatcher 命令调度器，用于注册命令
     * @param literal    命令的字面量
     * @param redirect   命令的重定向路径，可以为null
     */
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

        private void generateCommand() {
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
                LiteralArgumentBuilder<CommandSourceStack> keyNode = Commands.literal(rgRule.name());
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
                    categoryComponent.withStyle(
                        Style.EMPTY
                            .applyFormat(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s category %s".formatted(literal, category)))
                    );
                    categoriesComponent.append(categoryComponent);
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
                Object worldDefault = worldConfig.get(rule);
                Object globalDefault = globalConfig.get(rule);
                T ruleDefault = rule.defaultValue();
                boolean isGlobalDefault;
                if (worldDefault != null) {
                    //noinspection unchecked
                    isGlobalDefault = string.equals(rule.codec().encode((T) worldDefault));
                } else if (globalDefault != null) {
                    //noinspection unchecked
                    isGlobalDefault = string.equals(rule.codec().encode((T) globalDefault));
                } else {
                    isGlobalDefault = string.equals(rule.codec().encode(ruleDefault));
                }
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
            MutableComponent categoryComponent = TranslationUtil.trans(getDescriptionCategoryKey(category)).append(":");
            context.getSource().sendSuccess(() -> categoryComponent, false);
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
