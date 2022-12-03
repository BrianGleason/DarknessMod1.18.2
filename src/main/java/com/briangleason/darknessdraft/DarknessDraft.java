package com.briangleason.darknessdraft;

import com.briangleason.darknessdraft.mixin.network.MessageSyncConfig;
import com.briangleason.darknessdraft.mixin.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.system.CallbackI;
import org.slf4j.Logger;
import net.minecraftforge.network.NetworkConstants;


import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("darknessdraft")
public class DarknessDraft
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "darknessdraft";

    public DarknessDraft()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);

        if (FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);
            MinecraftForge.EVENT_BUS.addListener(this::onDisconnect);
        } else {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        }


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void onConfigChange(ModConfigEvent e) {
        if (!Config.onServer) {
            updateConfigSettings();
        }
    }

    private void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        DarknessDraft.LOGGER.info("Disconnecting from server, reverting back to client settings.");
        Config.onServer = false;
        updateConfigSettings();
    }

    private void onConfigReload(ModConfigEvent e) {
        DarknessDraft.LOGGER.info("Config updated, sending changes to players.");
        for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendConfigToClient(player);
        }
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        DarknessDraft.LOGGER.info("Server config settings being sent to player.");

        sendConfigToClient((ServerPlayer) event.getPlayer());
    }

    public void sendConfigToClient(ServerPlayer player) {
        PacketHandler.INSTANCE.sendTo(new MessageSyncConfig(DarknessDraft.Config.blockLightOnly.get(), DarknessDraft.Config.ignoreMoonPhase.get(), DarknessDraft.Config.minimumMoonLevel.get(), DarknessDraft.Config.maximumMoonLevel.get(),
                        DarknessDraft.Config.darkOverworld.get(), DarknessDraft.Config.darkDefault.get(), DarknessDraft.Config.darkNether.get(), DarknessDraft.Config.darkNetherFogConfigured.get(),
                        DarknessDraft.Config.darkEnd.get(), DarknessDraft.Config.darkEndFogConfigured.get(), DarknessDraft.Config.darkSkyless.get(), DarknessDraft.Config.minimumLight.get()),
                player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    private void updateConfigSettings() {
        Config.client_blockLightOnly = Config.blockLightOnly.get();
        Config.client_ignoreMoonPhase = Config.ignoreMoonPhase.get();
        Config.client_minimumMoonLevel = Config.minimumMoonLevel.get();
        Config.client_maximumMoonLevel = Config.maximumMoonLevel.get();
        Config.client_darkOverworld = Config.darkOverworld.get();
        Config.client_darkEnd = Config.darkEnd.get();
        Config.client_darkSkyless = Config.darkSkyless.get();
        Config.client_minimumLight = Config.minimumLight.get();
        bake();
    }

    public static final Config COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void bake() {
        Config.darkNetherFogEffective = Config.client_darkNether ? Config.darkNetherFogConfigured.get() : 1.0;
        Config.darkEndFogEffective = Config.client_darkEnd ? Config.darkEndFogConfigured.get() : 1.0;
    }

    public static boolean blockLightOnly() {
        return Config.client_blockLightOnly;
    }

    public static double darkNetherFog() {
        return Config.darkNetherFogEffective;
    }

    public static double darkEndFog() {
        return Config.darkEndFogEffective;
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    public static class Config {

        public static boolean onServer = false;
        public static double darkNetherFogEffective;
        public static double darkEndFogEffective;

        public static boolean client_blockLightOnly;
        public static boolean client_ignoreMoonPhase;
        public static double client_minimumMoonLevel;
        public static double client_maximumMoonLevel;
        public static boolean client_darkOverworld;
        public static boolean client_darkDefault;
        public static boolean client_darkNether;
        public static double client_darkNetherFogEffective;
        public static boolean client_darkEnd;
        public static double client_darkEndFogEffective;
        public static boolean client_darkSkyless;
        public static double client_minimumLight;

        public static ForgeConfigSpec.BooleanValue blockLightOnly;
        public static ForgeConfigSpec.BooleanValue ignoreMoonPhase;
        public static ForgeConfigSpec.DoubleValue minimumMoonLevel;
        public static ForgeConfigSpec.DoubleValue maximumMoonLevel;
        public static ForgeConfigSpec.BooleanValue darkOverworld;
        public static ForgeConfigSpec.BooleanValue darkDefault;
        public static ForgeConfigSpec.BooleanValue darkNether;
        public static ForgeConfigSpec.DoubleValue darkNetherFogConfigured;
        public static ForgeConfigSpec.BooleanValue darkEnd;
        public static ForgeConfigSpec.DoubleValue darkEndFogConfigured;
        public static ForgeConfigSpec.BooleanValue darkSkyless;
        public static ForgeConfigSpec.DoubleValue minimumLight;


        public Config(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            blockLightOnly = builder.define("only_affect_block_light", false);
            ignoreMoonPhase = builder.define("ignore_moon_phase", false);
            minimumMoonLevel = builder.defineInRange("minimum_moon_brightness", 0, 0, 1d);
            maximumMoonLevel = builder.defineInRange("maximum_moon_brightness", 1d, 0, 1d);
            darkOverworld = builder.define("dark_overworld", true);
            darkDefault = builder.define("dark_default", true);
            darkNether = builder.define("dark_nether", true);
            darkNetherFogConfigured = builder.defineInRange("dark_nether_fog", .5, 0, 1d);
            darkEnd = builder.define("dark_end", true);
            darkEndFogConfigured = builder.defineInRange("dark_end_fog", 0, 0, 1d);
            darkSkyless = builder.define("dark_skyless", true);
            minimumLight = builder.defineInRange("minimum_light", 0d,0d,1d);
            builder.pop();
        }
    }
}
