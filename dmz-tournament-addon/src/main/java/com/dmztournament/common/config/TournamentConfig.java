package com.dmztournament.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuration for the tournament addon.
 */
public class TournamentConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }
    
    public static class Common {
        // Tournament settings
        public final ForgeConfigSpec.IntValue DEFAULT_MAX_PARTICIPANTS;
        public final ForgeConfigSpec.IntValue MIN_PARTICIPANTS;
        public final ForgeConfigSpec.IntValue REGISTRATION_TIME;
        
        // Match settings
        public final ForgeConfigSpec.IntValue MATCH_TIME_LIMIT;
        public final ForgeConfigSpec.IntValue MAX_DEATHS;
        public final ForgeConfigSpec.IntValue BREAK_TIME;
        
        // Scoring
        public final ForgeConfigSpec.IntValue WIN_POINTS;
        public final ForgeConfigSpec.IntValue KILL_POINTS;
        public final ForgeConfigSpec.IntValue PARTICIPATION_POINTS;
        
        // Rewards
        public final ForgeConfigSpec.ConfigValue<String> REWARD_PRESET;
        public final ForgeConfigSpec.BooleanValue ENABLE_STAT_BONUSES;
        public final ForgeConfigSpec.BooleanValue ENABLE_TITLES;
        
        // Integration
        public final ForgeConfigSpec.BooleanValue INTEGRATE_DMZ_STATS;
        public final ForgeConfigSpec.BooleanValue INTEGRATE_DMZ_FORMS;
        
        // Debug
        public final ForgeConfigSpec.BooleanValue DEBUG_MODE;
        
        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("tournament");
            
            DEFAULT_MAX_PARTICIPANTS = builder
                .comment("Default maximum number of participants in a tournament")
                .defineInRange("defaultMaxParticipants", 16, 2, 64);
            
            MIN_PARTICIPANTS = builder
                .comment("Minimum number of participants required to start a tournament")
                .defineInRange("minParticipants", 2, 2, 64);
            
            REGISTRATION_TIME = builder
                .comment("Time in seconds for registration period (-1 for unlimited)")
                .defineInRange("registrationTime", -1, -1, 3600);
            
            builder.pop();
            
            builder.push("match");
            
            MATCH_TIME_LIMIT = builder
                .comment("Time limit for each match in seconds (-1 for no limit)")
                .defineInRange("matchTimeLimit", 300, -1, 3600);
            
            MAX_DEATHS = builder
                .comment("Maximum deaths before a player is eliminated")
                .defineInRange("maxDeaths", 3, 1, 10);
            
            BREAK_TIME = builder
                .comment("Break time between matches in seconds")
                .defineInRange("breakTime", 30, 0, 300);
            
            builder.pop();
            
            builder.push("scoring");
            
            WIN_POINTS = builder
                .comment("Points awarded for winning a match")
                .defineInRange("winPoints", 3, 0, 100);
            
            KILL_POINTS = builder
                .comment("Points awarded per kill")
                .defineInRange("killPoints", 1, 0, 10);
            
            PARTICIPATION_POINTS = builder
                .comment("Points awarded for participating")
                .defineInRange("participationPoints", 1, 0, 10);
            
            builder.pop();
            
            builder.push("rewards");
            
            REWARD_PRESET = builder
                .comment("Default reward preset to use (standard, premium, special)")
                .define("rewardPreset", "standard");
            
            ENABLE_STAT_BONUSES = builder
                .comment("Enable DMZ stat point bonuses for winners")
                .define("enableStatBonuses", true);
            
            ENABLE_TITLES = builder
                .comment("Enable title rewards for winners")
                .define("enableTitles", true);
            
            builder.pop();
            
            builder.push("integration");
            
            INTEGRATE_DMZ_STATS = builder
                .comment("Enable integration with DragonMineZ stats system")
                .define("integrateDMZStats", true);
            
            INTEGRATE_DMZ_FORMS = builder
                .comment("Enable integration with DragonMineZ transformation forms")
                .define("integrateDMZForms", true);
            
            builder.pop();
            
            builder.push("debug");
            
            DEBUG_MODE = builder
                .comment("Enable debug logging")
                .define("debugMode", false);
            
            builder.pop();
        }
    }
}
