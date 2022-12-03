package com.briangleason.darknessdraft.mixin.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import com.briangleason.darknessdraft.DarknessDraft;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1.0";
    private static final List<String> PROTOCOLS = Arrays.asList(new String[]{PROTOCOL_VERSION, NetworkRegistry.ABSENT, NetworkRegistry.ACCEPTVANILLA});

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(
            DarknessDraft.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOLS::contains,
            PROTOCOL_VERSION::equals
    );

    public static void setup() {
        DarknessDraft.LOGGER.info("Registering network messages");

        INSTANCE.registerMessage(0, MessageSyncConfig.class, MessageSyncConfig::encode, MessageSyncConfig::decode, MessageSyncConfig::clientHandle, Optional.of(PLAY_TO_CLIENT));
    }
}
