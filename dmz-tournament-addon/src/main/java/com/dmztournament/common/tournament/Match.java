package com.dmztournament.common.tournament;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.config.TournamentConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a single match between players.
 * Tracks match state, scores, and winner.
 */
public class Match {
    private final UUID id;
    private final List<UUID> players;
    private final MatchType type;
    
    // Match state
    private MatchState state;
    private long startTime;
    private long endTime;
    private int timeLimit; // in seconds, -1 for no limit
    
    // Scores
    private final Map<UUID, Integer> scores;
    private final Map<UUID, Integer> deaths;
    private final Map<UUID, Integer> kills;
    
    // Winner
    @Nullable
    private UUID winner;
    @Nullable
    private UUID loser;
    
    // Match statistics
    private int totalHits;
    private int totalKiAttacks;
    private long totalDamageDealt;
    
    public enum MatchType {
        ONE_VS_ONE("1v1", "1v1 Duel", 2),
        TWO_VS_TWO("2v2", "2v2 Team Battle", 4),
        THREE_VS_THREE("3v3", "3v3 Team Battle", 6),
        FREE_FOR_ALL("ffa", "Free For All", -1),
        TOURNAMENT("tournament", "Tournament Match", 2);
        
        private final String id;
        private final String displayName;
        private final int requiredPlayers;
        
        MatchType(String id, String displayName, int requiredPlayers) {
            this.id = id;
            this.displayName = displayName;
            this.requiredPlayers = requiredPlayers;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public int getRequiredPlayers() { return requiredPlayers; }
    }
    
    public enum MatchState {
        WAITING("waiting", "Waiting for players"),
        STARTING("starting", "Starting..."),
        IN_PROGRESS("in_progress", "In Progress"),
        PAUSED("paused", "Paused"),
        FINISHED("finished", "Finished"),
        CANCELLED("cancelled", "Cancelled");
        
        private final String id;
        private final String displayName;
        
        MatchState(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
    
    // Constructor for 1v1 matches
    public Match(UUID player1, UUID player2, MatchType type) {
        this.id = UUID.randomUUID();
        this.players = new ArrayList<>(Arrays.asList(player1, player2));
        this.type = type;
        this.state = MatchState.WAITING;
        this.timeLimit = TournamentConfig.COMMON.MATCH_TIME_LIMIT.get();
        this.scores = new HashMap<>();
        this.deaths = new HashMap<>();
        this.kills = new HashMap<>();
    }
    
    // Constructor for multi-player matches
    public Match(List<UUID> players, MatchType type) {
        this.id = UUID.randomUUID();
        this.players = new ArrayList<>(players);
        this.type = type;
        this.state = MatchState.WAITING;
        this.timeLimit = TournamentConfig.COMMON.MATCH_TIME_LIMIT.get();
        this.scores = new HashMap<>();
        this.deaths = new HashMap<>();
        this.kills = new HashMap<>();
    }
    
    // Constructor for loading from NBT
    public Match(UUID id) {
        this.id = id;
        this.players = new ArrayList<>();
        this.type = MatchType.ONE_VS_ONE;
        this.scores = new HashMap<>();
        this.deaths = new HashMap<>();
        this.kills = new HashMap<>();
    }
    
    /**
     * Start the match
     */
    public void start() {
        if (state != MatchState.WAITING) {
            return;
        }
        
        state = MatchState.STARTING;
        
        // Initialize scores
        for (UUID player : players) {
            scores.put(player, 0);
            deaths.put(player, 0);
            kills.put(player, 0);
        }
        
        // Countdown and start
        broadcast(Component.literal("§6Match starting in 5 seconds!"));
        
        // Teleport players to arena
        teleportPlayers();
        
        // Start the match
        state = MatchState.IN_PROGRESS;
        startTime = System.currentTimeMillis();
        
        broadcast(Component.literal("§a=== FIGHT! ==="));
        
        DMZTournament.getLogger().debug("Match {} started with {} players", id, players.size());
    }
    
    /**
     * Teleport players to the arena
     */
    private void teleportPlayers() {
        // Teleport logic will be handled by the arena system
        // This is a placeholder
    }
    
    /**
     * Handle player death during match
     */
    public void onPlayerDeath(ServerPlayer player, DamageSource source) {
        if (state != MatchState.IN_PROGRESS) {
            return;
        }
        
        UUID playerId = player.getUUID();
        if (!players.contains(playerId)) {
            return;
        }
        
        deaths.put(playerId, deaths.getOrDefault(playerId, 0) + 1);
        
        // Check if killer is another player
        if (source.getEntity() instanceof ServerPlayer killer && 
            players.contains(killer.getUUID()) && 
            !killer.getUUID().equals(playerId)) {
            kills.put(killer.getUUID(), kills.getOrDefault(killer.getUUID(), 0) + 1);
            scores.put(killer.getUUID(), scores.getOrDefault(killer.getUUID(), 0) + 
                TournamentConfig.COMMON.KILL_POINTS.get());
        }
        
        broadcast(Component.literal("§c" + player.getName().getString() + " has been eliminated!"));
        
        // Check win conditions based on match type
        checkWinCondition();
    }
    
    /**
     * Check if the match has a winner
     */
    private void checkWinCondition() {
        switch (type) {
            case ONE_VS_ONE, TOURNAMENT -> checkOneVsOneWin();
            case FREE_FOR_ALL -> checkFreeForAllWin();
            case TWO_VS_TWO, THREE_VS_THREE -> checkTeamWin();
        }
    }
    
    private void checkOneVsOneWin() {
        // First to reach death limit loses
        for (UUID player : players) {
            if (deaths.getOrDefault(player, 0) >= TournamentConfig.COMMON.MAX_DEATHS.get()) {
                // This player lost
                loser = player;
                winner = players.stream()
                    .filter(p -> !p.equals(player))
                    .findFirst()
                    .orElse(null);
                finish();
                return;
            }
        }
    }
    
    private void checkFreeForAllWin() {
        // Last player standing wins
        List<UUID> alive = new ArrayList<>();
        for (UUID player : players) {
            if (deaths.getOrDefault(player, 0) < TournamentConfig.COMMON.MAX_DEATHS.get()) {
                alive.add(player);
            }
        }
        
        if (alive.size() == 1) {
            winner = alive.get(0);
            finish();
        }
    }
    
    private void checkTeamWin() {
        // Team-based win condition
        // Implementation depends on team structure
    }
    
    /**
     * Update match (called every tick)
     */
    public void tick() {
        if (state != MatchState.IN_PROGRESS) {
            return;
        }
        
        // Check time limit
        if (timeLimit > 0) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsed >= timeLimit) {
                // Time's up - determine winner by score
                determineWinnerByScore();
                finish();
            }
        }
    }
    
    /**
     * Determine winner based on scores when time runs out
     */
    private void determineWinnerByScore() {
        int highestScore = -1;
        UUID highestPlayer = null;
        
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                highestPlayer = entry.getKey();
            }
        }
        
        winner = highestPlayer;
    }
    
    /**
     * Finish the match
     */
    public void finish() {
        if (state == MatchState.FINISHED || state == MatchState.CANCELLED) {
            return;
        }
        
        state = MatchState.FINISHED;
        endTime = System.currentTimeMillis();
        
        if (winner != null) {
            ServerPlayer winnerPlayer = DMZTournament.getTournamentManager()
                .getServer().getPlayerList().getPlayer(winner);
            if (winnerPlayer != null) {
                broadcast(Component.literal("§6=== " + winnerPlayer.getName().getString() + " WINS! ==="));
            }
        } else {
            broadcast(Component.literal("§7=== Match Ended - No Winner ==="));
        }
        
        // Notify tournament manager
        DMZTournament.getTournamentManager().onMatchComplete(this);
        
        DMZTournament.getLogger().debug("Match {} finished. Winner: {}", id, winner);
    }
    
    /**
     * Cancel the match
     */
    public void cancel() {
        state = MatchState.CANCELLED;
        broadcast(Component.literal("§cMatch has been cancelled!"));
    }
    
    /**
     * Broadcast a message to all players in the match
     */
    public void broadcast(Component message) {
        for (UUID playerId : players) {
            ServerPlayer player = DMZTournament.getTournamentManager()
                .getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }
    
    /**
     * Get match description for display
     */
    public String getMatchDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("§7").append(type.getDisplayName()).append(": ");
        
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = DMZTournament.getTournamentManager()
                .getServer().getPlayerList().getPlayer(players.get(i));
            if (player != null) {
                sb.append("§e").append(player.getName().getString());
                if (i < players.size() - 1) {
                    sb.append(" §7vs ");
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Get elapsed time in seconds
     */
    public long getElapsedTime() {
        if (startTime == 0) return 0;
        long end = (state == MatchState.FINISHED || state == MatchState.CANCELLED) ? 
            endTime : System.currentTimeMillis();
        return (end - startTime) / 1000;
    }
    
    /**
     * Check if a player is in this match
     */
    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }
    
    /**
     * Get remaining time in seconds
     */
    public int getRemainingTime() {
        if (timeLimit <= 0) return -1;
        long elapsed = getElapsedTime();
        return (int) Math.max(0, timeLimit - elapsed);
    }
    
    // Getters
    public UUID getId() { return id; }
    public List<UUID> getPlayers() { return Collections.unmodifiableList(players); }
    public MatchType getType() { return type; }
    public MatchState getState() { return state; }
    public boolean isStarted() { return state == MatchState.IN_PROGRESS; }
    public boolean isFinished() { return state == MatchState.FINISHED; }
    public boolean isCancelled() { return state == MatchState.CANCELLED; }
    @Nullable public UUID getWinner() { return winner; }
    @Nullable public UUID getLoser() { return loser; }
    public int getScore(UUID playerId) { return scores.getOrDefault(playerId, 0); }
    public int getDeaths(UUID playerId) { return deaths.getOrDefault(playerId, 0); }
    public int getKills(UUID playerId) { return kills.getOrDefault(playerId, 0); }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}
