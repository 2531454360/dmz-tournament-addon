package com.dmztournament.common.tournament;

import net.minecraft.nbt.CompoundTag;

/**
 * Defines rules for a tournament.
 * Includes settings for forms, items, time limits, etc.
 */
public class TournamentRules {
    // Form restrictions
    private boolean allowSuperSaiyan;
    private boolean allowKaioken;
    private boolean allowMajin;
    private boolean allowUltraInstinct;
    private boolean allowForms;
    private int maxFormLevel;
    
    // Item restrictions
    private boolean allowItems;
    private boolean allowHealingItems;
    private boolean allowWeapons;
    private boolean allowArmor;
    
    // Combat rules
    private boolean allowKiAttacks;
    private boolean allowMelee;
    private boolean allowFlight;
    private boolean allowTeleportation;
    
    // Time settings
    private int matchTimeLimit; // seconds, -1 for no limit
    private int roundTimeLimit; // seconds, -1 for no limit
    private int breakTime; // seconds between matches
    
    // Scoring
    private int pointsPerKill;
    private int pointsPerWin;
    private int pointsPerDraw;
    private int maxDeaths;
    
    // Other
    private boolean allowSpectators;
    private boolean showHealthBars;
    private boolean showScores;
    private boolean autoBalance;
    
    public TournamentRules() {
        // Default rules - everything allowed
        this.allowSuperSaiyan = true;
        this.allowKaioken = true;
        this.allowMajin = true;
        this.allowUltraInstinct = true;
        this.allowForms = true;
        this.maxFormLevel = -1; // No limit
        
        this.allowItems = true;
        this.allowHealingItems = true;
        this.allowWeapons = true;
        this.allowArmor = true;
        
        this.allowKiAttacks = true;
        this.allowMelee = true;
        this.allowFlight = true;
        this.allowTeleportation = true;
        
        this.matchTimeLimit = 300; // 5 minutes
        this.roundTimeLimit = -1;
        this.breakTime = 30; // 30 seconds
        
        this.pointsPerKill = 1;
        this.pointsPerWin = 3;
        this.pointsPerDraw = 1;
        this.maxDeaths = 3;
        
        this.allowSpectators = true;
        this.showHealthBars = true;
        this.showScores = true;
        this.autoBalance = false;
    }
    
    /**
     * Create rules for a standard tournament
     */
    public static TournamentRules standardRules() {
        return new TournamentRules();
    }
    
    /**
     * Create rules for a restricted tournament (no forms)
     */
    public static TournamentRules restrictedRules() {
        TournamentRules rules = new TournamentRules();
        rules.allowSuperSaiyan = false;
        rules.allowKaioken = false;
        rules.allowMajin = false;
        rules.allowUltraInstinct = false;
        rules.allowForms = false;
        return rules;
    }
    
    /**
     * Create rules for a base form only tournament
     */
    public static TournamentRules baseFormOnlyRules() {
        TournamentRules rules = new TournamentRules();
        rules.allowSuperSaiyan = false;
        rules.allowKaioken = false;
        rules.allowMajin = false;
        rules.allowUltraInstinct = false;
        rules.allowForms = false;
        rules.allowHealingItems = false;
        rules.matchTimeLimit = 180; // 3 minutes
        return rules;
    }
    
    /**
     * Create rules for an ultimate tournament
     */
    public static TournamentRules ultimateRules() {
        TournamentRules rules = new TournamentRules();
        rules.allowItems = false;
        rules.allowHealingItems = false;
        rules.matchTimeLimit = 600; // 10 minutes
        rules.maxDeaths = 1; // Single elimination per match
        return rules;
    }
    
    /**
     * Serialize to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        tag.putBoolean("allowSuperSaiyan", allowSuperSaiyan);
        tag.putBoolean("allowKaioken", allowKaioken);
        tag.putBoolean("allowMajin", allowMajin);
        tag.putBoolean("allowUltraInstinct", allowUltraInstinct);
        tag.putBoolean("allowForms", allowForms);
        tag.putInt("maxFormLevel", maxFormLevel);
        
        tag.putBoolean("allowItems", allowItems);
        tag.putBoolean("allowHealingItems", allowHealingItems);
        tag.putBoolean("allowWeapons", allowWeapons);
        tag.putBoolean("allowArmor", allowArmor);
        
        tag.putBoolean("allowKiAttacks", allowKiAttacks);
        tag.putBoolean("allowMelee", allowMelee);
        tag.putBoolean("allowFlight", allowFlight);
        tag.putBoolean("allowTeleportation", allowTeleportation);
        
        tag.putInt("matchTimeLimit", matchTimeLimit);
        tag.putInt("roundTimeLimit", roundTimeLimit);
        tag.putInt("breakTime", breakTime);
        
        tag.putInt("pointsPerKill", pointsPerKill);
        tag.putInt("pointsPerWin", pointsPerWin);
        tag.putInt("pointsPerDraw", pointsPerDraw);
        tag.putInt("maxDeaths", maxDeaths);
        
        tag.putBoolean("allowSpectators", allowSpectators);
        tag.putBoolean("showHealthBars", showHealthBars);
        tag.putBoolean("showScores", showScores);
        tag.putBoolean("autoBalance", autoBalance);
        
        return tag;
    }
    
    /**
     * Deserialize from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        allowSuperSaiyan = tag.getBoolean("allowSuperSaiyan");
        allowKaioken = tag.getBoolean("allowKaioken");
        allowMajin = tag.getBoolean("allowMajin");
        allowUltraInstinct = tag.getBoolean("allowUltraInstinct");
        allowForms = tag.getBoolean("allowForms");
        maxFormLevel = tag.getInt("maxFormLevel");
        
        allowItems = tag.getBoolean("allowItems");
        allowHealingItems = tag.getBoolean("allowHealingItems");
        allowWeapons = tag.getBoolean("allowWeapons");
        allowArmor = tag.getBoolean("allowArmor");
        
        allowKiAttacks = tag.getBoolean("allowKiAttacks");
        allowMelee = tag.getBoolean("allowMelee");
        allowFlight = tag.getBoolean("allowFlight");
        allowTeleportation = tag.getBoolean("allowTeleportation");
        
        matchTimeLimit = tag.getInt("matchTimeLimit");
        roundTimeLimit = tag.getInt("roundTimeLimit");
        breakTime = tag.getInt("breakTime");
        
        pointsPerKill = tag.getInt("pointsPerKill");
        pointsPerWin = tag.getInt("pointsPerWin");
        pointsPerDraw = tag.getInt("pointsPerDraw");
        maxDeaths = tag.getInt("maxDeaths");
        
        allowSpectators = tag.getBoolean("allowSpectators");
        showHealthBars = tag.getBoolean("showHealthBars");
        showScores = tag.getBoolean("showScores");
        autoBalance = tag.getBoolean("autoBalance");
    }
    
    // Getters and Setters
    public boolean isAllowSuperSaiyan() { return allowSuperSaiyan; }
    public void setAllowSuperSaiyan(boolean allow) { this.allowSuperSaiyan = allow; }
    public boolean isAllowKaioken() { return allowKaioken; }
    public void setAllowKaioken(boolean allow) { this.allowKaioken = allow; }
    public boolean isAllowMajin() { return allowMajin; }
    public void setAllowMajin(boolean allow) { this.allowMajin = allow; }
    public boolean isAllowUltraInstinct() { return allowUltraInstinct; }
    public void setAllowUltraInstinct(boolean allow) { this.allowUltraInstinct = allow; }
    public boolean isAllowForms() { return allowForms; }
    public void setAllowForms(boolean allow) { this.allowForms = allow; }
    public int getMaxFormLevel() { return maxFormLevel; }
    public void setMaxFormLevel(int level) { this.maxFormLevel = level; }
    public boolean isAllowItems() { return allowItems; }
    public void setAllowItems(boolean allow) { this.allowItems = allow; }
    public boolean isAllowHealingItems() { return allowHealingItems; }
    public void setAllowHealingItems(boolean allow) { this.allowHealingItems = allow; }
    public boolean isAllowWeapons() { return allowWeapons; }
    public void setAllowWeapons(boolean allow) { this.allowWeapons = allow; }
    public boolean isAllowArmor() { return allowArmor; }
    public void setAllowArmor(boolean allow) { this.allowArmor = allow; }
    public boolean isAllowKiAttacks() { return allowKiAttacks; }
    public void setAllowKiAttacks(boolean allow) { this.allowKiAttacks = allow; }
    public boolean isAllowMelee() { return allowMelee; }
    public void setAllowMelee(boolean allow) { this.allowMelee = allow; }
    public boolean isAllowFlight() { return allowFlight; }
    public void setAllowFlight(boolean allow) { this.allowFlight = allow; }
    public boolean isAllowTeleportation() { return allowTeleportation; }
    public void setAllowTeleportation(boolean allow) { this.allowTeleportation = allow; }
    public int getMatchTimeLimit() { return matchTimeLimit; }
    public void setMatchTimeLimit(int seconds) { this.matchTimeLimit = seconds; }
    public int getRoundTimeLimit() { return roundTimeLimit; }
    public void setRoundTimeLimit(int seconds) { this.roundTimeLimit = seconds; }
    public int getBreakTime() { return breakTime; }
    public void setBreakTime(int seconds) { this.breakTime = seconds; }
    public int getPointsPerKill() { return pointsPerKill; }
    public void setPointsPerKill(int points) { this.pointsPerKill = points; }
    public int getPointsPerWin() { return pointsPerWin; }
    public void setPointsPerWin(int points) { this.pointsPerWin = points; }
    public int getPointsPerDraw() { return pointsPerDraw; }
    public void setPointsPerDraw(int points) { this.pointsPerDraw = points; }
    public int getMaxDeaths() { return maxDeaths; }
    public void setMaxDeaths(int deaths) { this.maxDeaths = deaths; }
    public boolean isAllowSpectators() { return allowSpectators; }
    public void setAllowSpectators(boolean allow) { this.allowSpectators = allow; }
    public boolean isShowHealthBars() { return showHealthBars; }
    public void setShowHealthBars(boolean show) { this.showHealthBars = show; }
    public boolean isShowScores() { return showScores; }
    public void setShowScores(boolean show) { this.showScores = show; }
    public boolean isAutoBalance() { return autoBalance; }
    public void setAutoBalance(boolean auto) { this.autoBalance = auto; }
}
