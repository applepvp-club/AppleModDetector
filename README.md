# ModDetector

> Packet-level cheat mod detection for Minecraft servers. Identifies disallowed client modifications using a translation key exploit — without any client-side agent.

![PacketEvents](https://img.shields.io/badge/PacketEvents-required-brightgreen)
![Paper/Spigot](https://img.shields.io/badge/Paper%20%2F%20Spigot-1.19%2B-brightgreen)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## How it works

The plugin exploits Minecraft's sign editing flow to probe the client's translation table. If a mod is installed, it replaces translation keys with its own strings — betraying its presence.

**Detection flow (at connection time):**

1. A `BlockUpdatePacket` is sent to place a virtual sign near the player
2. An `OpenSignEditorPacket` is sent, opening the sign editor on the client
3. A `SignUpdatePacket` is sent containing the mod's translation key as the sign text
4. The client echoes the sign content back — if the value differs from the default fallback, the mod is present

> The entire handshake is invisible to the player and occurs silently at connection time. No GUI is shown, no chat messages are sent during detection.

---

## Detected mods

The following mods are blocked out of the box. You can extend the list in `config.yml`.

| Mod | Translation Key |
|-----|----------------|
| Meteor Client | `key.meteor-client.open-gui` |
| Item Scroller | `itemscroller.gui.button.config_gui.generic` |
| Freecam | `key.freecam.toggle` |
| Accurate Block Placement | `text.autoconfig.accurateblockplacement.title` |

---

## Configuration

All settings live in `plugins/ModDetector/config.yml`.

```yaml
# Fallback value used to detect mod presence
default: "fallb"

# Message shown to the player on detection
# Supports MiniMessage, modern & legacy ampersand codes
kick-message:
  - "&cYou are using a cheat mod that is not allowed on [Your Server] (%mod%!), if you join"
  - "with this again you will be banned!"

# Mod translation keys to detect
disallowed-mods:
  "Meteor": "key.meteor-client.open-gui"
  "Item Scroller": "itemscroller.gui.button.config_gui.generic"
  "Freecam": "key.freecam.toggle"
  "Accurate Block Placement": "text.autoconfig.accurateblockplacement.title"

# Enable packet & result logging for debugging
debug: false
```

### Color code support

The `kick-message` field supports all three Minecraft color systems:

- **MiniMessage** — `<red>`, `<#ff4455>`
- **Modern ampersand** — `&c`, `&#ff4455`
- **Legacy ampersand** — `&c`, `&x&f&f&4&4&5&5`

---

## Adding custom mods

Any mod that registers Minecraft translation keys can be detected. Find the key in the mod's language files (usually in `assets/<modid>/lang/en_us.json`) and add it to `disallowed-mods`:

```yaml
disallowed-mods:
  "Your Mod Name": "your.mod.translation.key"
```

> **Note:** The `default` field must be set to a string that is *never* a valid translation output — the default `"fallb"` is safe for vanilla clients. Do not change it unless you understand the detection logic.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Paper / Spigot | 1.19+ |
| [PacketEvents](https://github.com/retrooper/packetevents) | Latest |
| Java | 17+ |

---

## Installation

1. Download and place `ModDetector.jar` into your `plugins/` folder
2. Download [PacketEvents](https://github.com/retrooper/packetevents/releases) and place it in `plugins/` as well
3. Start the server — `config.yml` will be generated automatically
4. Edit `config.yml` to configure your kick message and disallowed mods
5. Reload with `/reload` or restart the server

---

*Inspired by DonutSMP's detection approach. MIT License.*