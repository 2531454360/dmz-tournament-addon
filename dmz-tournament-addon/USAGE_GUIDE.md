# DMZ Tournament Addon - 使用指南

## 简介

这是一个为 DragonMineZ 模组设计的武道会竞技场附属插件。它允许玩家创建竞技场、举办锦标赛、追踪比赛数据并发放奖励。

## 安装

1. 确保已安装 Minecraft 1.20.1 和 Forge 47.3.0+
2. 将 `dmztournament-1.0.0.jar` 放入 `mods` 文件夹
3. 启动游戏

## 快速开始

### 1. 创建竞技场

首先，你需要创建一个竞技场：

```
/tournament arena create myarena
```

然后设置竞技场的边界和出生点：
- 走到竞技场的一个角落，执行 `/tournament arena setcorner1 myarena`
- 走到对角，执行 `/tournament arena setcorner2 myarena`
- 设置玩家1出生点：走到位置执行 `/tournament arena setspawn1 myarena`
- 设置玩家2出生点：走到位置执行 `/tournament arena setspawn2 myarena`
- 添加观众出生点： `/tournament arena addspectator myarena`

激活竞技场：
```
/tournament arena activate myarena
```

### 2. 创建武道会

创建一个新的武道会：
```
/tournament create "天下第一武道会"
```

系统会返回一个武道会ID，例如 `a1b2c3d4`

### 3. 玩家加入

其他玩家可以加入：
```
/tournament join a1b2c3d4
```

### 4. 开始武道会

当有足够玩家后，开始武道会：
```
/tournament start a1b2c3d4
```

## 命令列表

### 竞技场管理

| 命令 | 描述 | 权限 |
|------|------|------|
| `/tournament arena create <name>` | 创建新竞技场 | 玩家 |
| `/tournament arena delete <name>` | 删除竞技场 | OP |
| `/tournament arena list` | 列出所有竞技场 | 所有人 |
| `/tournament arena info <name>` | 查看竞技场详情 | 所有人 |
| `/tournament arena activate <name>` | 激活竞技场 | OP |
| `/tournament arena deactivate <name>` | 停用竞技场 | OP |

### 武道会管理

| 命令 | 描述 | 权限 |
|------|------|------|
| `/tournament create <name>` | 创建武道会 | 玩家 |
| `/tournament join <id>` | 加入武道会 | 玩家 |
| `/tournament leave` | 离开武道会 | 玩家 |
| `/tournament start <id>` | 开始武道会 | OP |
| `/tournament cancel <id>` | 取消武道会 | OP |
| `/tournament list` | 列出武道会 | 所有人 |
| `/tournament info <id>` | 查看武道会详情 | 所有人 |

### 玩家功能

| 命令 | 描述 | 权限 |
|------|------|------|
| `/tournament stats` | 查看自己的统计 | 玩家 |
| `/tournament stats <player>` | 查看玩家统计 | OP |
| `/tournament leaderboard` | 查看排行榜 | 所有人 |
| `/tournament help` | 显示帮助 | 所有人 |

## 武道会格式

### 单败淘汰制 (Single Elimination)
- 输一场即被淘汰
- 最快的比赛形式
- 适合 4-16 人

### 双败淘汰制 (Double Elimination)
- 输两场才被淘汰
- 更公平，给玩家第二次机会
- 适合 4-16 人

### 循环赛 (Round Robin)
- 每个玩家与其他所有玩家对战
- 最公平的比赛形式
- 适合 3-8 人

### 瑞士制 (Swiss System)
- 根据战绩配对
- 适合大量玩家
- 适合 8-32 人

### 混战 (Free For All)
- 所有人同时战斗
- 最后存活者获胜
- 适合 3-10 人

## 比赛规则

可以在 `dmztournament-common.toml` 中配置默认规则：

```toml
[match]
# 每场比赛时间限制（秒）
matchTimeLimit = 300
# 最大死亡次数
maxDeaths = 3
# 比赛间隔时间
breakTime = 30
```

### 规则选项

- **形态限制**: 允许/禁止超级赛亚人、界王拳等
- **物品限制**: 允许/禁止治疗物品、武器、护甲
- **战斗限制**: 允许/禁止气功攻击、近战、飞行、瞬移
- **时间限制**: 每场比赛的时间限制

## 奖励系统

### 预设奖励

**标准奖励 (standard)**
- 冠军: 5个仙豆 + 10个钻石 + "武道会冠军"称号
- 亚军: 3个仙豆 + 5个钻石
- 季军: 1个仙豆 + 2个钻石

**高级奖励 (premium)**
- 冠军: 10个仙豆 + 3个下界合金锭 + 100武道会积分
- 亚军: 6个仙豆 + 1个下界合金锭 + 50武道会积分
- 季军: 3个仙豆 + 2个钻石块 + 25武道会积分

**特殊奖励 (special)**
- 冠军: 1个龙珠 + 1个龙蛋 + "龙珠大师"称号

### 自定义奖励

服务器管理员可以通过数据包或配置文件自定义奖励。

## 玩家统计

系统会追踪以下数据：
- 参加武道会次数
- 武道会获胜次数
- 胜率
- 总击杀数
- 总死亡数
- K/D 比率
- 武道会积分
- 最佳连胜纪录
- 解锁的称号

## 配置选项

### 主要配置 (`dmztournament-common.toml`)

```toml
[tournament]
# 默认最大参与人数
defaultMaxParticipants = 16
# 最小开始人数
minParticipants = 2

[scoring]
# 获胜得分
winPoints = 3
# 击杀得分
killPoints = 1
# 参与得分
participationPoints = 1

[rewards]
# 默认奖励预设
rewardPreset = "standard"
# 启用属性加成
enableStatBonuses = true
# 启用称号
enableTitles = true

[integration]
# 启用DMZ属性系统集成
integrateDMZStats = true
# 启用DMZ形态系统集成
integrateDMZForms = true
```

## 常见问题

### Q: 玩家掉线了怎么办？
A: 系统会自动将该玩家判负，视为弃权。

### Q: 可以暂停武道会吗？
A: 目前不支持暂停功能，但可以通过取消后重新创建来实现。

### Q: 如何设置多个竞技场？
A: 重复创建竞技场的步骤即可，系统支持无限数量的竞技场。

### Q: 观众可以进入竞技场吗？
A: 可以，设置观众出生点后，观众可以使用 `/tournament spectate` 命令观战。

### Q: 如何重置玩家数据？
A: 删除世界文件夹中的 `dmztournament_rewards.dat` 文件。

## 开发者信息

### API 使用

```java
// 获取竞技场管理器
ArenaManager arenaManager = DMZTournament.getArenaManager();

// 获取武道会管理器
TournamentManager tournamentManager = DMZTournament.getTournamentManager();

// 创建武道会
Tournament tournament = tournamentManager.createTournament("名称", hostPlayer);

// 获取玩家奖励数据
PlayerRewards rewards = tournamentManager.getRewardManager().getPlayerRewards(playerUUID);
```

### 事件

- `TournamentStartEvent` - 武道会开始时触发
- `TournamentEndEvent` - 武道会结束时触发
- `MatchStartEvent` - 比赛开始时触发
- `MatchEndEvent` - 比赛结束时触发
- `PlayerJoinTournamentEvent` - 玩家加入武道会时触发
- `PlayerLeaveTournamentEvent` - 玩家离开武道会时触发

## 支持与反馈

如有问题或建议，请在 GitHub Issues 中提交。

## 更新日志

### v1.0.0
- 初始版本
- 竞技场系统
- 武道会系统
- 奖励系统
- 玩家统计
