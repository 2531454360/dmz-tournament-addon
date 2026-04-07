package com.dmztournament.common.rewards;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tracks rewards and statistics for a player.
 */
public class PlayerRewards {
    private final UUID playerId;
    
    // Tournament statistics
    private int tournamentsPlayed;
    private int tournamentsWon;
    private int matchesPlayed;
    private int matchesWon;
    private int totalKills;
    private int totalDeaths;
    
    // Points and currency
    private int tournamentPoints;
    private int rankingPoints;
    
    // Unlocked titles
    private final List<String> unlockedTitles;
    private String currentTitle;
    
    // Achievement tracking
    private int winStreak;
    private int bestWinStreak;
    private long firstTournamentDate;
    private long lastTournamentDate;
    
    public PlayerRewards(UUID playerId) {
        this.playerId = playerId;
        this.unlockedTitles = new ArrayList<>();
        this.firstTournamentDate = -1;
        this.lastTournamentDate = -1;
    }
    
    /**
     * Add a title to the player's collection
     */
    public void addTitle(String title) {
        if (!unlockedTitles.contains(title)) {
            unlockedTitles.add(title);
        }
    }
    
    /**
     * Check if player has a title
     */
    public boolean hasTitle(String title) {
        return unlockedTitles.contains(title);
    }
    
    /**
     * Set the player's current display title
     */
    public void setCurrentTitle(String title) {
        if (unlockedTitles.contains(title) || title == null) {
            this.currentTitle = title;
        }
    }
    
    /**
     * Add tournament points
     */
    public void addTournamentPoints(int points) {
        this.tournamentPoints += points;
        this.rankingPoints += points;
    }
    
    /**
     * Spend tournament points
     */
    public boolean spendTournamentPoints(int points) {
        if (tournamentPoints >= points) {
            tournamentPoints -= points;
            return true;
        }
        return false;
    }
    
    /**
     * Increment tournaments played
     */
    public void incrementTournamentsPlayed() {
        tournamentsPlayed++;
        lastTournamentDate = System.currentTimeMillis();
        if (firstTournamentDate == -1) {
            firstTournamentDate = lastTournamentDate;
        }
    }
    
    /**
     * Increment tournaments won
     */
    public void incrementTournamentsWon() {
        tournamentsWon++;
        winStreak++;
        if (winStreak > bestWinStreak) {
            bestWinStreak = winStreak;
        }
    }
    
    /**
     * Record a loss (resets win streak)
     */
    public void recordLoss() {
        winStreak = 0;
    }
    
    /**
     * Increment matches played
     */
    public void incrementMatchesPlayed() {
        matchesPlayed++;
    }
    
    /**
     * Increment matches won
     */
    public void incrementMatchesWon() {
        matchesWon++;
    }
    
    /**
     * Add kills
     */
    public void addKills(int kills) {
        this.totalKills += kills;
    }
    
    /**
     * Add deaths
     */
    public void addDeaths(int deaths) {
        this.totalDeaths += deaths;
    }
    
    /**
     * Get win rate as a percentage
     */
    public double getWinRate() {
        if (tournamentsPlayed == 0) return 0.0;
        return (double) tournamentsWon / tournamentsPlayed * 100.0;
    }
    
    /**
     * Get K/D ratio
     */
    public double getKDRatio() {
        if (totalDeaths == 0) return totalKills;
        return (double) totalKills / totalDeaths;
    }
    
    /**
     * Serialize to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        tag.putUUID("playerId", playerId);
        tag.putInt("tournamentsPlayed", tournamentsPlayed);
        tag.putInt("tournamentsWon", tournamentsWon);
        tag.putInt("matchesPlayed", matchesPlayed);
        tag.putInt("matchesWon", matchesWon);
        tag.putInt("totalKills", totalKills);
        tag.putInt("totalDeaths", totalDeaths);
        tag.putInt("tournamentPoints", tournamentPoints);
        tag.putInt("rankingPoints", rankingPoints);
        tag.putInt("winStreak", winStreak);
        tag.putInt("bestWinStreak", bestWinStreak);
        tag.putLong("firstTournamentDate", firstTournamentDate);
        tag.putLong("lastTournamentDate", lastTournamentDate);
        
        ListTag titlesList = new ListTag();
        for (String title : unlockedTitles) {
            CompoundTag titleTag = new CompoundTag();
            titleTag.putString("title", title);
            titlesList.add(titleTag);
        }
        tag.put("unlockedTitles", titlesList);
        
        if (currentTitle != null) {
            tag.putString("currentTitle", currentTitle);
        }
        
        return tag;
    }
    
    /**
     * Deserialize from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.tournamentsPlayed = tag.getInt("tournamentsPlayed");
        this.tournamentsWon = tag.getInt("tournamentsWon");
        this.matchesPlayed = tag.getInt("matchesPlayed");
        this.matchesWon = tag.getInt("matchesWon");
        this.totalKills = tag.getInt("totalKills");
        this.totalDeaths = tag.getInt("totalDeaths");
        this.tournamentPoints = tag.getInt("tournamentPoints");
        this.rankingPoints = tag.getInt("rankingPoints");
        this.winStreak = tag.getInt("winStreak");
        this.bestWinStreak = tag.getInt("bestWinStreak");
        this.firstTournamentDate = tag.getLong("firstTournamentDate");
        this.lastTournamentDate = tag.getLong("lastTournamentDate");
        
        ListTag titlesList = tag.getList("unlockedTitles", Tag.TAG_COMPOUND);
        for (int i = 0; i < titlesList.size(); i++) {
            unlockedTitles.add(titlesList.getCompound(i).getString("title"));
        }
        
        if (tag.contains("currentTitle")) {
            this.currentTitle = tag.getString("currentTitle");
        }
    }
    
    // Getters
    public UUID getPlayerId() { return playerId; }
    public int getTournamentsPlayed() { return tournamentsPlayed; }
    public int getTournamentsWon() { return tournamentsWon; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public int getMatchesWon() { return matchesWon; }
    public int getTotalKills() { return totalKills; }
    public int getTotalDeaths() { return totalDeaths; }
    public int getTournamentPoints() { return tournamentPoints; }
    public int getRankingPoints() { return rankingPoints; }
    public List<String> getUnlockedTitles() { return new ArrayList<>(unlockedTitles); }
    public String getCurrentTitle() { return currentTitle; }
    public int getWinStreak() { return winStreak; }
    public int getBestWinStreak() { return bestWinStreak; }
    public long getFirstTournamentDate() { return firstTournamentDate; }
    public long getLastTournamentDate() { return lastTournamentDate; }
}
