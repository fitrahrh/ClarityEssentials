# 🌟 ClarityEssentials

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Paper-26.3+-red.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge)](https://openjdk.org)

**ClarityEssentials** adalah plugin core modern yang dirancang khusus untuk **Clarity Network** sebagai pengganti fitur-fitur esensial lama (seperti EssentialsX). Plugin ini memanfaatkan sepenuhnya **Paper Native Dialog API** dan **Adventure MiniMessage**, sehingga seluruh interaksi antarmuka (UI) disajikan melalui dialog native resmi client Minecraft tanpa menggunakan Chest GUI / inventory hack.

---

## ✨ Fitur Utama

### 💬 1. Custom Chat & Group Formatting
* Ditenagai oleh **Adventure MiniMessage** dengan dukungan penuh untuk format warna HEX (`<#RRGGBB>`), gradien (`<gradient:#color1:#color2>`), dan styling teks modern.
* Integrasi **LuckPerms Group**: Format chat otomatis disesuaikan dengan primary group pemain (misalnya `owner`, `admin`, `vip`, `default`).
* Dukungan **PlaceholderAPI (PAPI)** secara native pada template chat (`{prefix}`, `{suffix}`, `{player}`, `{nickname}`, `{message}`, dan placeholder `%...%`).
* Perlindungan keamanan: Pemain tanpa izin `clarityessentials.chat.color` tidak dapat menyisipkan tag MiniMessage eksploitatif.

### 🏷️ 2. Smart Nickname System (`/nick`, `/realname`)
* Pemain dapat mengganti display name mereka secara dinamis.
* **Validasi Regex Ketat** (`allowed-nicks-regex: "^[a-zA-Z0-9_]{3,16}$"`) untuk mencegah penyalahgunaan simbol dan font aneh.
* Opsi bypass izin (`clarityessentials.nick.allowunsafe`) untuk Administrator & VIP.
* Dukungan warna MiniMessage (`clarityessentials.nick.color`).
* Sinkronisasi nama pada display name chat dan tablist.
* Perintah `/realname <nickname>` untuk mendeteksi identitas username asli pemain.

### 🌀 3. Teleportation Request System (`/tpa`, `/tpahere`, `/tptoggle`)
* Manajemen permintaan teleportasi antar pemain (`/tpa`, `/tpahere`, `/tpaccept`, `/tpdeny`).
* Pesan interaktif chat yang dapat langsung diklik (`[TERIMA]` dan `[TOLAK]`).
* Batas waktu kedaluwarsa request (default: 60 detik).
* Hitung mundur teleport warmup (default: 3 detik) dengan deteksi pembatalan jika pemain bergerak (movement cancellation).
* Perintah `/tptoggle` untuk mengaktifkan atau menonaktifkan permintaan teleportasi masuk.

### 🏠 4. Native Dialog Home Management (`/home`, `/sethome`, `/delhome`)
* Seluruh manajemen home (melihat daftar home, teleportasi, dan hapus) dikelola melalui **Paper Native Dialog API**.
* Tidak menggunakan inventory chest GUI sama sekali!
* Batas jumlah home per pemain dapat disesuaikan berdasarkan permission rank (`clarityessentials.homes.<rank>`).
* Dialog konfirmasi native sebelum menghapus home untuk mencegah penghapusan yang tidak sengaja.

### 🧭 5. Centralized Native Player Dashboard (`/menu`, `/dashboard`)
* Hub dialog interaktif native yang menyatukan seluruh fitur plugin dalam satu layar:
  - Status pemain (Rank, Ping, Nickname, Status TPA, Jumlah Home).
  - Tombol pintas **Home Manager**.
  - Tombol pintas **Nickname Settings**.
  - Tombol langsung untuk **Toggle TPA** (On/Off).
  - Integrasi langsung dengan **ClarityLangCore Language Dialog** (`/lang`).

---

## 🌐 Integrasi ClarityLangCore

Plugin ini terhubung langsung dengan **`ClarityLangCore`** melalui `ClarityLangAPI`:
* Bundle bahasa terdaftar secara otomatis di `onEnable()`.
* Mendukung multi-bahasa dinamis per-pemain (`id_ID.yml`, `en_US.yml`).
* Dilengkapi dengan fallback internal mandiri jika dijalankan tanpa `ClarityLangCore`.

---

## 📜 Daftar Perintah & Izin (Permissions)

| Perintah | Alias | Deskripsi | Izin (Permission) |
| :--- | :--- | :--- | :--- |
| `/menu` | `/dashboard`, `/cp` | Membuka Player Dashboard Dialog | `clarityessentials.command.menu` |
| `/nick <nama\|reset>` | `/nickname` | Mengganti atau mereset nama panggilan | `clarityessentials.command.nick` |
| `/realname <nick>` | - | Melihat username asli pemain bernickname | `clarityessentials.command.realname` |
| `/tpa <player>` | - | Mengirim permintaan teleportasi ke pemain | `clarityessentials.command.tpa` |
| `/tpahere <player>` | - | Mengajak pemain berteleportasi ke lokasi Anda | `clarityessentials.command.tpahere` |
| `/tpaccept [player]` | `/tpyes` | Menerima permintaan teleportasi | `clarityessentials.command.tpaccept` |
| `/tpdeny [player]` | `/tpno` | Menolak permintaan teleportasi | `clarityessentials.command.tpdeny` |
| `/tptoggle` | - | Mengaktifkan/menonaktifkan TPA masuk | `clarityessentials.command.tptoggle` |
| `/home [name]` | `/homes` | Membuka Home Dialog atau teleportasi ke home | `clarityessentials.command.home` |
| `/sethome [name]` | - | Menyimpan lokasi saat ini sebagai home | `clarityessentials.command.sethome` |
| `/delhome [name]` | `/deletehome` | Menghapus home tersimpan via dialog konfirmasi | `clarityessentials.command.delhome` |
| `/ce reload` | `/clarityessentials`, `/essentials` | Memuat ulang konfigurasi dan data plugin | `clarityessentials.admin` |

### Izin Khusus Tambahan:
* `clarityessentials.chat.color`: Izin menggunakan tag warna MiniMessage di chat.
* `clarityessentials.nick.color`: Izin menggunakan warna pada nickname.
* `clarityessentials.nick.allowunsafe`: Izin bypass batasan karakter dan regex nickname.
* `clarityessentials.nick.others`: Izin mengganti nickname pemain lain.
* `clarityessentials.tpa.bypasswarmup`: Izin teleportasi instan tanpa delay hitung mundur.
* `clarityessentials.homes.unlimited`: Izin jumlah home tanpa batas.
* `clarityessentials.homes.<rank>`: Izin kuota home sesuai konfigurasi `config.yml`.

---

## 🛠️ Build & Kompilasi

Proyek ini dibangun menggunakan **JDK 25** dan **Gradle Kotlin DSL**:

```powershell
# Build plugin JAR
.\gradlew build
```

File JAR hasil kompilasi terletak di:
`build/libs/ClarityEssentials-1.0.0.jar`
