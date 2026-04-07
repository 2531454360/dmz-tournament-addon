package com.dmztournament.common.rewards;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Represents an item reward.
 */
public class RewardItem {
    private String itemId;
    private int count;
    private CompoundTag nbt;
    
    public RewardItem() {
        this.itemId = "minecraft:air";
        this.count = 1;
    }
    
    public RewardItem(String itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }
    
    /**
     * Create an ItemStack from this reward
     */
    public ItemStack createItemStack() {
        ResourceLocation location = new ResourceLocation(itemId);
        Item item = BuiltInRegistries.ITEM.get(location);
        
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        
        ItemStack stack = new ItemStack(item, count);
        if (nbt != null) {
            stack.setTag(nbt.copy());
        }
        
        return stack;
    }
    
    /**
     * Serialize to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", itemId);
        tag.putInt("count", count);
        if (nbt != null) {
            tag.put("nbt", nbt.copy());
        }
        return tag;
    }
    
    /**
     * Deserialize from NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.itemId = tag.getString("itemId");
        this.count = tag.getInt("count");
        if (tag.contains("nbt")) {
            this.nbt = tag.getCompound("nbt");
        }
    }
    
    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public CompoundTag getNbt() { return nbt; }
    public void setNbt(CompoundTag nbt) { this.nbt = nbt; }
}
