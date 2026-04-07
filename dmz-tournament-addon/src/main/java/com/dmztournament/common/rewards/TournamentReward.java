package com.dmztournament.common.rewards;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines rewards for tournament placements.
 */
public class TournamentReward {
    private String id;
    private String name;
    private String description;
    
    // Item rewards
    private List<RewardItem> firstPlaceItems;
    private List<RewardItem> secondPlaceItems;
    private List<RewardItem> thirdPlaceItems;
    private List<RewardItem> participationItems;
    
    // Title rewards
    private String firstPlaceTitle;
    private String secondPlaceTitle;
    private String thirdPlaceTitle;
    private String participationTitle;
    
    // DMZ stat point rewards
    private int firstPlaceStatBonus;
    private int secondPlaceStatBonus;
    private int thirdPlaceStatBonus;
    private int participationStatBonus;
    
    // Experience rewards
    private int firstPlaceExp;
    private int secondPlaceExp;
    private int thirdPlaceExp;
    private int participationExp;
    
    public TournamentReward(String id, String name) {
        this.id = id;
        this.name = name;
        this.description = "";
        
        this.firstPlaceItems = new ArrayList<>();
        this.secondPlaceItems = new ArrayList<>();
        this.thirdPlaceItems = new ArrayList<>();
        this.participationItems = new ArrayList<>();
        
        this.firstPlaceStatBonus = 0;
        this.secondPlaceStatBonus = 0;
        this.thirdPlaceStatBonus = 0;
        this.participationStatBonus = 0;
        
        this.firstPlaceExp = 0;
        this.secondPlaceExp = 0;
        this.thirdPlaceExp = 0;
        this.participationExp = 0;
    }
    
    /**
     * Serialize to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("description", description);
        
        tag.put("firstPlaceItems", serializeItemList(firstPlaceItems));
        tag.put("secondPlaceItems", serializeItemList(secondPlaceItems));
        tag.put("thirdPlaceItems", serializeItemList(thirdPlaceItems));
        tag.put("participationItems", serializeItemList(participationItems));
        
        tag.putString("firstPlaceTitle", firstPlaceTitle != null ? firstPlaceTitle : "");
        tag.putString("secondPlaceTitle", secondPlaceTitle != null ? secondPlaceTitle : "");
        tag.putString("thirdPlaceTitle", thirdPlaceTitle != null ? thirdPlaceTitle : "");
        tag.putString("participationTitle", participationTitle != null ? participationTitle : "");
        
        tag.putInt("firstPlaceStatBonus", firstPlaceStatBonus);
        tag.putInt("secondPlaceStatBonus", secondPlaceStatBonus);
        tag.putInt("thirdPlaceStatBonus", thirdPlaceStatBonus);
        tag.putInt("participationStatBonus", participationStatBonus);
        
        tag.putInt("firstPlaceExp", firstPlaceExp);
        tag.putInt("secondPlaceExp", secondPlaceExp);
        tag.putInt("thirdPlaceExp", thirdPlaceExp);
        tag.putInt("participationExp", participationExp);
        
        return tag;
    }
    
    /**
     * Deserialize from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.id = tag.getString("id");
        this.name = tag.getString("name");
        this.description = tag.getString("description");
        
        this.firstPlaceItems = deserializeItemList(tag.getList("firstPlaceItems", Tag.TAG_COMPOUND));
        this.secondPlaceItems = deserializeItemList(tag.getList("secondPlaceItems", Tag.TAG_COMPOUND));
        this.thirdPlaceItems = deserializeItemList(tag.getList("thirdPlaceItems", Tag.TAG_COMPOUND));
        this.participationItems = deserializeItemList(tag.getList("participationItems", Tag.TAG_COMPOUND));
        
        this.firstPlaceTitle = tag.getString("firstPlaceTitle");
        if (firstPlaceTitle.isEmpty()) firstPlaceTitle = null;
        this.secondPlaceTitle = tag.getString("secondPlaceTitle");
        if (secondPlaceTitle.isEmpty()) secondPlaceTitle = null;
        this.thirdPlaceTitle = tag.getString("thirdPlaceTitle");
        if (thirdPlaceTitle.isEmpty()) thirdPlaceTitle = null;
        this.participationTitle = tag.getString("participationTitle");
        if (participationTitle.isEmpty()) participationTitle = null;
        
        this.firstPlaceStatBonus = tag.getInt("firstPlaceStatBonus");
        this.secondPlaceStatBonus = tag.getInt("secondPlaceStatBonus");
        this.thirdPlaceStatBonus = tag.getInt("thirdPlaceStatBonus");
        this.participationStatBonus = tag.getInt("participationStatBonus");
        
        this.firstPlaceExp = tag.getInt("firstPlaceExp");
        this.secondPlaceExp = tag.getInt("secondPlaceExp");
        this.thirdPlaceExp = tag.getInt("thirdPlaceExp");
        this.participationExp = tag.getInt("participationExp");
    }
    
    private ListTag serializeItemList(List<RewardItem> items) {
        ListTag list = new ListTag();
        for (RewardItem item : items) {
            list.add(item.serializeNBT());
        }
        return list;
    }
    
    private List<RewardItem> deserializeItemList(ListTag list) {
        List<RewardItem> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            RewardItem item = new RewardItem();
            item.deserializeNBT(list.getCompound(i));
            items.add(item);
        }
        return items;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<RewardItem> getFirstPlaceItems() { return firstPlaceItems; }
    public void setFirstPlaceItems(List<RewardItem> items) { this.firstPlaceItems = items; }
    public List<RewardItem> getSecondPlaceItems() { return secondPlaceItems; }
    public void setSecondPlaceItems(List<RewardItem> items) { this.secondPlaceItems = items; }
    public List<RewardItem> getThirdPlaceItems() { return thirdPlaceItems; }
    public void setThirdPlaceItems(List<RewardItem> items) { this.thirdPlaceItems = items; }
    public List<RewardItem> getParticipationItems() { return participationItems; }
    public void setParticipationItems(List<RewardItem> items) { this.participationItems = items; }
    public String getFirstPlaceTitle() { return firstPlaceTitle; }
    public void setFirstPlaceTitle(String title) { this.firstPlaceTitle = title; }
    public String getSecondPlaceTitle() { return secondPlaceTitle; }
    public void setSecondPlaceTitle(String title) { this.secondPlaceTitle = title; }
    public String getThirdPlaceTitle() { return thirdPlaceTitle; }
    public void setThirdPlaceTitle(String title) { this.thirdPlaceTitle = title; }
    public String getParticipationTitle() { return participationTitle; }
    public void setParticipationTitle(String title) { this.participationTitle = title; }
    public int getFirstPlaceStatBonus() { return firstPlaceStatBonus; }
    public void setFirstPlaceStatBonus(int bonus) { this.firstPlaceStatBonus = bonus; }
    public int getSecondPlaceStatBonus() { return secondPlaceStatBonus; }
    public void setSecondPlaceStatBonus(int bonus) { this.secondPlaceStatBonus = bonus; }
    public int getThirdPlaceStatBonus() { return thirdPlaceStatBonus; }
    public void setThirdPlaceStatBonus(int bonus) { this.thirdPlaceStatBonus = bonus; }
    public int getParticipationStatBonus() { return participationStatBonus; }
    public void setParticipationStatBonus(int bonus) { this.participationStatBonus = bonus; }
    public int getFirstPlaceExp() { return firstPlaceExp; }
    public void setFirstPlaceExp(int exp) { this.firstPlaceExp = exp; }
    public int getSecondPlaceExp() { return secondPlaceExp; }
    public void setSecondPlaceExp(int exp) { this.secondPlaceExp = exp; }
    public int getThirdPlaceExp() { return thirdPlaceExp; }
    public void setThirdPlaceExp(int exp) { this.thirdPlaceExp = exp; }
    public int getParticipationExp() { return participationExp; }
    public void setParticipationExp(int exp) { this.participationExp = exp; }
}
