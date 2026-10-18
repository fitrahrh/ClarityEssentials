# 🌟 ClarityEssentials

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Paper-26.3+-red.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge)](https://openjdk.org)

**ClarityEssentials** is a next-generation core plugin custom-engineered for **Clarity Network** (Minecraft 1.21.4+ / Paper 26.3+). Built to completely replace legacy essentials plugins (such as EssentialsX), it leverages **Paper Native Dialog API** and **Adventure MiniMessage** to deliver a sleek, modern user experience entirely rendered through native client dialog screens without chest GUI hacks or inventory tricks.

---

## ✨ Features

### 🎮 1. Native ESC / Pause Menu Button ("Clarity Menu")
* Adds a native button directly onto Minecraft's Game Menu (ESC screen) via the `#minecraft:pause_screen_additions` dialog tag.
* Client-native integration inspired by top modern servers (like Donut SMP) that opens a multi-action dialog directly from the pause screen.
* Configurable button label and dialog title in `config.yml` (`esc-menu.button-text` and `esc-menu.dialog-title`).
* Provides instant access to all core gameplay features arranged cleanly in 2 columns.

### 🧭 2. Centralized Player Dashboard (`/menu`, `/dashboard`)
* Comprehensive player hub rendered using Paper's native multi-action dialog.
* **12 Core Feature Actions (2 Columns, 6 Rows)**:
  * **Row 1**: Homes (L), RTP (R)
  * **Row 2**: Auction (L), Friends (R)
  * **Row 3**: Webstore (L), Teleport (R)
  * **Row 4**: Pay (L), Stats (R)
  * **Row 5**: Leaderboards (L), Settings (R)
  * **Row 6**: Skills (L), Coming Soon (R)
  * **Bottom**: Clean exit button to return to gameplay.
* **Webstore Button**: Sends an interactive clickable link in chat directing players to the official server store (`store.clarity-id.net`).
* Clean, minimalist design using white typography without distracting clutter.

### 🕊️ 3. Advanced Flight System (`/fly`)
* Flight mode with duration support (e.g. `/fly 30m`, `/fly 1h`, `/fly 45s`, `/fly unlimited`).
* Countdown warnings before flight expiration (60s, 30s, 15s, 10s, 5s-1s).
* Temporary **fall damage protection** buffer upon flight expiration or disable.
* **World & Region Protection**:
  * Configurable **Blacklist / Whitelist** for Worlds.
  * Configurable **Blacklist / Whitelist** for **WorldGuard 7 Regions** (e.g., spawn, pvp arena, warzone).
  * Safely disables flight and notifies the player upon entering restricted zones.

### ⚡ 4. Movement & Flight Speed (`/speed`)
* EssentialsX-compliant speed multiplier system with automatic ground/flight detection:
  * Running `/speed <0-10>` while walking modifies **walking speed**.
  * Running `/speed <0-10>` while flying modifies **flying speed**.
* Explicit speed modes: `/speed walk <value>` and `/speed fly <value>`.
* Reset speeds back to vanilla defaults: `/speed reset` (or `/speed <walk|fly> reset`).
* Target other players: `/speed <value> [player]`.

### 💖 5. Player Vitals Management (`/feed`, `/heal`)
* **`/feed [player]`**: Fully replenishes food hunger bar, saturation, clears hunger potion effects, and plays sound.
* **`/heal [player]`**: Fully restores maximum health ([`Attribute.MAX_HEALTH`](https://jd.papermc.io/paper/1.21/org/bukkit/attribute/Attribute.html#MAX_HEALTH)), restores hunger, removes negative potion effects, and extinguishes fire.

### 🎮 6. Game Mode Switcher & Direct Shortcuts (`/gamemode`, `/gmc`, `/gms`, `/gmsp`, `/gma`)
* `/gamemode <mode> [player]` (alias `/gm`) supporting mode names (`survival`, `creative`, `adventure`, `spectator`) or numerical IDs (`0`, `1`, `2`, `3`).
* Dedicated instant shortcut aliases:
  * `/gmc [player]` - Creative Mode
  * `/gms [player]` - Survival Mode
  * `/gmsp [player]` - Spectator Mode
  * `/gma [player]` - Adventure Mode

### 📢 7. Global Announcement Broadcast (`/broadcast`, `/bc`, `/bcast`)
* Global announcement broadcasts supporting Adventure MiniMessage formatting.
* Full **PlaceholderAPI** expansion support in broadcast messages.
* Attention-grabbing ping chime sound for all online players.

### 💤 8. AFK Status System (`/afk`)
* Toggle Away-From-Keyboard status with optional reason (`/afk [reason]`).
* Server-wide broadcast announcements upon entering and leaving AFK status.
* Automatic un-AFK detection when players move, chat, run commands, or interact.

### 🏠 9. Native Dialog Home Management & Digital Currency Limit (`/home`, `/sethome`, `/delhome`, `/homelimit`)
* All home operations (listing, creating, teleporting, deleting) operate via native dialogs.
* **Interactive Text Input**: `/sethome` (or clicking "+ New Home") opens a native text dialog to name the home.
* **Deletion Confirmation**: Prompts a confirmation dialog before deleting to prevent accidental loss.
* **Digital Currency / Numerical Home Limit Quota**:
  * Player home limits can be incremented, decremented, or set dynamically via commands (`/homelimit give/take/set <player> <amount>`).
  * Total Allowed Homes = Base Quota (from rank/default) + Extra Bonus Limit slots.
  * Perfect for store packages, voting rewards, or token purchases!

### 💬 10. Private Messaging & Admin SocialSpy (`/msg`, `/r`, `/socialspy`)
* Fast, private 1-on-1 player messaging (`/msg <player> <message>`, aliases: `/tell`, `/w`, `/whisper`).
* Quick reply command (`/r <message>`).
* Privacy-aware: respects individual player `/menu` privacy settings (Anyone, Friends Only, Off).
* **Staff Social Spy (`/socialspy`)**: Allows authorized administrators (`clarityessentials.socialspy`) to monitor player private messages in real-time. Preferences persist across restarts.

### 🚪 11. Custom Join & Leave Messages (Default + VIP Permissions)
* Suppresses vanilla join/quit notifications and broadcasts customizable MiniMessage announcements.
* **VIP Custom Join Messages**: Priority-based permission checks (`clarityessentials.join.<group>`) allow ranks (Owner, Admin, VIP) to have unique entrance broadcasts.
* Individual player control: players can filter join/leave broadcasts in `/menu` Settings (Anyone, Friends Only, or Off).

### 👥 12. Friends System
* Add friends with instant player validation (prevents adding yourself or non-existent players).
* Real-time online/offline player status.
* Quick actions for each friend:
  * Send instant Teleport Request (`/tpa`).
  * Send Money via quick pay dialog.
  * Friendly-fire protection toggle to avoid harming teammates.
  * Remove friend with a native confirmation dialog.

### 🌀 13. Teleportation & Player List (`/tpa`, `/tpahere`, `/tptoggle`)
* Interactive clickable chat messages for incoming teleport requests (`[ACCEPT]` and `[DENY]`).
* Request timeout handling (default: 60s) and warmup countdown with movement cancellation.
* Toggle incoming requests on/off via `/tptoggle`.
* Quick teleport list in the dialog menu to quickly request teleport to saved players.

### 💰 14. Quick Pay System (`/pay`)
* Vault Economy integration with transaction validation and balance checks.
* Saved recipient list for quick transfers.
* Preset denomination buttons and native text input dialog for custom transfer amounts.

### 🏷️ 15. Smart Nickname System (`/nick`, `/realname`)
* Native text dialog to preview, change, or reset nicknames.
* Strict regex validation (`allowed-nicks-regex: "^[a-zA-Z0-9_]{3,16}$"`) prevents exploit characters.
* Dedicated permissions for colors (`clarityessentials.nick.color`) and unsafe character bypass (`clarityessentials.nick.allowunsafe`).
* Automatic synchronization across chat and tablist.
* `/realname <nickname>` command to resolve a player's real username.

### ⚙️ 16. Player Settings & Leaderboards
* Interactive native toggles for player preferences: Chat (Public Chat filter), Notifications, PvP, Visuals, Privacy, and Scoreboard.
* Real-time leaderboards across multiple categories: Balance, Deaths, Playtime, Blocks Placed, Blocks Broken, and Mobs Killed.

### 🌐 17. Multi-Language Support (ClarityLangCore Integration)
* Automatically connects to **ClarityLangCore** when available, enabling per-player dynamic language switching (`/lang`).
* Includes 9 bundled language localizations:
  * 🇺🇸 English (`en_US`)
  * 🇮🇩 Indonesian (`id_ID`)
  * 🇩🇪 German (`de_DE`)
  * 🇪🇸 Spanish (`es_ES`)
  * 🇫🇷 French (`fr_FR`)
  * 🇯🇵 Japanese (`ja_JP`)
  * 🇧🇷 Portuguese (`pt_BR`)
  * 🇷🇺 Russian (`ru_RU`)
  * 🇨🇳 Simplified Chinese (`zh_CN`)
* Self-contained standalone fallback when running without ClarityLangCore.

### 📍 18. Spawn & First-Join System (`/spawn`, `/setspawn`)
* Dedicated spawn system with separate **default spawn** and **first-join spawn** locations.
* `/setspawn [default|firstjoin]`: Define the main server spawn or a dedicated welcoming area for first-time players.
* `/spawn [player]`: Teleport self or send target players to the server spawn. Supports warmup delay and movement cancellation.
* Configurable death respawn routing: automatically respawns players at spawn point, with option to respect or override beds/respawn anchors.

### ✉️ 19. Mailbox & Broadcast Mail System (`/mail`)
* EssentialsX-style persistent mailbox system supporting offline player recipients.
* **Personal & Admin Commands**:
  * `/mail read [page]`: Read incoming messages with automated pagination and unread status tracking.
  * `/mail send <player> <message>`: Send private mail to online or offline players.
  * `/mail clear [number]`: Clear a specific mail message by index.
  * `/mail clearall`: Clear all messages from your mailbox.
  * `/mail sendall <message>`: Broadcast a permanent mail announcement to all players.
  * `/mail sendtempall <duration> <message>`: Broadcast an expiring announcement mail (e.g., `30m`, `24h`, `7d`).
  * `/mail clear <player> [number]`: Staff command to purge messages from a specific player's mailbox.
* Automated join notification with clickable `/mail read` link when players log in with unread messages.

### 📰 20. Native JoinDialog & Server Update / Changelog (`/update`, `/changelog`)
* Uses **Paper Native Dialog API** to present visually appealing, client-native modals without any chest GUI hacks.
* **First-Join Tips Dialog**: Welcomes brand new players with a beginner guidelines modal on their first connection.
* **Server Update & Changelog Dialog**:
  * Displays changelog highlights once a day on player join.
  * Includes interactive action buttons: **Tutup (Close)** and **Jangan ingatkan lagi (Don't remind again)**.
  * Dismissing reminders suppresses daily popups for the current version (`current-update-id`), but automatically re-alerts players when an admin bumps the update ID!
  * Manually viewable anytime via `/update` or `/changelog`.


---

## 🧩 PlaceholderAPI Expansion

ClarityEssentials natively registers expansion identifiers: `%clarityessentials_<placeholder>%` and `%clarity_<placeholder>%`.

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%clarityessentials_homes_count%` | Number of homes created by the player | `2` |
| `%clarityessentials_homes_max%` | Total maximum allowed homes | `5` or `Unlimited` |
| `%clarityessentials_homes_base%` | Base home limit from rank/config | `3` |
| `%clarityessentials_homes_extra%` | Extra purchased/granted home slots | `2` |
| `%clarityessentials_homes_remaining%` | Remaining home slots available | `3` |
| `%clarityessentials_nickname%` | Player's custom formatted nickname | `<#00fbff>Haruki</#00fbff>` |
| `%clarityessentials_raw_nickname%` | Plain unformatted nickname | `Haruki` |
| `%clarityessentials_realname%` | Player's original Minecraft username | `Harukiyu` |
| `%clarityessentials_tpa_status%` | Incoming TPA toggle state | `Enabled` / `Disabled` |
| `%clarityessentials_friends_count%` | Total friends added | `12` |
| `%clarityessentials_friends_online%` | Number of friends currently online | `4` |
| `%clarityessentials_setting_public_chat%` | Public chat toggle state | `ON` / `OFF` |
| `%clarityessentials_setting_pm%` | Private message privacy setting | `Anyone` / `Friends` / `Off` |
| `%clarityessentials_setting_socialspy%` | Admin Social Spy toggle state | `ON` / `OFF` |
| `%clarityessentials_setting_join_leave%` | Join/Leave broadcast filter | `Anyone` / `Friends` / `Off` |

---

## 📜 Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/menu` | `/dashboard`, `/cp` | Open Centralized Player Dashboard | `clarityessentials.command.menu` |
| `/feed [player]` | `/eat` | Replenish hunger and saturation bar | `clarityessentials.command.feed` |
| `/heal [player]` | - | Restore max health, extinguish fire & status effects | `clarityessentials.command.heal` |
| `/fly [duration] [player]`| `/flight` | Toggle flight or set duration (`30m`, `1h`, `unlimited`) | `clarityessentials.command.fly` |
| `/speed [fly\|walk] <0-10>`| - | Modify ground walking or flight speed multiplier | `clarityessentials.command.speed` |
| `/broadcast <message>` | `/bc`, `/bcast` | Broadcast a global announcement with MiniMessage & PAPI | `clarityessentials.command.broadcast` |
| `/afk [reason]` | `/away` | Toggle away-from-keyboard status with optional reason | `clarityessentials.command.afk` |
| `/gamemode <mode> [player]`| `/gm` | Switch player game mode (survival, creative, etc.) | `clarityessentials.command.gamemode` |
| `/gmc [player]` | - | Instant shortcut to Creative mode | `clarityessentials.command.gamemode.creative` |
| `/gms [player]` | - | Instant shortcut to Survival mode | `clarityessentials.command.gamemode.survival` |
| `/gmsp [player]` | - | Instant shortcut to Spectator mode | `clarityessentials.command.gamemode.spectator` |
| `/gma [player]` | - | Instant shortcut to Adventure mode | `clarityessentials.command.gamemode.adventure` |
| `/msg <player> <msg>` | `/tell`, `/w`, `/whisper` | Send a private message to a player | `clarityessentials.command.msg` |
| `/reply <msg>` | `/r` | Reply to the last messaged player | `clarityessentials.command.reply` |
| `/socialspy` | `/spy` | Toggle admin monitoring of private messages | `clarityessentials.socialspy` |
| `/homelimit` | `/hl` | View your home quota or give/take slots | `clarityessentials.command.homelimit` |
| `/home [name]` | `/homes` | Open Home Dialog or teleport to a home | `clarityessentials.command.home` |
| `/sethome [name]` | - | Save current location as a home | `clarityessentials.command.sethome` |
| `/delhome [name]` | `/deletehome` | Delete a saved home with confirmation | `clarityessentials.command.delhome` |
| `/nick <name\|reset>` | `/nickname` | Set or reset your custom nickname | `clarityessentials.command.nick` |
| `/realname <nickname>` | - | Lookup real username from nickname | `clarityessentials.command.realname` |
| `/tpa <player>` | - | Send teleport request to a player | `clarityessentials.command.tpa` |
| `/tpahere <player>` | - | Request a player to teleport to you | `clarityessentials.command.tpahere` |
| `/tpaccept [player]` | `/tpyes` | Accept a pending teleport request | `clarityessentials.command.tpaccept` |
| `/tpdeny [player]` | `/tpno` | Decline a pending teleport request | `clarityessentials.command.tpdeny` |
| `/spawn [player]` | - | Teleport self or others to server spawn | `clarityessentials.command.spawn` |
| `/setspawn [default\|firstjoin]` | - | Set default spawn or first-join spawn location | `clarityessentials.command.setspawn` |
| `/mail <subcommand>` | - | EssentialsX-style mailbox system | `clarityessentials.command.mail` |
| `/update` | `/changelog`, `/news` | View server updates and patch notes modal | `clarityessentials.command.update` |
| `/tptoggle` | - | Toggle incoming teleport requests | `clarityessentials.command.tptoggle` |
| `/ce reload` | `/clarityessentials` | Reload plugin configuration and data | `clarityessentials.admin` |

### Special Permissions:
* `clarityessentials.command.feed.others`: Feed other players.
* `clarityessentials.command.heal.others`: Heal other players.
* `clarityessentials.command.fly.others`: Toggle flight for other players.
* `clarityessentials.command.fly.unlimited`: Allow unlimited flight without duration expiration.
* `clarityessentials.command.speed.others`: Change walking or flight speed of other players.
* `clarityessentials.command.gamemode.others`: Change game mode for other players.
* `clarityessentials.command.spawn.others`: Teleport other players to spawn.
* `clarityessentials.command.mail.sendall`: Send global broadcast and temporary broadcast mails (`/mail sendall`, `/mail sendtempall`).
* `clarityessentials.command.mail.admin`: Manage and clear mail messages from other players' mailboxes.
* `clarityessentials.homelimit.admin`: Permission to view others' home quotas or give/take extra slots (`/homelimit give/take/set/check`).
* `clarityessentials.socialspy`: Permission to toggle and view private messages via `/socialspy`.
* `clarityessentials.chat.color`: Permission to use color and MiniMessage tags in chat.
* `clarityessentials.nick.color`: Permission to format nicknames with colors.
* `clarityessentials.nick.allowunsafe`: Permission to bypass regex and length limits on nicknames.
* `clarityessentials.nick.others`: Permission to modify other players' nicknames.
* `clarityessentials.tpa.bypasswarmup`: Permission to teleport instantly without warmup delay.
* `clarityessentials.homes.unlimited`: Grants unlimited home creation slots.
* `clarityessentials.homes.<rank>`: Grants home limit matching specific rank group defined in `config.yml`.
* `clarityessentials.join.<rank>`: Custom entrance join message matching VIP rules in `config.yml`.

---

## ⚙️ Configuration

The main configuration file is located at `plugins/ClarityEssentials/config.yml`.

```yaml
# Spawn System
spawn:
  enabled: true
  respawn-at-spawn: true
  respawn-override-bed: false
  teleport-on-first-join: true
  teleport-on-join: false

# Mail System
mail:
  enabled: true
  notify-on-join: true

# JoinDialog & Changelog System (Paper Native Dialog API)
join-dialog:
  enabled: true
  current-update-id: "v1.0.0"
  daily-reminder: true
  first-join-tips:
    enabled: true
    title: "<gradient:#00fbff:#0077ff><bold>Welcome to Clarity Network!</bold></gradient>"
    body:
      - "<yellow>★ <bold>Beginner Tips & Guidelines:</bold></yellow>"
      - "<gray>1. Use <white>/menu</white> or press <white>ESC</white> to open Dashboard.</gray>"
  changelog:
    title: "<gradient:#ffaa00:#ff5555><bold>Server Updates & Changelog</bold></gradient>"
    body:
      - "<gold><bold>What's New:</bold></gold>"
      - "<gray>• Added <white>/spawn</white> with dedicated first-join spawn point.</gray>"
      - "<gray>• Added <white>/mail</white> system for persistent inboxes.</gray>"

# ESC (Pause Menu) Custom Button & Native Dialog Menu
esc-menu:
  enabled: true
  button-text: "Clarity Menu"   # Button text on ESC Game Menu
  dialog-title: "Clarity Menu"  # Title header inside the dialog
  columns: 2
  webstore-url: "https://store.clarity-id.net"
  commands:
    rtp: "rtp"
    auction: "ah"
    stats: "stats"
    skills: "skills"

# Fly System Settings (/fly)
fly:
  enabled: true
  default-duration: "unlimited"
  world-mode: "BLACKLIST" # Options: BLACKLIST, WHITELIST, DISABLED
  worlds:
    - "world_nether"
    - "world_the_end"
  region-mode: "BLACKLIST" # Options: BLACKLIST, WHITELIST, DISABLED
  regions:
    - "pvp_arena"
    - "warzone"
    - "spawn"
  fall-protection-seconds: 15

# AFK System Settings (/afk)
afk:
  enabled: true
  auto-unafk-on-move: true
  auto-unafk-on-chat: true

# Home settings
homes:
  enabled: true
  default-limit: 3
  limits:
    vip: 5
    mvp: 10
    elite: 20
    sponsor: 50

# Custom Join & Leave Messages
join-leave:
  enabled: true
  default-join: "<dark_gray>[<green>+</green>]</dark_gray> <white><player></white> <gray>joined the game.</gray>"
  default-leave: "<dark_gray>[<red>-</red>]</dark_gray> <white><player></white> <gray>left the game.</gray>"
  vip-join:
    owner:
      permission: "clarityessentials.join.owner"
      message: "<gold>★ <bold>[Owner]</bold> <gradient:#ff5555:#ffaa00><player></gradient> has joined the game! ★</gold>"
    admin:
      permission: "clarityessentials.join.admin"
      message: "<red>★ <bold>[Admin]</bold> <red><player></red> has joined the game! ★</red>"
    vip:
      permission: "clarityessentials.join.vip"
      message: "<yellow>★ <gold>[VIP]</gold> <yellow><player></yellow> joined the server! ★</yellow>"
```

---

## 🛠️ Building from Source

### Requirements
* **Java 25** (JDK 25 LTS or newer)
* **Gradle 9.5+** (Gradle wrapper included)

### Compile
```powershell
# On Windows
.\gradlew build

# On Linux / macOS
./gradlew build
```

The output jar will be generated at:
`build/libs/ClarityEssentials-1.0.0.jar`

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.
