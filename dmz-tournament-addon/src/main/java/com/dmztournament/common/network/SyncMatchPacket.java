package com.dmztournament.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync match data to clients.
 */
public class SyncMatchPacket {
    private final UUID matchId;
    private final String state;
    private final int timeRemaining;
    private final UUID player1;
    private final UUID player2;
    private final UUID winner;
    
    public SyncMatchPacket(UUID matchId, String state, int timeRemaining, 
                           UUID player1, UUID player2, UUID winner) {
        this.matchId = matchId;
        this.state = state;
        this.timeRemaining = timeRemaining;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }
    
    public static void encode(SyncMatchPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.matchId);
        buffer.writeUtf(packet.state);
        buffer.writeInt(packet.timeRemaining);
        buffer.writeUUID(packet.player1);
        buffer.writeUUID(packet.player2);
        buffer.writeBoolean(packet.winner != null);
        if (packet.winner != null) {
            buffer.writeUUID(packet.winner);
        }
    }
    
    public static SyncMatchPacket decode(FriendlyByteBuf buffer) {
        UUID matchId = buffer.readUUID();
        String state = buffer.readUtf();
        int timeRemaining = buffer.readInt();
        UUID player1 = buffer.readUUID();
        UUID player2 = buffer.readUUID();
        UUID winner = buffer.readBoolean() ? buffer.readUUID() : null;
        
        return new SyncMatchPacket(matchId, state, timeRemaining, player1, player2, winner);
    }
    
    public static void handle(SyncMatchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            // Update client-side match data for UI
        });
        context.setPacketHandled(true);
    }
    
    // Getters
    public UUID getMatchId() { return matchId; }
    public String getState() { return state; }
    public int getTimeRemaining() { return timeRemaining; }
    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public UUID getWinner() { return winner; }
}
