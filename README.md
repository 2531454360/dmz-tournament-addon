# DMZ Tournament Addon

A battle arena and tournament system addon for the DragonMineZ Minecraft mod.

## Features

### 🏟️ Arena System
- Create custom battle arenas with defined boundaries
- Set player spawn points and spectator areas
- Support for multiple arena types (Standard, Elevated, Underwater, Lava, Void, Custom)
- Arena validation and activation system

### 🏆 Tournament System
- Multiple tournament formats:
  - **Single Elimination**: Players eliminated after one loss
  - **Double Elimination**: Players eliminated after two losses
  - **Round Robin**: Everyone fights everyone
  - **Swiss System**: Players paired based on performance
  - **Free For All**: Battle Royale style

### 📊 Match System
- Real-time match tracking
- Configurable time limits
- Death and kill tracking
- Automatic win condition detection
- Forfeit handling for disconnections

### 🎁 Reward System
- Customizable reward presets (Standard, Premium, Special)
- Item rewards for top placements
- Title rewards for winners
- Tournament points system
- Integration with DMZ stats system

### 📈 Player Statistics
- Tournaments played/won tracking
- Win rate calculation
- K/D ratio tracking
- Win streak tracking
- Tournament points

## Commands

### Arena Commands
```
/tournament arena create <name>     - Create a new arena
/tournament arena delete <name>     - Delete an arena
/tournament arena list              - List all arenas
/tournament arena info <name>       - Show arena details
```

### Tournament Commands
```
/tournament create <name>           - Create a tournament
/tournament join <id>               - Join a tournament
/tournament leave                   - Leave current tournament
/tournament start <id>              - Start a tournament (admin)
/tournament cancel <id>             - Cancel a tournament (admin)
/tournament list                    - List active tournaments
/tournament info <id>               - Show tournament details
```

### Player Commands
```
/tournament stats                   - View your stats
/tournament stats <player>          - View another player's stats (admin)
/tournament leaderboard             - View top players
/tournament help                    - Show help
```

### Quick Commands
```
/tournament quickstart <name>       - Create and auto-join a tournament
```

## Configuration

The mod can be configured via `dmztournament-common.toml`:

```toml
[tournament]
# Default maximum number of participants in a tournament
defaultMaxParticipants = 16
# Minimum number of participants required to start a tournament
minParticipants = 2
# Time in seconds for registration period (-1 for unlimited)
registrationTime = -1

[match]
# Time limit for each match in seconds (-1 for no limit)
matchTimeLimit = 300
# Maximum deaths before a player is eliminated
maxDeaths = 3
# Break time between matches in seconds
breakTime = 30

[scoring]
# Points awarded for winning a match
winPoints = 3
# Points awarded per kill
killPoints = 1
# Points awarded for participating
participationPoints = 1

[rewards]
# Default reward preset to use (standard, premium, special)
rewardPreset = "standard"
# Enable DMZ stat point bonuses for winners
enableStatBonuses = true
# Enable title rewards for winners
enableTitles = true

[integration]
# Enable integration with DragonMineZ stats system
integrateDMZStats = true
# Enable integration with DragonMineZ transformation forms
integrateDMZForms = true
```

## Items

- **Tournament Token**: Used to enter tournaments
- **Champion Medal**: Awarded to tournament champions
- **Arena Pass**: Grants access to tournament arenas
- **Tournament Chest**: Contains tournament rewards

## Integration with DragonMineZ

This addon integrates with DragonMineZ's systems:
- **Stats System**: Track player performance and award stat bonuses
- **Forms System**: Tournament rules can restrict transformations
- **Skills System**: Match tracking for skill usage

## Building

1. Clone the repository
2. Run `./gradlew build`
3. The built JAR will be in `build/libs/`

## Requirements

- Minecraft 1.20.1
- Forge 47.3.0+
- DragonMineZ 2.0.0+ (optional but recommended)

## License

MIT License

## Credits

Created for the DragonMineZ community.
