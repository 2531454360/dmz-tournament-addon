package com.dmztournament.common.rewards;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.config.TournamentConfig;
import com.dmztournament.common.tournament.Tournament;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages rewards for tournament winners.
 * Supports items, titles, and DMZ stat bonuses.
 */
public class RewardManager {
    private static final String DATA_FILE = "dmztournament_rewards.dat";
    
    private final MinecraftServer server;
    private final Map<String, TournamentReward> rewardPresets;
    private final Map<UUID, PlayerRewards> playerRewards;
    
    public RewardManager(MinecraftServer server) {
        this.server = server;
        this.rewardPresets = new HashMap<>();
        this.playerRewards = new HashMap<>();
        
        // Initialize default reward presets
        initializeDefaultPresets();
    }
    
    /**
     * Initialize default reward presets
     */
    private void initializeDefaultPresets() {
        // Standard tournament rewards
        TournamentReward standard = new TournamentReward("standard", "Standard Rewards");
        standard.setFirstPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 5),
            createRewardItem("minecraft:diamond", 10)
        ));
        standard.setSecondPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 3),
            createRewardItem("minecraft:diamond", 5)
        ));
        standard.setThirdPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 1),
            createRewardItem("minecraft:diamond", 2)
        ));
        standard.setFirstPlaceTitle("§6§lTournament Champion");
        standard.setSecondPlaceTitle("§7§lTournament Runner-up");
        standard.setThirdPlaceTitle("§c§lTournament Third Place");
        rewardPresets.put("standard", standard);
        
        // Premium tournament rewards
        TournamentReward premium = new TournamentReward("premium", "Premium Rewards");
        premium.setFirstPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 10),
            createRewardItem("minecraft:netherite_ingot", 3),
            createRewardItem("minecraft:enchanted_golden_apple", 5)
        ));
        premium.setSecondPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 6),
            createRewardItem("minecraft:netherite_ingot", 1),
            createRewardItem("minecraft:enchanted_golden_apple", 2)
        ));
        premium.setThirdPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:senzu_bean", 3),
            createRewardItem("minecraft:diamond_block", 2)
        ));
        premium.setFirstPlaceStatBonus(100); // DMZ stat points
        premium.setSecondPlaceStatBonus(50);
        premium.setThirdPlaceStatBonus(25);
        rewardPresets.put("premium", premium);
        
        // Special event rewards
        TournamentReward special = new TournamentReward("special", "Special Event Rewards");
        special.setFirstPlaceItems(Arrays.asList(
            createRewardItem("dragonminez:dragon_ball", 1),
            createRewardItem("minecraft:dragon_egg", 1)
        ));
        special.setFirstPlaceTitle("§6§lDragon Ball Master");
        rewardPresets.put("special", special);
    }
    
    /**
     * Create a reward item from string
     */
    private RewardItem createRewardItem(String itemId, int count) {
        return new RewardItem(itemId, count);
    }
    
    /**
     * Distribute rewards for a tournament
     */
    public void distributeRewards(Tournament tournament) {
        String presetName = TournamentConfig.COMMON.REWARD_PRESET.get();
        TournamentReward reward = rewardPresets.getOrDefault(presetName, rewardPresets.get("standard"));
        
        List<UUID> participants = tournament.getParticipants();
        Map<UUID, Integer> rankings = new HashMap<>();
        
        // Get rankings from tournament
        for (UUID playerId : participants) {
            rankings.put(playerId, tournament.getPlayerRank(playerId));
        }
        
        // Sort by rank
        List<Map.Entry<UUID, Integer>> sorted = rankings.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.comparingByValue())
            .toList();
        
        // Distribute rewards
        for (Map.Entry<UUID, Integer> entry : sorted) {
            UUID playerId = entry.getKey();
            int rank = entry.getValue();
            
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) continue;
            
            distributeRewardsToPlayer(player, rank, reward);
        }
        
        // Give participation rewards to everyone else
        for (UUID playerId : participants) {
            if (!rankings.containsKey(playerId) || rankings.get(playerId) <= 0) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player != null) {
                    distributeParticipationReward(player, reward);
                }
            }
        }
        
        DMZTournament.getLogger().info("Distributed rewards for tournament: {}", tournament.getName());
    }
    
    /**
     * Distribute rewards to a player based on their rank
     */
    private void distributeRewardsToPlayer(ServerPlayer player, int rank, TournamentReward reward) {
        List<RewardItem> items;
        String title;
        int statBonus;
        
        switch (rank) {
            case 1 -> {
                items = reward.getFirstPlaceItems();
                title = reward.getFirstPlaceTitle();
                statBonus = reward.getFirstPlaceStatBonus();
                player.sendSystemMessage(Component.literal("§6§l=== 1st PLACE! ==="));
            }
            case 2 -> {
                items = reward.getSecondPlaceItems();
                title = reward.getSecondPlaceTitle();
                statBonus = reward.getSecondPlaceStatBonus();
                player.sendSystemMessage(Component.literal("§7§l=== 2nd PLACE! ==="));
            }
            case 3 -> {
                items = reward.getThirdPlaceItems();
                title = reward.getThirdPlaceTitle();
                statBonus = reward.getThirdPlaceStatBonus();
                player.sendSystemMessage(Component.literal("§c§l=== 3rd PLACE! ==="));
            }
            default -> {
                distributeParticipationReward(player, reward);
                return;
            }
        }
        
        // Give items
        if (items != null) {
            for (RewardItem rewardItem : items) {
                ItemStack stack = rewardItem.createItemStack();
                if (!stack.isEmpty()) {
                    player.getInventory().add(stack);
                    player.sendSystemMessage(Component.literal("§a+ " + stack.getCount() + "x " + 
                        stack.getHoverName().getString()));
                }
            }
        }
        
        // Grant title (if title system is available)
        if (title != null && !title.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eTitle unlocked: " + title));
            // Store title for player
            getPlayerRewards(player.getUUID()).addTitle(title);
        }
        
        // Grant stat bonus (DMZ integration)
        if (statBonus > 0) {
            player.sendSystemMessage(Component.literal("§b+" + statBonus + " Tournament Points!"));
            getPlayerRewards(player.getUUID()).addTournamentPoints(statBonus);
        }
        
        // Update statistics
        getPlayerRewards(player.getUUID()).incrementTournamentsPlayed();
        if (rank == 1) {
            getPlayerRewards(player.getUUID()).incrementTournamentsWon();
        }
        
        saveData();
    }
    
    /**
     * Distribute participation reward
     */
    private void distributeParticipationReward(ServerPlayer player, TournamentReward reward) {
        player.sendSystemMessage(Component.literal("§7Thanks for participating!"));
        
        // Small participation reward
        ItemStack participationReward = new ItemStack(net.minecraft.world.item.Items.EMERALD, 3);
        player.getInventory().add(participationReward);
        
        getPlayerRewards(player.getUUID()).incrementTournamentsPlayed();
    }
    
    /**
     * Get or create player rewards data
     */
    public PlayerRewards getPlayerRewards(UUID playerId) {
        return playerRewards.computeIfAbsent(playerId, PlayerRewards::new);
    }
    
    /**
     * Get a reward preset
     */
    public Optional<TournamentReward> getRewardPreset(String name) {
        return Optional.ofNullable(rewardPresets.get(name));
    }
    
    /**
     * Add or update a reward preset
     */
    public void setRewardPreset(String name, TournamentReward reward) {
        rewardPresets.put(name, reward);
    }
    
    /**
     * Get all reward preset names
     */
    public Set<String> getRewardPresetNames() {
        return Collections.unmodifiableSet(rewardPresets.keySet());
    }
    
    /**
     * Save reward data to disk
     */
    public void saveData() {
        try {
            File dataFile = getDataFile();
            CompoundTag rootTag = new CompoundTag();
            
            // Save player rewards
            ListTag playerList = new ListTag();
            for (PlayerRewards rewards : playerRewards.values()) {
                playerList.add(rewards.serializeNBT());
            }
            rootTag.put("playerRewards", playerList);
            rootTag.putInt("version", 1);
            
            NbtIo.write(rootTag, dataFile);
            DMZTournament.getLogger().debug("Saved reward data to disk");
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to save reward data", e);
        }
    }
    
    /**
     * Load reward data from disk
     */
    public void loadData() {
        File dataFile = getDataFile();
        
        if (!dataFile.exists()) {
            return;
        }
        
        try {
            CompoundTag rootTag = NbtIo.read(dataFile);
            if (rootTag == null) return;
            
            int version = rootTag.getInt("version");
            if (version != 1) {
                DMZTournament.getLogger().warn("Reward data version mismatch: {}", version);
            }
            
            ListTag playerList = rootTag.getList("playerRewards", Tag.TAG_COMPOUND);
            for (int i = 0; i < playerList.size(); i++) {
                CompoundTag playerTag = playerList.getCompound(i);
                UUID playerId = playerTag.getUUID("playerId");
                PlayerRewards rewards = new PlayerRewards(playerId);
                rewards.deserializeNBT(playerTag);
                playerRewards.put(playerId, rewards);
            }
            
            DMZTournament.getLogger().info("Loaded reward data for {} players", playerRewards.size());
        } catch (IOException e) {
            DMZTournament.getLogger().error("Failed to load reward data", e);
        }
    }
    
    private File getDataFile() {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(DATA_FILE).toFile();
    }
}
