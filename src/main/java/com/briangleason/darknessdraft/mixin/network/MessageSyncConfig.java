package com.briangleason.darknessdraft.mixin.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.briangleason.darknessdraft.DarknessDraft;

import java.util.function.Supplier;

public class MessageSyncConfig {

    public boolean blockLightOnly;
    public boolean ignoreMoonPhase;
    public double minimumMoonLevel;
    public double maximumMoonLevel;
    public boolean darkOverworld;
    public boolean darkDefault;
    public boolean darkNether;
    public double darkNetherFogEffective;
    public boolean darkEnd;
    public double darkEndFogEffective;
    public boolean darkSkyless;
    public double minimumLight;

    public MessageSyncConfig(boolean blockLightOnly, boolean ignoreMoonPhase, double minimumMoonLevel, double maximumMoonLevel,
                             boolean darkOverworld, boolean darkDefault, boolean darkNether, double darkNetherFogEffective,
                             boolean darkEnd, double darkEndFogEffective, boolean darkSkyless, double minimumLight) {
        this.blockLightOnly = blockLightOnly;
        this.ignoreMoonPhase = ignoreMoonPhase;
        this.minimumMoonLevel = minimumMoonLevel;
        this.maximumMoonLevel = maximumMoonLevel;
        this.darkOverworld = darkOverworld;
        this.darkDefault = darkDefault;
        this.darkNether = darkNether;
        this.darkNetherFogEffective = darkNetherFogEffective;
        this.darkEnd = darkEnd;
        this.darkEndFogEffective = darkEndFogEffective;
        this.darkSkyless = darkSkyless;
        this.minimumLight = minimumLight;
    }

    public static MessageSyncConfig decode(FriendlyByteBuf buffer) {
        MessageSyncConfig message = null;
        try {
            message = new MessageSyncConfig(buffer.readBoolean(), buffer.readBoolean(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(),
                    buffer.readBoolean(), buffer.readBoolean(), buffer.readDouble(),
                    buffer.readBoolean(), buffer.readDouble(), buffer.readBoolean(),
                    buffer.readDouble());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            DarknessDraft.LOGGER.warn("Exception while reading MessageConfigSync: " + e);
        }
        return message;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(DarknessDraft.Config.blockLightOnly.get());
        buffer.writeBoolean(DarknessDraft.Config.ignoreMoonPhase.get());
        buffer.writeDouble(DarknessDraft.Config.minimumMoonLevel.get());
        buffer.writeDouble(DarknessDraft.Config.maximumMoonLevel.get());
        buffer.writeBoolean(DarknessDraft.Config.darkOverworld.get());
        buffer.writeBoolean(DarknessDraft.Config.darkDefault.get());
        buffer.writeBoolean(DarknessDraft.Config.darkNether.get());
        buffer.writeDouble(DarknessDraft.Config.darkNetherFogConfigured.get());
        buffer.writeBoolean(DarknessDraft.Config.darkEnd.get());
        buffer.writeDouble(DarknessDraft.Config.darkEndFogConfigured.get());
        buffer.writeBoolean(DarknessDraft.Config.darkSkyless.get());
        buffer.writeDouble(DarknessDraft.Config.minimumLight.get());
    }

    public static void clientHandle(MessageSyncConfig msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DarknessDraft.Config.onServer = true;
            DarknessDraft.Config.client_blockLightOnly = msg.blockLightOnly;
            DarknessDraft.Config.client_ignoreMoonPhase = msg.ignoreMoonPhase;
            DarknessDraft.Config.client_minimumMoonLevel = msg.minimumMoonLevel;
            DarknessDraft.Config.client_maximumMoonLevel = msg.maximumMoonLevel;
            DarknessDraft.Config.client_darkOverworld = msg.darkOverworld;
            DarknessDraft.Config.client_darkDefault = msg.darkDefault;
            DarknessDraft.Config.client_darkNether = msg.darkNether;
            DarknessDraft.Config.client_darkNetherFogEffective = msg.darkNetherFogEffective;
            DarknessDraft.Config.client_darkEnd = msg.darkEnd;
            DarknessDraft.Config.client_darkEndFogEffective = msg.darkNetherFogEffective;
            DarknessDraft.Config.client_darkSkyless = msg.darkSkyless;
            DarknessDraft.Config.client_minimumLight = msg.minimumLight;

            DarknessDraft.LOGGER.info("Settings synced with server config.");
        });
        ctx.get().setPacketHandled(true);
    }
}
