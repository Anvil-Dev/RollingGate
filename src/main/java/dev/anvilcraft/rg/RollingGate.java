package dev.anvilcraft.rg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.anvilcraft.rg.api.client.ClientRGRuleManager;
import dev.anvilcraft.rg.api.RGAdditional;
import dev.anvilcraft.rg.api.server.ServerRGRuleManager;
import dev.anvilcraft.rg.api.server.TranslationUtil;
import dev.anvilcraft.rg.client.RollingGateClientRules;
import dev.anvilcraft.rg.tools.WelcomeMessage;
import dev.anvilcraft.rg.tools.serializer.ChatFormattingSerializer;
import dev.anvilcraft.rg.tools.serializer.DimTypeSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(RollingGate.MODID)
public class RollingGate implements RGAdditional {
    public static final String MODID = "rolling_gate";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ServerRGRuleManager SERVER_RULE_MANAGER = new ServerRGRuleManager(RollingGate.MODID);

    public RollingGate(@NotNull IEventBus modEventBus, @NotNull ModContainer modContainer) {
        modEventBus.addListener(this::onLoadComplete);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::registerCommand);
        modContainer.registerExtensionPoint(RGAdditional.class, this);
    }

    @Override
    public void loadServerRules(@NotNull ServerRGRuleManager manager) {
        manager.register(RollingGateServerRules.class);
        TranslationUtil.loadLanguage(RollingGate.class, MODID, "zh_cn");
        TranslationUtil.loadLanguage(RollingGate.class, MODID, "en_us");
    }

    @Override
    public void loadClientRules(@NotNull ClientRGRuleManager manager) {
        manager.register(RollingGateClientRules.class);
    }

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        ModList.get().forEachModContainer((modId, modContainer) -> {
            RollingGate.SERVER_RULE_MANAGER.setNamespace(modId);
            Optional<RGAdditional> additional = modContainer.getCustomExtension(RGAdditional.class);
            additional.ifPresent(add -> add.loadServerRules(RollingGate.SERVER_RULE_MANAGER));
        });
    }

    @SubscribeEvent
    public void onServerStarting(@NotNull ServerStartingEvent event) {
        RollingGate.SERVER_RULE_MANAGER.reInit(event.getServer());
    }

    @SubscribeEvent
    public void registerCommand(@NotNull RegisterCommandsEvent event) {
        RollingGate.SERVER_RULE_MANAGER.generateCommand(event.getDispatcher(), MODID, "rg");
    }

    @SubscribeEvent
    public void onPlayerLoggingIn(@NotNull PlayerEvent.PlayerLoggedInEvent event){
        if(RollingGateServerRules.welcomePlayer){
            WelcomeMessage.onPlayerLoggedIn((ServerPlayer) event.getEntity());
        }
    }

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(ResourceKey.class, new DimTypeSerializer())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeHierarchyAdapter(ChatFormatting.class, new ChatFormattingSerializer())
            .registerTypeHierarchyAdapter(WelcomeMessage.MessageData.class, new WelcomeMessage.MessageData.Serializer())
            .create();

    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static @NotNull ResourceLocation parseLocation(String string){
        return ResourceLocation.parse(string);
    }
}
