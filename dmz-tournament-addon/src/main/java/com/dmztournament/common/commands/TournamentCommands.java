package com.dmztournament.common.commands;

import com.dmztournament.DMZTournament;
import com.dmztournament.common.arena.Arena;
import com.dmztournament.common.rewards.PlayerRewards;
import com.dmztournament.common.tournament.Tournament;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * Commands for the tournament system.
 */
public class TournamentCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tournament")
            .requires(source -> source.hasPermission(0))
            
            // Help command
            .then(Commands.literal("help")
                .executes(TournamentCommands::showHelp))
            
            // Arena commands
            .then(Commands.literal("arena")
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(TournamentCommands::createArena)))
                .then(Commands.literal("delete")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(TournamentCommands::deleteArena)))
                .then(Commands.literal("list")
                    .executes(TournamentCommands::listArenas))
                .then(Commands.literal("info")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(TournamentCommands::arenaInfo))))
            
            // Tournament commands
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(TournamentCommands::createTournament)))
            
            .then(Commands.literal("join")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(TournamentCommands::joinTournament)))
            
            .then(Commands.literal("leave")
                .executes(TournamentCommands::leaveTournament))
            
            .then(Commands.literal("start")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(TournamentCommands::startTournament)))
            
            .then(Commands.literal("cancel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(TournamentCommands::cancelTournament)))
            
            .then(Commands.literal("list")
                .executes(TournamentCommands::listTournaments))
            
            .then(Commands.literal("info")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(TournamentCommands::tournamentInfo)))
            
            // Player stats
            .then(Commands.literal("stats")
                .executes(TournamentCommands::showStats)
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> showPlayerStats(ctx, EntityArgument.getPlayer(ctx, "player")))))
            
            // Leaderboard
            .then(Commands.literal("leaderboard")
                .executes(TournamentCommands::showLeaderboard))
            
            // Quick start (admin)
            .then(Commands.literal("quickstart")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(TournamentCommands::quickStart)))
        );
        
        // Alias: tourney
        dispatcher.register(Commands.literal("tourney")
            .redirect(dispatcher.getRoot().getChild("tournament")));
        
        // Alias: tour
        dispatcher.register(Commands.literal("tour")
            .redirect(dispatcher.getRoot().getChild("tournament")));
    }
    
    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSystemMessage(Component.literal("§6=== Tournament Addon Help ==="));
        source.sendSystemMessage(Component.literal("§e/tournament arena create <name> §7- Create a new arena"));
        source.sendSystemMessage(Component.literal("§e/tournament arena delete <name> §7- Delete an arena"));
        source.sendSystemMessage(Component.literal("§e/tournament arena list §7- List all arenas"));
        source.sendSystemMessage(Component.literal("§e/tournament create <name> §7- Create a tournament"));
        source.sendSystemMessage(Component.literal("§e/tournament join <id> §7- Join a tournament"));
        source.sendSystemMessage(Component.literal("§e/tournament leave §7- Leave current tournament"));
        source.sendSystemMessage(Component.literal("§e/tournament start <id> §7- Start a tournament (admin)"));
        source.sendSystemMessage(Component.literal("§e/tournament cancel <id> §7- Cancel a tournament (admin)"));
        source.sendSystemMessage(Component.literal("§e/tournament list §7- List active tournaments"));
        source.sendSystemMessage(Component.literal("§e/tournament stats §7- View your stats"));
        source.sendSystemMessage(Component.literal("§e/tournament leaderboard §7- View top players"));
        
        return 1;
    }
    
    private static int createArena(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        Arena arena = DMZTournament.getArenaManager().createArena(name, player.getName().getString());
        if (arena != null) {
            source.sendSuccess(() -> Component.literal("§aCreated arena: " + name), true);
            source.sendSystemMessage(Component.literal("§7Use /tournament arena setup to configure spawn points"));
            return 1;
        } else {
            source.sendFailure(Component.literal("§cAn arena with that name already exists!"));
            return 0;
        }
    }
    
    private static int deleteArena(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        
        Optional<Arena> arena = DMZTournament.getArenaManager().getArena(name);
        if (arena.isEmpty()) {
            source.sendFailure(Component.literal("§cArena not found!"));
            return 0;
        }
        
        if (DMZTournament.getArenaManager().deleteArena(arena.get().getId())) {
            source.sendSuccess(() -> Component.literal("§aDeleted arena: " + name), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFailed to delete arena!"));
            return 0;
        }
    }
    
    private static int listArenas(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSystemMessage(Component.literal("§6=== Arenas ==="));
        
        var arenas = DMZTournament.getArenaManager().getAllArenas();
        if (arenas.isEmpty()) {
            source.sendSystemMessage(Component.literal("§7No arenas created yet."));
            source.sendSystemMessage(Component.literal("§7Use /tournament arena create <name> to create one."));
        } else {
            for (Arena arena : arenas) {
                String status = arena.isActive() ? "§a[Active]" : "§7[Inactive]";
                source.sendSystemMessage(Component.literal(status + " §e" + arena.getName() + 
                    " §7- " + arena.getType().getDisplayName()));
            }
        }
        
        return 1;
    }
    
    private static int arenaInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        
        Optional<Arena> arenaOpt = DMZTournament.getArenaManager().getArena(name);
        if (arenaOpt.isEmpty()) {
            source.sendFailure(Component.literal("§cArena not found!"));
            return 0;
        }
        
        Arena arena = arenaOpt.get();
        source.sendSystemMessage(Component.literal("§6=== Arena: " + arena.getName() + " ==="));
        source.sendSystemMessage(Component.literal("§7Type: §e" + arena.getType().getDisplayName()));
        source.sendSystemMessage(Component.literal("§7Creator: §e" + arena.getCreator()));
        source.sendSystemMessage(Component.literal("§7Active: §e" + (arena.isActive() ? "Yes" : "No")));
        source.sendSystemMessage(Component.literal("§7Valid: §e" + (arena.isValid() ? "Yes" : "No")));
        source.sendSystemMessage(Component.literal("§7Total Matches: §e" + arena.getTotalMatches()));
        
        return 1;
    }
    
    private static int createTournament(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        Tournament tournament = DMZTournament.getTournamentManager().createTournament(name, player);
        tournament.setState(Tournament.TournamentState.REGISTRATION);
        
        source.sendSuccess(() -> Component.literal("§aCreated tournament: " + name), true);
        source.sendSystemMessage(Component.literal("§7Tournament ID: §e" + tournament.getId().toString().substring(0, 8)));
        source.sendSystemMessage(Component.literal("§7Players can join with: §e/tournament join " + tournament.getId().toString().substring(0, 8)));
        
        return 1;
    }
    
    private static int joinTournament(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String idStr = StringArgumentType.getString(context, "id");
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        // Try to find tournament by partial ID
        Tournament tournament = null;
        for (Tournament t : DMZTournament.getTournamentManager().getAllTournaments()) {
            if (t.getId().toString().startsWith(idStr)) {
                tournament = t;
                break;
            }
        }
        
        if (tournament == null) {
            source.sendFailure(Component.literal("§cTournament not found!"));
            return 0;
        }
        
        if (tournament.registerPlayer(player)) {
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFailed to join tournament!"));
            return 0;
        }
    }
    
    private static int leaveTournament(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        // Find tournament player is registered in
        for (Tournament tournament : DMZTournament.getTournamentManager().getAllTournaments()) {
            if (tournament.isRegistered(player.getUUID())) {
                if (tournament.unregisterPlayer(player)) {
                    return 1;
                }
            }
        }
        
        source.sendFailure(Component.literal("§cYou are not registered in any tournament!"));
        return 0;
    }
    
    private static int startTournament(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String idStr = StringArgumentType.getString(context, "id");
        
        Tournament tournament = null;
        for (Tournament t : DMZTournament.getTournamentManager().getAllTournaments()) {
            if (t.getId().toString().startsWith(idStr)) {
                tournament = t;
                break;
            }
        }
        
        if (tournament == null) {
            source.sendFailure(Component.literal("§cTournament not found!"));
            return 0;
        }
        
        if (DMZTournament.getTournamentManager().startTournament(tournament.getId())) {
            source.sendSuccess(() -> Component.literal("§aTournament started!"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFailed to start tournament!"));
            return 0;
        }
    }
    
    private static int cancelTournament(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String idStr = StringArgumentType.getString(context, "id");
        
        Tournament tournament = null;
        for (Tournament t : DMZTournament.getTournamentManager().getAllTournaments()) {
            if (t.getId().toString().startsWith(idStr)) {
                tournament = t;
                break;
            }
        }
        
        if (tournament == null) {
            source.sendFailure(Component.literal("§cTournament not found!"));
            return 0;
        }
        
        if (DMZTournament.getTournamentManager().cancelTournament(tournament.getId())) {
            source.sendSuccess(() -> Component.literal("§cTournament cancelled!"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFailed to cancel tournament!"));
            return 0;
        }
    }
    
    private static int listTournaments(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSystemMessage(Component.literal("§6=== Tournaments ==="));
        
        var tournaments = DMZTournament.getTournamentManager().getAllTournaments();
        if (tournaments.isEmpty()) {
            source.sendSystemMessage(Component.literal("§7No active tournaments."));
            source.sendSystemMessage(Component.literal("§7Use /tournament create <name> to create one."));
        } else {
            for (Tournament tournament : tournaments) {
                String stateColor = switch (tournament.getState()) {
                    case REGISTRATION -> "§a";
                    case IN_PROGRESS -> "§6";
                    case FINISHED -> "§7";
                    default -> "§c";
                };
                source.sendSystemMessage(Component.literal(stateColor + "[" + tournament.getState().getDisplayName() + "] §e" + tournament.getName() + 
                    " §7(" + tournament.getParticipantCount() + " players) ID: " + tournament.getId().toString().substring(0, 8)));
            }
        }
        
        return 1;
    }
    
    private static int tournamentInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String idStr = StringArgumentType.getString(context, "id");
        
        Tournament tournament = null;
        for (Tournament t : DMZTournament.getTournamentManager().getAllTournaments()) {
            if (t.getId().toString().startsWith(idStr)) {
                tournament = t;
                break;
            }
        }
        
        if (tournament == null) {
            source.sendFailure(Component.literal("§cTournament not found!"));
            return 0;
        }
        
        source.sendSystemMessage(Component.literal("§6=== Tournament: " + tournament.getName() + " ==="));
        source.sendSystemMessage(Component.literal("§7Status: §e" + tournament.getState().getDisplayName()));
        source.sendSystemMessage(Component.literal("§7Host: §e" + tournament.getHostName()));
        source.sendSystemMessage(Component.literal("§7Format: §e" + tournament.getFormat().getDisplayName()));
        source.sendSystemMessage(Component.literal("§7Participants: §e" + tournament.getParticipantCount()));
        
        return 1;
    }
    
    private static int showStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        return showPlayerStats(context, player);
    }
    
    private static int showPlayerStats(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        CommandSourceStack source = context.getSource();
        PlayerRewards rewards = DMZTournament.getTournamentManager().getRewardManager().getPlayerRewards(player.getUUID());
        
        source.sendSystemMessage(Component.literal("§6=== Tournament Stats: " + player.getName().getString() + " ==="));
        source.sendSystemMessage(Component.literal("§7Tournaments Played: §e" + rewards.getTournamentsPlayed()));
        source.sendSystemMessage(Component.literal("§7Tournaments Won: §e" + rewards.getTournamentsWon()));
        source.sendSystemMessage(Component.literal("§7Win Rate: §e" + String.format("%.1f%%", rewards.getWinRate())));
        source.sendSystemMessage(Component.literal("§7Matches Played: §e" + rewards.getMatchesPlayed()));
        source.sendSystemMessage(Component.literal("§7Kills: §e" + rewards.getTotalKills()));
        source.sendSystemMessage(Component.literal("§7Deaths: §e" + rewards.getTotalDeaths()));
        source.sendSystemMessage(Component.literal("§7K/D Ratio: §e" + String.format("%.2f", rewards.getKDRatio())));
        source.sendSystemMessage(Component.literal("§7Tournament Points: §e" + rewards.getTournamentPoints()));
        source.sendSystemMessage(Component.literal("§7Best Win Streak: §e" + rewards.getBestWinStreak()));
        
        if (rewards.getCurrentTitle() != null) {
            source.sendSystemMessage(Component.literal("§7Current Title: §e" + rewards.getCurrentTitle()));
        }
        
        return 1;
    }
    
    private static int showLeaderboard(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSystemMessage(Component.literal("§6=== Tournament Leaderboard ==="));
        source.sendSystemMessage(Component.literal("§7Leaderboard coming soon!"));
        
        return 1;
    }
    
    private static int quickStart(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        // Create and immediately start a tournament
        Tournament tournament = DMZTournament.getTournamentManager().createTournament(name, player);
        tournament.setState(Tournament.TournamentState.REGISTRATION);
        
        // Auto-register the creator
        tournament.registerPlayer(player);
        
        source.sendSuccess(() -> Component.literal("§aQuick tournament created!"), true);
        source.sendSystemMessage(Component.literal("§7Tournament ID: §e" + tournament.getId().toString().substring(0, 8)));
        source.sendSystemMessage(Component.literal("§7Players can join with: §e/tournament join " + tournament.getId().toString().substring(0, 8)));
        source.sendSystemMessage(Component.literal("§7Start when ready with: §e/tournament start " + tournament.getId().toString().substring(0, 8)));
        
        return 1;
    }
}
