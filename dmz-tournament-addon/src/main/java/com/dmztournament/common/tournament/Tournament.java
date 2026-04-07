package com.dmztournament.common.tournament;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.arena.Arena;
import com.dmztournament.common.config.TournamentConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a tournament with multiple participants.
 * Supports various tournament formats and bracket systems.
 */
public class Tournament {
    private final UUID id;
    private String name;
    private String description;
    private UUID hostId;
    private String hostName;
    
    // Tournament settings
    private TournamentFormat format;
    private TournamentRules rules;
    private int maxParticipants;
    private int minParticipants;
    private long startTime;
    private long registrationEndTime;
    
    // Tournament state
    private TournamentState state;
    private final List<UUID> participants;
    private final List<UUID> eliminated;
    private final List<Match> matches;
    private final Map<UUID, Integer> scores;
    private final Map<UUID, Integer> rankings;
    
    // Current match
    @Nullable
    private Match currentMatch;
    @Nullable
    private UUID arenaId;
    
    // Statistics
    private long createdTime;
    private long endedTime;
    
    public enum TournamentState {
        SETUP("setup", "Setting Up"),
        REGISTRATION("registration", "Open for Registration"),
        STARTING("starting", "Starting Soon"),
        IN_PROGRESS("in_progress", "In Progress"),
        PAUSED("paused", "Paused"),
        FINISHED("finished", "Finished"),
        CANCELLED("cancelled", "Cancelled");
        
        private final String id;
        private final String displayName;
        
        TournamentState(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum TournamentFormat {
        SINGLE_ELIMINATION("single_elimination", "Single Elimination", "Players are eliminated after one loss"),
        DOUBLE_ELIMINATION("double_elimination", "Double Elimination", "Players are eliminated after two losses"),
        ROUND_ROBIN("round_robin", "Round Robin", "Everyone fights everyone"),
        SWISS("swiss", "Swiss System", "Players are paired based on performance"),
        FREE_FOR_ALL("free_for_all", "Free For All", "Battle Royale style");
        
        private final String id;
        private final String displayName;
        private final String description;
        
        TournamentFormat(String id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        
        public static TournamentFormat fromId(String id) {
            for (TournamentFormat format : values()) {
                if (format.id.equalsIgnoreCase(id)) {
                    return format;
                }
            }
            return SINGLE_ELIMINATION;
        }
    }
    
    public Tournament(String name, ServerPlayer host) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.hostId = host.getUUID();
        this.hostName = host.getName().getString();
        
        this.format = TournamentFormat.SINGLE_ELIMINATION;
        this.rules = new TournamentRules();
        this.maxParticipants = TournamentConfig.COMMON.DEFAULT_MAX_PARTICIPANTS.get();
        this.minParticipants = 2;
        
        this.state = TournamentState.SETUP;
        this.participants = new ArrayList<>();
        this.eliminated = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.scores = new HashMap<>();
        this.rankings = new HashMap<>();
        
        this.createdTime = System.currentTimeMillis();
    }
    
    public Tournament(UUID id) {
        this.id = id;
        this.participants = new ArrayList<>();
        this.eliminated = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.scores = new HashMap<>();
        this.rankings = new HashMap<>();
    }
    
    /**
     * Register a player for the tournament
     */
    public boolean registerPlayer(ServerPlayer player) {
        if (state != TournamentState.REGISTRATION) {
            return false;
        }
        
        if (participants.size() >= maxParticipants) {
            player.sendSystemMessage(Component.literal("§cTournament is full!"));
            return false;
        }
        
        if (participants.contains(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§cYou are already registered!"));
            return false;
        }
        
        participants.add(player.getUUID());
        scores.put(player.getUUID(), 0);
        player.sendSystemMessage(Component.literal("§aYou have registered for " + name + "!"));
        
        broadcast(Component.literal("§e" + player.getName().getString() + " §7has registered! (" + 
            participants.size() + "/" + maxParticipants + ")"));
        
        return true;
    }
    
    /**
     * Unregister a player from the tournament
     */
    public boolean unregisterPlayer(ServerPlayer player) {
        if (state != TournamentState.REGISTRATION && state != TournamentState.SETUP) {
            return false;
        }
        
        if (!participants.remove(player.getUUID())) {
            return false;
        }
        
        scores.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("§aYou have left the tournament!"));
        
        broadcast(Component.literal("§e" + player.getName().getString() + " §7has left the tournament."));
        
        return true;
    }
    
    /**
     * Start the tournament
     */
    public boolean start() {
        if (state != TournamentState.REGISTRATION) {
            return false;
        }
        
        if (participants.size() < minParticipants) {
            broadcast(Component.literal("§cNot enough participants to start! Need at least " + minParticipants));
            return false;
        }
        
        state = TournamentState.STARTING;
        generateBracket();
        
        broadcast(Component.literal("§6=== Tournament Starting! ==="));
        broadcast(Component.literal("§7Format: §e" + format.getDisplayName()));
        broadcast(Component.literal("§7Participants: §e" + participants.size()));
        
        // Start first match after delay
        state = TournamentState.IN_PROGRESS;
        startNextMatch();
        
        return true;
    }
    
    /**
     * Generate tournament bracket based on format
     */
    private void generateBracket() {
        matches.clear();
        List<UUID> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled);
        
        switch (format) {
            case SINGLE_ELIMINATION -> generateSingleElimination(shuffled);
            case DOUBLE_ELIMINATION -> generateDoubleElimination(shuffled);
            case ROUND_ROBIN -> generateRoundRobin(shuffled);
            case SWISS -> generateSwiss(shuffled);
            case FREE_FOR_ALL -> generateFreeForAll(shuffled);
        }
    }
    
    private void generateSingleElimination(List<UUID> players) {
        // Create initial matches
        for (int i = 0; i < players.size(); i += 2) {
            if (i + 1 < players.size()) {
                Match match = new Match(players.get(i), players.get(i + 1), Match.MatchType.TOURNAMENT);
                matches.add(match);
            } else {
                // Odd player gets a bye
                scores.put(players.get(i), scores.getOrDefault(players.get(i), 0) + 1);
            }
        }
    }
    
    private void generateDoubleElimination(List<UUID> players) {
        // Similar to single but with losers bracket
        generateSingleElimination(players);
    }
    
    private void generateRoundRobin(List<UUID> players) {
        // Everyone fights everyone
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Match match = new Match(players.get(i), players.get(j), Match.MatchType.TOURNAMENT);
                matches.add(match);
            }
        }
    }
    
    private void generateSwiss(List<UUID> players) {
        // First round is random
        generateSingleElimination(players);
    }
    
    private void generateFreeForAll(List<UUID> players) {
        // All players in one match
        Match match = new Match(players, Match.MatchType.FREE_FOR_ALL);
        matches.add(match);
    }
    
    /**
     * Start the next match in the tournament
     */
    public void startNextMatch() {
        if (currentMatch != null && !currentMatch.isFinished()) {
            return;
        }
        
        Optional<Match> nextMatch = matches.stream()
            .filter(m -> !m.isStarted() && !m.isFinished())
            .findFirst();
        
        if (nextMatch.isPresent()) {
            currentMatch = nextMatch.get();
            currentMatch.start();
            
            broadcast(Component.literal("§6=== Next Match ==="));
            broadcast(Component.literal(currentMatch.getMatchDescription()));
        } else {
            endTournament();
        }
    }
    
    /**
     * End the tournament and calculate final rankings
     */
    public void endTournament() {
        state = TournamentState.FINISHED;
        endedTime = System.currentTimeMillis();
        
        // Calculate rankings based on scores
        List<Map.Entry<UUID, Integer>> sorted = scores.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .toList();
        
        for (int i = 0; i < sorted.size(); i++) {
            rankings.put(sorted.get(i).getKey(), i + 1);
        }
        
        // Announce winners
        broadcast(Component.literal("§6=== Tournament Finished! ==="));
        broadcast(Component.literal("§eFinal Rankings:"));
        
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            String medal = i == 0 ? "§6🥇" : i == 1 ? "§7🥈" : "§c🥉";
            broadcast(Component.literal(medal + " #" + (i + 1) + " - " + sorted.get(i).getKey()));
        }
        
        // Distribute rewards
        distributeRewards();
        
        DMZTournament.getLogger().info("Tournament '{}' finished with {} participants", name, participants.size());
    }
    
    /**
     * Distribute rewards to winners
     */
    private void distributeRewards() {
        // Rewards will be handled by the reward system
        DMZTournament.getTournamentManager().distributeRewards(this);
    }
    
    /**
     * Handle match completion
     */
    public void onMatchComplete(Match match) {
        if (match.getWinner() != null) {
            UUID winner = match.getWinner();
            scores.put(winner, scores.getOrDefault(winner, 0) + TournamentConfig.COMMON.WIN_POINTS.get());
            
            if (match.getLoser() != null) {
                eliminated.add(match.getLoser());
            }
        }
        
        // Start next match after delay
        startNextMatch();
    }
    
    /**
     * Broadcast a message to all participants
     */
    public void broadcast(Component message) {
        for (UUID playerId : participants) {
            Player player = DMZTournament.getTournamentManager().getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }
    
    /**
     * Cancel the tournament
     */
    public void cancel() {
        state = TournamentState.CANCELLED;
        broadcast(Component.literal("§cTournament has been cancelled!"));
        DMZTournament.getLogger().info("Tournament '{}' was cancelled", name);
    }
    
    /**
     * Check if a player is registered
     */
    public boolean isRegistered(UUID playerId) {
        return participants.contains(playerId);
    }
    
    /**
     * Get player's current rank
     */
    public int getPlayerRank(UUID playerId) {
        return rankings.getOrDefault(playerId, -1);
    }
    
    /**
     * Get player's current score
     */
    public int getPlayerScore(UUID playerId) {
        return scores.getOrDefault(playerId, 0);
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getHostId() { return hostId; }
    public String getHostName() { return hostName; }
    public TournamentState getState() { return state; }
    public void setState(TournamentState state) { this.state = state; }
    public TournamentFormat getFormat() { return format; }
    public void setFormat(TournamentFormat format) { this.format = format; }
    public TournamentRules getRules() { return rules; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int max) { this.maxParticipants = max; }
    public int getParticipantCount() { return participants.size(); }
    public List<UUID> getParticipants() { return Collections.unmodifiableList(participants); }
    public List<UUID> getEliminated() { return Collections.unmodifiableList(eliminated); }
    public Match getCurrentMatch() { return currentMatch; }
    public UUID getArenaId() { return arenaId; }
    public void setArenaId(UUID arenaId) { this.arenaId = arenaId; }
    public long getCreatedTime() { return createdTime; }
    public long getEndedTime() { return endedTime; }
    public List<Match> getMatches() { return Collections.unmodifiableList(matches); }
    
    /**
     * Serialize tournament to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.putString("description", description != null ? description : "");
        tag.putUUID("hostId", hostId);
        tag.putString("hostName", hostName);
        
        tag.putString("format", format.getId());
        tag.put("rules", rules.serializeNBT());
        tag.putInt("maxParticipants", maxParticipants);
        tag.putInt("minParticipants", minParticipants);
        tag.putLong("startTime", startTime);
        tag.putLong("registrationEndTime", registrationEndTime);
        
        tag.putString("state", state.getId());
        
        ListTag participantsList = new ListTag();
        for (UUID playerId : participants) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("id", playerId);
            participantsList.add(playerTag);
        }
        tag.put("participants", participantsList);
        
        ListTag eliminatedList = new ListTag();
        for (UUID playerId : eliminated) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("id", playerId);
            eliminatedList.add(playerTag);
        }
        tag.put("eliminated", eliminatedList);
        
        CompoundTag scoresTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            scoresTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("scores", scoresTag);
        
        CompoundTag rankingsTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : rankings.entrySet()) {
            rankingsTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("rankings", rankingsTag);
        
        if (arenaId != null) {
            tag.putUUID("arenaId", arenaId);
        }
        
        tag.putLong("createdTime", createdTime);
        tag.putLong("endedTime", endedTime);
        
        return tag;
    }
    
    /**
     * Deserialize tournament from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.name = tag.getString("name");
        this.description = tag.getString("description");
        if (this.description.isEmpty()) this.description = null;
        
        this.hostId = tag.getUUID("hostId");
        this.hostName = tag.getString("hostName");
        
        this.format = TournamentFormat.fromId(tag.getString("format"));
        this.rules = new TournamentRules();
        this.rules.deserializeNBT(tag.getCompound("rules"));
        
        this.maxParticipants = tag.getInt("maxParticipants");
        this.minParticipants = tag.getInt("minParticipants");
        this.startTime = tag.getLong("startTime");
        this.registrationEndTime = tag.getLong("registrationEndTime");
        
        this.state = TournamentState.valueOf(tag.getString("state").toUpperCase());
        
        ListTag participantsList = tag.getList("participants", Tag.TAG_COMPOUND);
        for (int i = 0; i < participantsList.size(); i++) {
            participants.add(participantsList.getCompound(i).getUUID("id"));
        }
        
        ListTag eliminatedList = tag.getList("eliminated", Tag.TAG_COMPOUND);
        for (int i = 0; i < eliminatedList.size(); i++) {
            eliminated.add(eliminatedList.getCompound(i).getUUID("id"));
        }
        
        CompoundTag scoresTag = tag.getCompound("scores");
        for (String key : scoresTag.getAllKeys()) {
            scores.put(UUID.fromString(key), scoresTag.getInt(key));
        }
        
        CompoundTag rankingsTag = tag.getCompound("rankings");
        for (String key : rankingsTag.getAllKeys()) {
            rankings.put(UUID.fromString(key), rankingsTag.getInt(key));
        }
        
        if (tag.contains("arenaId")) {
            this.arenaId = tag.getUUID("arenaId");
        }
        
        this.createdTime = tag.getLong("createdTime");
        this.endedTime = tag.getLong("endedTime");
    }
}
