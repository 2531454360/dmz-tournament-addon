package com.dmztournament.common.tournament;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.rewards.RewardManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages all tournaments in the system.
 * Handles creation, execution, and persistence of tournaments.
 */
public class TournamentManager {
    private static final String DATA_FILE = "dmztournament_tournaments.dat";
    
    private final MinecraftServer server;
    private final Map<UUID, Tournament> tournaments;
    private final Map<UUID, Match> activeMatches;
    private final RewardManager rewardManager;
    
    @Nullable
    private Tournament activeTournament;
    
    public TournamentManager(MinecraftServer server) {
        this.server = server;
        this.tournaments = new HashMap<>();
        this.activeMatches = new HashMap<>();
        this.rewardManager = new RewardManager(server);
    }
    
    /**
     * Create a new tournament
     */
    public Tournament createTournament(String name, net.minecraft.server.level.ServerPlayer host) {
        Tournament tournament = new Tournament(name, host);
        tournaments.put(tournament.getId(), tournament);
        
        saveData();
        DMZTournament.getLogger().info("Created tournament: {} by {}", name, host.getName().getString());
        
        return tournament;
    }
    
    /**
     * Delete a tournament
     */
    public boolean deleteTournament(UUID id) {
        Tournament tournament = tournaments.remove(id);
        if (tournament != null) {
            if (tournament == activeTournament) {
                activeTournament = null;
            }
            saveData();
            DMZTournament.getLogger().info("Deleted tournament: {}", tournament.getName());
            return true;
        }
        return false;
    }
    
    /**
     * Get a tournament by ID
     */
    public Optional<Tournament> getTournament(UUID id) {
        return Optional.ofNullable(tournaments.get(id));
    }
    
    /**
     * Get all tournaments
     */
    public Collection<Tournament> getAllTournaments() {
        return Collections.unmodifiableCollection(tournaments.values());
    }
    
    /**
     * Get active tournaments
     */
    public List<Tournament> getActiveTournaments() {
        return tournaments.values().stream()
            .filter(t -> t.getState() == Tournament.TournamentState.IN_PROGRESS ||
                        t.getState() == Tournament.TournamentState.REGISTRATION)
            .toList();
    }
    
    /**
     * Get the currently active tournament
     */
    @Nullable
    public Tournament getActiveTournament() {
        return activeTournament;
    }
    
    /**
     * Set the active tournament
     */
    public void setActiveTournament(@Nullable Tournament tournament) {
        this.activeTournament = tournament;
    }
    
    /**
     * Start a tournament
     */
    public boolean startTournament(UUID id) {
        Tournament tournament = tournaments.get(id);
        if (tournament == null) {
            return false;
        }
        
        if (tournament.start()) {
            activeTournament = tournament;
            saveData();
            return true;
        }
        return false;
    }
    
    /**
     * Cancel a tournament
     */
    public boolean cancelTournament(UUID id) {
        Tournament tournament = tournaments.get(id);
        if (tournament == null) {
            return false;
        }
        
        tournament.cancel();
        if (tournament == activeTournament) {
            activeTournament = null;
        }
        saveData();
        return true;
    }
    
    /**
     * Register a player for a tournament
     */
    public boolean registerPlayer(UUID tournamentId, net.minecraft.server.level.ServerPlayer player) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null) {
            return false;
        }
        
        return tournament.registerPlayer(player);
    }
    
    /**
     * Unregister a player from a tournament
     */
    public boolean unregisterPlayer(UUID tournamentId, net.minecraft.server.level.ServerPlayer player) {
        Tournament tournament = tournaments.get(tournamentId);
        if (tournament == null) {
            return false;
        }
        
        return tournament.unregisterPlayer(player);
    }
    
    /**
     * Called when a match is completed
     */
    public void onMatchComplete(Match match) {
        // Update tournament if this was a tournament match
        if (activeTournament != null) {
            activeTournament.onMatchComplete(match);
        }
        
        // Remove from active matches
        activeMatches.remove(match.getId());
        
        // Update player statistics
        updatePlayerStats(match);
        
        saveData();
    }
    
    /**
     * Update player statistics after a match
     */
    private void updatePlayerStats(Match match) {
        // This will integrate with DMZ's stats system
        // For now, just log the results
        DMZTournament.getLogger().debug("Match completed: {} - Winner: {}", 
            match.getId(), match.getWinner());
    }
    
    /**
     * Distribute rewards for a tournament
     */
    public void distributeRewards(Tournament tournament) {
        rewardManager.distributeRewards(tournament);
    }
    
    /**
     * Get a match by ID
     */
    public Optional<Match> getMatch(UUID id) {
        return Optional.ofNullable(activeMatches.get(id));
    }
    
    /**
     * Check if a player is in any active match
     */
    public boolean isPlayerInMatch(UUID playerId) {
        return activeMatches.values().stream()
            .anyMatch(m -> m.hasPlayer(playerId) && m.isStarted() && !m.isFinished());
    }
    
    /**
     * Get the match a player is currently in
     */
    @Nullable
    public Match getPlayerMatch(UUID playerId) {
        return activeMatches.values().stream()
            .filter(m -> m.hasPlayer(playerId) && m.isStarted() && !m.isFinished())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Update all active matches (called every tick)
     */
    public void tick() {
        for (Match match : activeMatches.values()) {
            match.tick();
        }
    }
    
    /**
     * Save tournament data to disk
     */
    public void saveData() {
        try {
            File dataFile = getDataFile();
            CompoundTag rootTag = new CompoundTag();
            
            ListTag tournamentList = new ListTag();
            for (Tournament tournament : tournaments.values()) {
                tournamentList.add(tournament.serializeNBT());
            }
            rootTag.put("tournaments", tournamentList);
            rootTag.putInt("version", 1);
            
            NbtIo.write(rootTag, dataFile);
            DMZTournament.getLogger().debug("Saved {} tournaments to disk", tournaments.size());
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to save tournament data", e);
        }
    }
    
    /**
     * Load tournament data from disk
     */
    public void loadData() {
        File dataFile = getDataFile();
        
        if (!dataFile.exists()) {
            DMZTournament.getLogger().info("No tournament data file found, starting fresh");
            return;
        }
        
        try {
            CompoundTag rootTag = NbtIo.read(dataFile);
            if (rootTag == null) {
                DMZTournament.getLogger().warn("Tournament data file was empty");
                return;
            }
            
            int version = rootTag.getInt("version");
            if (version != 1) {
                DMZTournament.getLogger().warn("Tournament data version mismatch: {}", version);
            }
            
            ListTag tournamentList = rootTag.getList("tournaments", Tag.TAG_COMPOUND);
            for (int i = 0; i < tournamentList.size(); i++) {
                CompoundTag tournamentTag = tournamentList.getCompound(i);
                UUID id = tournamentTag.getUUID("id");
                Tournament tournament = new Tournament(id);
                tournament.deserializeNBT(tournamentTag);
                
                tournaments.put(id, tournament);
                
                // Restore active tournament reference
                if (tournament.getState() == Tournament.TournamentState.IN_PROGRESS) {
                    activeTournament = tournament;
                }
            }
            
            DMZTournament.getLogger().info("Loaded {} tournaments from disk", tournaments.size());
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to load tournament data", e);
        }
    }
    
    private File getDataFile() {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(DATA_FILE).toFile();
    }
    
    public MinecraftServer getServer() {
        return server;
    }
    
    public RewardManager getRewardManager() {
        return rewardManager;
    }
}
