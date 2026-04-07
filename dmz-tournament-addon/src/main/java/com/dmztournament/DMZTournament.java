package com.dmztournament;

import com.dmztournament.common.arena.ArenaManager;
import com.dmztournament.common.commands.TournamentCommands;
import com.dmztournament.common.config.TournamentConfig;
import com.dmztournament.common.events.TournamentEvents;
import com.dmztournament.common.network.PacketHandler;
import com.dmztournament.common.tournament.TournamentManager;
import com.dmztournament.init.MainItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DMZ Tournament Addon - A battle arena and tournament system for DragonMineZ
 * 
 * Features:
 * - Create custom battle arenas
 * - Host tournaments with multiple players
 * - Ranking system with ELO-style ratings
 * - Unique rewards for tournament winners
 * - Integration with DragonMineZ stats system
 */
@Mod(DMZTournament.MOD_ID)
public class DMZTournament {
    public static final String MOD_ID = "dmztournament";
    public static final String MOD_NAME = "DMZ Tournament Addon";
    public static final String VERSION = "1.0.0";
    
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    private static ArenaManager arenaManager;
    private static TournamentManager tournamentManager;
    
    public DMZTournament() {
        LOGGER.info("Initializing {} v{}", MOD_NAME, VERSION);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TournamentConfig.COMMON_SPEC);
        
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        
        // Register items
        MainItems.ITEMS.register(modEventBus);
        
        // Register event listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
        forgeEventBus.register(this);
        forgeEventBus.register(new TournamentEvents());
        
        LOGGER.info("{} initialization complete!", MOD_NAME);
    }
    
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup starting...");
        
        event.enqueueWork(() -> {
            // Initialize network packet handler
            PacketHandler.register();
            
            LOGGER.info("Common setup complete!");
        });
    }
    
    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup starting...");
        // Client-side setup (renderers, keybinds, etc.)
        LOGGER.info("Client setup complete!");
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting - initializing tournament systems...");
        
        arenaManager = new ArenaManager(event.getServer());
        tournamentManager = new TournamentManager(event.getServer());
        
        // Load saved data
        arenaManager.loadData();
        tournamentManager.loadData();
        
        LOGGER.info("Tournament systems initialized!");
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server stopping - saving tournament data...");
        
        if (arenaManager != null) {
            arenaManager.saveData();
        }
        if (tournamentManager != null) {
            tournamentManager.saveData();
        }
        
        LOGGER.info("Tournament data saved!");
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering tournament commands...");
        TournamentCommands.register(event.getDispatcher());
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public static ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public static TournamentManager getTournamentManager() {
        return tournamentManager;
    }
}
