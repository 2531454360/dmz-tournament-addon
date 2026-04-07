package com.dmztournament.common.events;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.config.TournamentConfig;
import com.dmztournament.common.tournament.Match;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Event handlers for the tournament system.
 */
public class TournamentEvents {
    
    /**
     * Handle player death for match tracking
     */
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        // Check if player is in an active match
        Match match = DMZTournament.getTournamentManager().getPlayerMatch(player.getUUID());
        if (match != null) {
            DamageSource source = event.getSource();
            match.onPlayerDeath(player, source);
            
            if (TournamentConfig.COMMON.DEBUG_MODE.get()) {
                DMZTournament.getLogger().debug("Player {} died in match {}", 
                    player.getName().getString(), match.getId());
            }
        }
    }
    
    /**
     * Handle player logging out during a match
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        // Check if player is in an active match
        Match match = DMZTournament.getTournamentManager().getPlayerMatch(player.getUUID());
        if (match != null) {
            // Treat as a forfeit
            match.onPlayerDeath(player, player.level().damageSources().generic());
            
            DMZTournament.getLogger().info("Player {} logged out during match {} - treated as forfeit",
                player.getName().getString(), match.getId());
        }
        
        // Remove from tournament registration
        if (DMZTournament.getTournamentManager().getActiveTournament() != null) {
            var tournament = DMZTournament.getTournamentManager().getActiveTournament();
            if (tournament.isRegistered(player.getUUID())) {
                tournament.unregisterPlayer(player);
            }
        }
    }
    
    /**
     * Handle server tick for match updates
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        // Update tournament manager
        DMZTournament.getTournamentManager().tick();
    }
    
    /**
     * Handle player respawn (teleport back to arena if match is still active)
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        // Check if player is in an active match
        Match match = DMZTournament.getTournamentManager().getPlayerMatch(player.getUUID());
        if (match != null && !match.isFinished()) {
            // Player will respawn at their spawn point
            // The match system handles elimination based on death count
        }
    }
    
    /**
     * Handle player joining the server
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        // Welcome message if there's an active tournament
        var activeTournament = DMZTournament.getTournamentManager().getActiveTournament();
        if (activeTournament != null && 
            activeTournament.getState() == com.dmztournament.common.tournament.Tournament.TournamentState.REGISTRATION) {
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6§lA tournament is open for registration!"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7Use §e/tournament list §7to see details."));
        }
    }
}
