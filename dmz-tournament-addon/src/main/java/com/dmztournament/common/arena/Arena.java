package com.dmztournament.common.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a battle arena for tournaments.
 * Each arena has defined boundaries, spawn points, and spectator areas.
 */
public class Arena {
    private final UUID id;
    private String name;
    private String description;
    private String creator;
    
    // Arena boundaries
    private BlockPos corner1;
    private BlockPos corner2;
    
    // Spawn positions
    private BlockPos player1Spawn;
    private BlockPos player2Spawn;
    private List<BlockPos> spectatorSpawns;
    
    // Arena state
    private boolean isActive;
    private boolean isInUse;
    private ArenaType type;
    private String requiredDimension;
    
    // Statistics
    private int totalMatches;
    private int tournamentMatches;
    
    public enum ArenaType {
        STANDARD("standard", "Standard Arena"),
        ELEVATED("elevated", "Elevated Platform"),
        UNDERWATER("underwater", "Underwater Arena"),
        LAVA("lava", "Lava Pit Arena"),
        VOID("void", "Void Arena"),
        CUSTOM("custom", "Custom Arena");
        
        private final String id;
        private final String displayName;
        
        ArenaType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        
        public static ArenaType fromId(String id) {
            for (ArenaType type : values()) {
                if (type.id.equalsIgnoreCase(id)) {
                    return type;
                }
            }
            return STANDARD;
        }
    }
    
    public Arena(String name, String creator) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.creator = creator;
        this.description = "";
        this.spectatorSpawns = new ArrayList<>();
        this.type = ArenaType.STANDARD;
        this.requiredDimension = Level.OVERWORLD.location().toString();
        this.isActive = false;
        this.isInUse = false;
        this.totalMatches = 0;
        this.tournamentMatches = 0;
    }
    
    public Arena(UUID id) {
        this.id = id;
        this.spectatorSpawns = new ArrayList<>();
    }
    
    /**
     * Check if a position is within the arena boundaries
     */
    public boolean isWithinBoundaries(Vec3 pos) {
        if (corner1 == null || corner2 == null) return false;
        
        AABB bounds = new AABB(
            Math.min(corner1.getX(), corner2.getX()),
            Math.min(corner1.getY(), corner2.getY()),
            Math.min(corner1.getZ(), corner2.getZ()),
            Math.max(corner1.getX(), corner2.getX()),
            Math.max(corner1.getY(), corner2.getY()),
            Math.max(corner1.getZ(), corner2.getZ())
        );
        
        return bounds.contains(pos);
    }
    
    /**
     * Teleport a player to their spawn position
     */
    public boolean teleportPlayer(ServerPlayer player, int playerNumber) {
        BlockPos spawn = playerNumber == 1 ? player1Spawn : player2Spawn;
        if (spawn == null) return false;
        
        ServerLevel level = player.server.getLevel(player.level().dimension());
        if (level == null) return false;
        
        player.teleportTo(level, 
            spawn.getX() + 0.5, 
            spawn.getY(), 
            spawn.getZ() + 0.5, 
            player.getYRot(), 
            player.getXRot()
        );
        return true;
    }
    
    /**
     * Teleport a spectator to a random spectator spawn
     */
    public boolean teleportSpectator(ServerPlayer player) {
        if (spectatorSpawns.isEmpty()) return false;
        
        BlockPos spawn = spectatorSpawns.get(new Random().nextInt(spectatorSpawns.size()));
        ServerLevel level = player.server.getLevel(player.level().dimension());
        if (level == null) return false;
        
        player.teleportTo(level,
            spawn.getX() + 0.5,
            spawn.getY(),
            spawn.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );
        return true;
    }
    
    /**
     * Get the center position of the arena
     */
    public Vec3 getCenter() {
        if (corner1 == null || corner2 == null) return Vec3.ZERO;
        
        return new Vec3(
            (corner1.getX() + corner2.getX()) / 2.0,
            (corner1.getY() + corner2.getY()) / 2.0,
            (corner1.getZ() + corner2.getZ()) / 2.0
        );
    }
    
    /**
     * Validate that the arena is properly configured
     */
    public boolean isValid() {
        return corner1 != null && corner2 != null 
            && player1Spawn != null && player2Spawn != null
            && !spectatorSpawns.isEmpty()
            && name != null && !name.isEmpty();
    }
    
    /**
     * Serialize arena to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.putString("description", description);
        tag.putString("creator", creator);
        tag.putString("type", type.getId());
        tag.putString("dimension", requiredDimension);
        tag.putBoolean("active", isActive);
        tag.putInt("totalMatches", totalMatches);
        tag.putInt("tournamentMatches", tournamentMatches);
        
        if (corner1 != null) {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", corner1.getX());
            pos.putInt("y", corner1.getY());
            pos.putInt("z", corner1.getZ());
            tag.put("corner1", pos);
        }
        
        if (corner2 != null) {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", corner2.getX());
            pos.putInt("y", corner2.getY());
            pos.putInt("z", corner2.getZ());
            tag.put("corner2", pos);
        }
        
        if (player1Spawn != null) {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", player1Spawn.getX());
            pos.putInt("y", player1Spawn.getY());
            pos.putInt("z", player1Spawn.getZ());
            tag.put("player1Spawn", pos);
        }
        
        if (player2Spawn != null) {
            CompoundTag pos = new CompoundTag();
            pos.putInt("x", player2Spawn.getX());
            pos.putInt("y", player2Spawn.getY());
            pos.putInt("z", player2Spawn.getZ());
            tag.put("player2Spawn", pos);
        }
        
        ListTag spectatorList = new ListTag();
        for (BlockPos pos : spectatorSpawns) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            spectatorList.add(posTag);
        }
        tag.put("spectatorSpawns", spectatorList);
        
        return tag;
    }
    
    /**
     * Deserialize arena from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.name = tag.getString("name");
        this.description = tag.getString("description");
        this.creator = tag.getString("creator");
        this.type = ArenaType.fromId(tag.getString("type"));
        this.requiredDimension = tag.getString("dimension");
        this.isActive = tag.getBoolean("active");
        this.totalMatches = tag.getInt("totalMatches");
        this.tournamentMatches = tag.getInt("tournamentMatches");
        
        if (tag.contains("corner1")) {
            CompoundTag pos = tag.getCompound("corner1");
            this.corner1 = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
        
        if (tag.contains("corner2")) {
            CompoundTag pos = tag.getCompound("corner2");
            this.corner2 = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
        
        if (tag.contains("player1Spawn")) {
            CompoundTag pos = tag.getCompound("player1Spawn");
            this.player1Spawn = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
        
        if (tag.contains("player2Spawn")) {
            CompoundTag pos = tag.getCompound("player2Spawn");
            this.player2Spawn = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
        
        if (tag.contains("spectatorSpawns")) {
            ListTag list = tag.getList("spectatorSpawns", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag posTag = list.getCompound(i);
                spectatorSpawns.add(new BlockPos(
                    posTag.getInt("x"), 
                    posTag.getInt("y"), 
                    posTag.getInt("z")
                ));
            }
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreator() { return creator; }
    public BlockPos getCorner1() { return corner1; }
    public void setCorner1(BlockPos corner1) { this.corner1 = corner1; }
    public BlockPos getCorner2() { return corner2; }
    public void setCorner2(BlockPos corner2) { this.corner2 = corner2; }
    public BlockPos getPlayer1Spawn() { return player1Spawn; }
    public void setPlayer1Spawn(BlockPos player1Spawn) { this.player1Spawn = player1Spawn; }
    public BlockPos getPlayer2Spawn() { return player2Spawn; }
    public void setPlayer2Spawn(BlockPos player2Spawn) { this.player2Spawn = player2Spawn; }
    public List<BlockPos> getSpectatorSpawns() { return spectatorSpawns; }
    public void addSpectatorSpawn(BlockPos pos) { spectatorSpawns.add(pos); }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isInUse() { return isInUse; }
    public void setInUse(boolean inUse) { isInUse = inUse; }
    public ArenaType getType() { return type; }
    public void setType(ArenaType type) { this.type = type; }
    public String getRequiredDimension() { return requiredDimension; }
    public void setRequiredDimension(String dimension) { this.requiredDimension = dimension; }
    public int getTotalMatches() { return totalMatches; }
    public void incrementTotalMatches() { this.totalMatches++; }
    public int getTournamentMatches() { return tournamentMatches; }
    public void incrementTournamentMatches() { this.tournamentMatches++; }
}
