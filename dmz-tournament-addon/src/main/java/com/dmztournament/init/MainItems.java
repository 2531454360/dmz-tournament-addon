package com.dmztournament.init;

import com.dmztournament.DMZTournament;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Item registration for the tournament addon.
 */
public class MainItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, DMZTournament.MOD_ID);
    
    // Tournament items
    public static final RegistryObject<Item> TOURNAMENT_TOKEN = ITEMS.register("tournament_token",
        () -> new Item(new Item.Properties()
            .stacksTo(64)));
    
    public static final RegistryObject<Item> CHAMPION_MEDAL = ITEMS.register("champion_medal",
        () -> new Item(new Item.Properties()
            .stacksTo(1)
            .fireResistant()));
    
    public static final RegistryObject<Item> ARENA_PASS = ITEMS.register("arena_pass",
        () -> new Item(new Item.Properties()
            .stacksTo(1)));
    
    // Reward items
    public static final RegistryObject<Item> TOURNAMENT_CHEST = ITEMS.register("tournament_chest",
        () -> new Item(new Item.Properties()
            .stacksTo(16)));
}
