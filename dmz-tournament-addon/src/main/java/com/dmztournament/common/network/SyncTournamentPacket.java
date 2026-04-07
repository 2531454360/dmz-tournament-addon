package com.dmztournament.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync tournament data to clients.
 */
public class SyncTournamentPacket {
    private final UUID tournamentId;
    private final String name;
    private final String state;
    private final int participantCount;
    private final int maxParticipants;
    
    public SyncTournamentPacket(UUID tournamentId, String name, String state, 
                                 int participantCount, int maxParticipants) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.state = state;
        this.participantCount = participantCount;
        this.maxParticipants = maxParticipants;
    }
    
    public static void encode(SyncTournamentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.tournamentId);
        buffer.writeUtf(packet.name);
        buffer.writeUtf(packet.state);
        buffer.writeInt(packet.participantCount);
        buffer.writeInt(packet.maxParticipants);
    }
    
    public static SyncTournamentPacket decode(FriendlyByteBuf buffer) {
        return new SyncTournamentPacket(
            buffer.readUUID(),
            buffer.readUtf(),
            buffer.readUtf(),
            buffer.readInt(),
            buffer.readInt()
        );
    }
    
    public static void handle(SyncTournamentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            // Update client-side tournament data
            // This would be used for UI updates
        });
        context.setPacketHandled(true);
    }
    
    // Getters
    public UUID getTournamentId() { return tournamentId; }
    public String getName() { return name; }
    public String getState() { return state; }
    public int getParticipantCount() { return participantCount; }
    public int getMaxParticipants() { return maxParticipants; }
}
