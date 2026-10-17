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
* **14 Core Feature Actions (2 Columns)**:
  * **Left Column**: Homes, Auction, Quick Buy, Orders, Sell, Teleport, Leaderboards
  * **Right Column**: RTP, RTP Queue, Friends, Shard Shop, Pay, Stats, Settings
  * **Bottom**: Clean exit button to return to gameplay.
* Clean, minimalist design using white typography without distracting clutter.

### 🏠 3. Native Dialog Home Management & Digital Currency Limit (`/home`, `/sethome`, `/delhome`, `/homelimit`)
* All home operations (listing, creating, teleporting, deleting) operate via native dialogs.
* **Interactive Text Input**: `/sethome` (or clicking "+ New Home") opens a native text dialog to name the home.
* **Deletion Confirmation**: Prompts a confirmation dialog before deleting to prevent accidental loss.
* **Digital Currency / Numerical Home Limit Quota**:
  * Player home limits can be incremented, decremented, or set dynamically via commands (`/homelimit give/take/set <player> <amount>`).
  * Total Allowed Homes = Base Quota (from rank/default) + Extra Bonus Limit slots.
  * Perfect for store packages, voting rewards, or token purchases!

### 💬 4. Private Messaging & Admin SocialSpy (`/msg`, `/r`, `/socialspy`)
* Fast, private 1-on-1 player messaging (`/msg <player> <message>`, aliases: `/tell`, `/w`, `/whisper`).
* Quick reply command (`/r <message>`).
* Privacy-aware: respects individual player `/menu` privacy settings (Anyone, Friends Only, Off).
* **Staff Social Spy (`/socialspy`)**: Allows authorized administrators (`clarityessentials.socialspy`) to monitor player private messages in real-time. Preferences persist across restarts.

### 🚪 5. Custom Join & Leave Messages (Default + VIP Permissions)
* Suppresses vanilla join/quit notifications and broadcasts customizable MiniMessage announcements.
* **VIP Custom Join Messages**: Priority-based permission checks (`clarityessentials.join.<group>`) allow ranks (Owner, Admin, VIP) to have unique entrance broadcasts.
* Individual player control: players can filter join/leave broadcasts in `/menu` Settings (Anyone, Friends Only, or Off).

### 👥 6. Friends System
* Add friends with instant player validation (prevents adding yourself or non-existent players).
* Real-time online/offline player status.
* Quick actions for each friend:
  * Send instant Teleport Request (`/tpa`).
  * Send Money via quick pay dialog.
  * Friendly-fire protection toggle to avoid harming teammates.
  * Remove friend with a native confirmation dialog.

### 🌀 7. Teleportation & Player List (`/tpa`, `/tpahere`, `/tptoggle`)
* Interactive clickable chat messages for incoming teleport requests (`[ACCEPT]` and `[DENY]`).
* Request timeout handling (default: 60s) and warmup countdown with movement cancellation.
* Toggle incoming requests on/off via `/tptoggle`.
* Quick teleport list in the dialog menu to quickly request teleport to saved players.

### 💰 8. Quick Pay System (`/pay`)
* Vault Economy integration with transaction validation and balance checks.
* Saved recipient list for quick transfers.
* Preset denomination buttons and native text input dialog for custom transfer amounts.

### 🏷️ 9. Smart Nickname System (`/nick`, `/realname`)
* Native text dialog to preview, change, or reset nicknames.
* Strict regex validation (`allowed-nicks-regex: "^[a-zA-Z0-9_]{3,16}$"`) prevents exploit characters.
* Dedicated permissions for colors (`clarityessentials.nick.color`) and unsafe character bypass (`clarityessentials.nick.allowunsafe`).
* Automatic synchronization across chat and tablist.
* `/realname <nickname>` command to resolve a player's real username.

### ⚙️ 10. Player Settings & Leaderboards
* Interactive native toggles for player preferences: Chat (Public Chat filter), Notifications, PvP, Visuals, Privacy, and Scoreboard.
* Real-time leaderboards across multiple categories: Balance, Deaths, Playtime, Blocks Placed, Blocks Broken, and Mobs Killed.

### 🌐 11. Multi-Language Support (ClarityLangCore Integration)
* Automatically connects to **ClarityLangCore** when available, enabling per-player dynamic language switching (`/lang`).
* Includes 9 bundled language localizations: English (`en_US`), Indonesian (`id_ID`), German (`de_DE`), Spanish (`es_ES`), French (`fr_FR`), Japanese (`ja_JP`), Portuguese (`pt_BR`), Russian (`ru_RU`), and Chinese (`zh_CN`).
* Self-contained standalone fallback when running without ClarityLangCore.

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
| `/tptoggle` | - | Toggle incoming teleport requests | `clarityessentials.command.tptoggle` |
| `/ce reload` | `/clarityessentials` | Reload plugin configuration and data | `clarityessentials.admin` |

### Special Permissions:
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
# ESC (Pause Menu) Custom Button & Native Dialog Menu
esc-menu:
  enabled: true
  button-text: "Clarity Menu"   # Button text on ESC Game Menu
  dialog-title: "Clarity Menu"  # Title header inside the dialog
  columns: 2
  commands:
    rtp: "rtp"
    rtp-queue: "rtp queue"
    auction: "ah"
    quick-buy: "buy"
    orders: "orders"
    shard-shop: "shards"
    sell: "sell"
    stats: "stats"

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
