package com.dmztournament.common.network;

import com.dmztournament.DMZTournament;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Network packet handler for the tournament addon.
 */
public class PacketHandler {
    private static SimpleChannel INSTANCE;
    
    private static int packetId = 0;
    
    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(DMZTournament.MOD_ID, "main"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();
        
        // Register packets here
        // Example:
        // INSTANCE.messageBuilder(MyPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
        //     .decoder(MyPacket::decode)
        //     .encoder(MyPacket::encode)
        //     .consumerMainThread(MyPacket::handle)
        //     .add();
        
        // Sync tournament data to clients
        INSTANCE.messageBuilder(SyncTournamentPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncTournamentPacket::decode)
            .encoder(SyncTournamentPacket::encode)
            .consumerMainThread(SyncTournamentPacket::handle)
            .add();
        
        // Sync match data to clients
        INSTANCE.messageBuilder(SyncMatchPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SyncMatchPacket::decode)
            .encoder(SyncMatchPacket::encode)
            .consumerMainThread(SyncMatchPacket::handle)
            .add();
    }
    
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    
    public static <MSG> void sendToClient(MSG message, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
    
    public static <MSG> void sendToDimension(MSG message, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }
}
