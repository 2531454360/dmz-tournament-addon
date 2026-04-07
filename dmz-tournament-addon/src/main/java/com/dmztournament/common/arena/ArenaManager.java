package com.dmztournament.common.arena;

import com.dmztournament.DMZTournament;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages all arenas in the tournament system.
 * Handles creation, deletion, and persistence of arenas.
 */
public class ArenaManager {
    private static final String DATA_FILE = "dmztournament_arenas.dat";
    
    private final MinecraftServer server;
    private final Map<UUID, Arena> arenas;
    private final Map<String, UUID> arenaNames;
    
    public ArenaManager(MinecraftServer server) {
        this.server = server;
        this.arenas = new HashMap<>();
        this.arenaNames = new HashMap<>();
    }
    
    /**
     * Create a new arena
     */
    public Arena createArena(String name, String creator) {
        if (arenaNames.containsKey(name.toLowerCase())) {
            return null; // Arena with this name already exists
        }
        
        Arena arena = new Arena(name, creator);
        arenas.put(arena.getId(), arena);
        arenaNames.put(name.toLowerCase(), arena.getId());
        
        saveData();
        DMZTournament.getLogger().info("Created new arena: {} by {}", name, creator);
        
        return arena;
    }
    
    /**
     * Delete an arena
     */
    public boolean deleteArena(UUID id) {
        Arena arena = arenas.remove(id);
        if (arena != null) {
            arenaNames.remove(arena.getName().toLowerCase());
            saveData();
            DMZTournament.getLogger().info("Deleted arena: {}", arena.getName());
            return true;
        }
        return false;
    }
    
    /**
     * Get an arena by ID
     */
    public Optional<Arena> getArena(UUID id) {
        return Optional.ofNullable(arenas.get(id));
    }
    
    /**
     * Get an arena by name
     */
    public Optional<Arena> getArena(String name) {
        UUID id = arenaNames.get(name.toLowerCase());
        if (id != null) {
            return Optional.ofNullable(arenas.get(id));
        }
        return Optional.empty();
    }
    
    /**
     * Get all arenas
     */
    public Collection<Arena> getAllArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }
    
    /**
     * Get all active arenas
     */
    public List<Arena> getActiveArenas() {
        return arenas.values().stream()
            .filter(Arena::isActive)
            .filter(arena -> !arena.isInUse())
            .toList();
    }
    
    /**
     * Get all available arenas for tournaments
     */
    public List<Arena> getAvailableArenas() {
        return arenas.values().stream()
            .filter(Arena::isValid)
            .filter(Arena::isActive)
            .filter(arena -> !arena.isInUse())
            .toList();
    }
    
    /**
     * Rename an arena
     */
    public boolean renameArena(UUID id, String newName) {
        if (arenaNames.containsKey(newName.toLowerCase())) {
            return false; // Name already taken
        }
        
        Arena arena = arenas.get(id);
        if (arena == null) {
            return false;
        }
        
        arenaNames.remove(arena.getName().toLowerCase());
        arena.setName(newName);
        arenaNames.put(newName.toLowerCase(), id);
        
        saveData();
        return true;
    }
    
    /**
     * Get total number of arenas
     */
    public int getArenaCount() {
        return arenas.size();
    }
    
    /**
     * Get number of active arenas
     */
    public int getActiveArenaCount() {
        return (int) arenas.values().stream().filter(Arena::isActive).count();
    }
    
    /**
     * Save arena data to disk
     */
    public void saveData() {
        try {
            File dataFile = getDataFile();
            CompoundTag rootTag = new CompoundTag();
            
            ListTag arenaList = new ListTag();
            for (Arena arena : arenas.values()) {
                arenaList.add(arena.serializeNBT());
            }
            rootTag.put("arenas", arenaList);
            rootTag.putInt("version", 1);
            
            NbtIo.write(rootTag, dataFile);
            DMZTournament.getLogger().debug("Saved {} arenas to disk", arenas.size());
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to save arena data", e);
        }
    }
    
    /**
     * Load arena data from disk
     */
    public void loadData() {
        File dataFile = getDataFile();
        
        if (!dataFile.exists()) {
            DMZTournament.getLogger().info("No arena data file found, starting fresh");
            return;
        }
        
        try {
            CompoundTag rootTag = NbtIo.read(dataFile);
            if (rootTag == null) {
                DMZTournament.getLogger().warn("Arena data file was empty");
                return;
            }
            
            int version = rootTag.getInt("version");
            if (version != 1) {
                DMZTournament.getLogger().warn("Arena data version mismatch: {}", version);
            }
            
            ListTag arenaList = rootTag.getList("arenas", Tag.TAG_COMPOUND);
            for (int i = 0; i < arenaList.size(); i++) {
                CompoundTag arenaTag = arenaList.getCompound(i);
                UUID id = arenaTag.getUUID("id");
                Arena arena = new Arena(id);
                arena.deserializeNBT(arenaTag);
                
                arenas.put(id, arena);
                arenaNames.put(arena.getName().toLowerCase(), id);
            }
            
            DMZTournament.getLogger().info("Loaded {} arenas from disk", arenas.size());
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to load arena data", e);
        }
    }
    
    private File getDataFile() {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(DATA_FILE).toFile();
    }
}
