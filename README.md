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

### 🏠 3. Native Dialog Home Management (`/home`, `/sethome`, `/delhome`)
* All home operations (listing, creating, teleporting, deleting) operate via native dialogs.
* **Interactive Text Input**: `/sethome` (or clicking "+ New Home") opens a native text dialog to name the home.
* **Deletion Confirmation**: Prompts a confirmation dialog before deleting to prevent accidental loss.
* Rank-based home quotas configured via permissions (`clarityessentials.homes.<rank>`) or unlimited (`clarityessentials.homes.unlimited`).

### 👥 4. Friends System
* Add friends with instant player validation (prevents adding yourself or non-existent players).
* Real-time online/offline player status.
* Quick actions for each friend:
  * Send instant Teleport Request (`/tpa`).
  * Send Money via quick pay dialog.
  * Friendly-fire protection toggle to avoid harming teammates.
  * Remove friend with a native confirmation dialog.

### 🌀 5. Teleportation & Player List (`/tpa`, `/tpahere`, `/tptoggle`)
* Interactive clickable chat messages for incoming teleport requests (`[ACCEPT]` and `[DENY]`).
* Request timeout handling (default: 60s) and warmup countdown with movement cancellation.
* Toggle incoming requests on/off via `/tptoggle`.
* Quick teleport list in the dialog menu to quickly request teleport to saved players.

### 💰 6. Quick Pay System (`/pay`)
* Vault Economy integration with transaction validation and balance checks.
* Saved recipient list for quick transfers.
* Preset denomination buttons and native text input dialog for custom transfer amounts.

### 🏷️ 7. Smart Nickname System (`/nick`, `/realname`)
* Native text dialog to preview, change, or reset nicknames.
* Strict regex validation (`allowed-nicks-regex: "^[a-zA-Z0-9_]{3,16}$"`) prevents exploit characters.
* Dedicated permissions for colors (`clarityessentials.nick.color`) and unsafe character bypass (`clarityessentials.nick.allowunsafe`).
* Automatic synchronization across chat and tablist.
* `/realname <nickname>` command to resolve a player's real username.

### 💬 8. Modern Chat & Group Formatting
* Full Adventure MiniMessage support including RGB Hex (`<#RRGGBB>`), gradients (`<gradient:#ff5555:#ffaa00>`), and tags.
* Primary LuckPerms group matching for custom prefixes, suffixes, and formats.
* Native PlaceholderAPI (PAPI) expansion support.
* Permission protection (`clarityessentials.chat.color`) prevents unauthorized styling.

### ⚙️ 9. Player Settings & Leaderboards
* Interactive native toggles for player preferences: Chat, Notifications, PvP, Visuals, Privacy, and Scoreboard.
* Real-time leaderboards across multiple categories: Balance, Deaths, Playtime, Blocks Placed, Blocks Broken, and Mobs Killed.

### 🌐 10. Multi-Language Support (ClarityLangCore Integration)
* Automatically connects to **ClarityLangCore** when available, enabling per-player dynamic language switching (`/lang`).
* Includes 9 bundled language localizations: English (`en_US`), Indonesian (`id_ID`), German (`de_DE`), Spanish (`es_ES`), French (`fr_FR`), Japanese (`ja_JP`), Portuguese (`pt_BR`), Russian (`ru_RU`), and Chinese (`zh_CN`).
* Self-contained standalone fallback when running without ClarityLangCore.

---

## 📜 Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/menu` | `/dashboard`, `/cp` | Open Centralized Player Dashboard | `clarityessentials.command.menu` |
| `/nick <name\|reset>` | `/nickname` | Set or reset your custom nickname | `clarityessentials.command.nick` |
| `/realname <nickname>` | - | Lookup real username from nickname | `clarityessentials.command.realname` |
| `/tpa <player>` | - | Send teleport request to a player | `clarityessentials.command.tpa` |
| `/tpahere <player>` | - | Request a player to teleport to you | `clarityessentials.command.tpahere` |
| `/tpaccept [player]` | `/tpyes` | Accept a pending teleport request | `clarityessentials.command.tpaccept` |
| `/tpdeny [player]` | `/tpno` | Decline a pending teleport request | `clarityessentials.command.tpdeny` |
| `/tptoggle` | - | Toggle incoming teleport requests | `clarityessentials.command.tptoggle` |
| `/home [name]` | `/homes` | Open Home Dialog or teleport to a home | `clarityessentials.command.home` |
| `/sethome [name]` | - | Save current location as a home | `clarityessentials.command.sethome` |
| `/delhome [name]` | `/deletehome` | Delete a saved home with confirmation | `clarityessentials.command.delhome` |
| `/ce reload` | `/clarityessentials` | Reload plugin configuration and data | `clarityessentials.admin` |

### Special Permissions:
* `clarityessentials.chat.color`: Permission to use color and MiniMessage tags in chat.
* `clarityessentials.nick.color`: Permission to format nicknames with colors.
* `clarityessentials.nick.allowunsafe`: Permission to bypass regex and length limits on nicknames.
* `clarityessentials.nick.others`: Permission to modify other players' nicknames.
* `clarityessentials.tpa.bypasswarmup`: Permission to teleport instantly without warmup delay.
* `clarityessentials.homes.unlimited`: Grants unlimited home creation slots.
* `clarityessentials.homes.<rank>`: Grants home limit matching specific rank group defined in `config.yml`.

---

## ⚙️ Configuration

The main configuration file is located at `plugins/ClarityEssentials/config.yml`.

```yaml
# ESC (Pause Menu) Custom Button & Native Dialog Menu
# Note: Minecraft client syncs dialog registry and tags during login.
# Server restart and client reconnect is required when changing these settings.
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
