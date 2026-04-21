> Official release page for Vanguard — a lightweight staff activity tracker built for modern Paper/Spigot environments.

# Vanguard v1.0.2
by NathanFCS

Vanguard is a lightweight staff activity tracker for Paper/Spigot servers.

Built for server teams that want a clean, reliable way to monitor staff performance without unnecessary complexity.

---

## Overview

Vanguard automatically tracks staff activity the moment they join the server. No setup required from staff members beyond having the correct permission.

- Tracks active time per session and lifetime
- Detects AFK status automatically
- Logs moderation actions
- Provides a clean leaderboard
- Admin reset tools with confirmation flow

---

## Requirements

- Paper or Spigot **1.21+**
- Java **21+**
- No additional dependencies

---

## Installation

1. Place `Vanguard-1.0.2.jar` in your `plugins/` folder
2. Restart the server
3. Assign permissions as needed
4. Done

---

## Commands

### Staff
Permission node: `vanguard.staff`

| Command | Description |
|---------|-------------|
| `/vanguard help` | Show command menu |
| `/vanguard stats [player]` | View staff performance data |
| `/vanguard top` | View staff leaderboard |
| `/afk` | Toggle tracking pause |

### Admin
Permission node: `vanguard.admin`

| Command | Description |
|---------|-------------|
| `/vanguard reset <player>` | Clear one staff profile |
| `/vanguard reset all` | Clear all tracked profiles |

Aliases: `/vg` for `/vanguard`

---

## Permissions

| Node | Description | Default |
|------|-------------|---------|
| `vanguard.staff` | Access to tracking and stats commands | OP |
| `vanguard.admin` | Access to reset commands | OP |

---

## Tracking System

- Tracking starts **automatically** when a staff member joins
- Tracking pauses when `/afk` is used
- Tracking resumes automatically on movement, chat, or any command
- Tracking stops and saves when the staff member leaves
- Data persists through server restarts

---

## AFK System

- `/afk` toggles tracking pause manually
- When paused, an action bar notification is shown
- Tracking resumes automatically on:
  - Player movement
  - Chat message
  - Any command other than `/afk`

---

## Reset System

All reset commands require a **two-step confirmation** within 10 seconds.

```text
/vg reset Aaron      — first prompt: shows warning
/vg reset Aaron      — second prompt: executes reset

/vg reset all        — first prompt: shows warning
/vg reset all        — second prompt: executes reset
```

If the confirmation window expires, the request must be restarted.

---

## Stats Display

```text
/vg stats Aaron

 Vanguard — Aaron

 ▸ Status: ACTIVE
 ▸ Active Time: 3h 24m
 ▸ Session: 42m 10s
 ▸ Actions: 7
```

| Field | Description |
|-------|-------------|
| Status | Current state: ACTIVE, AFK, IDLE, OFFLINE |
| Active Time | Total cumulative active time across all sessions |
| Session | Active time in the current session only |
| Actions | Total moderation commands executed with a target |

---

## Moderation Action Tracking

The following commands are tracked when used **with a target**:

`ban` `tempban` `kick` `mute` `tempmute` `warn` `unmute` `freeze` `jail`

Commands used without a target are ignored.

---

## Changelog

```text
v1.0.2 - reset system
       - /vanguard reset <player> with two-step confirmation
       - /vanguard reset all with two-step confirmation
       - fixed action count: commands without target no longer counted
       - action bar color updated

v1.0.1 - bug fixes
       - status now correctly shows ACTIVE when online
       - AFK auto-resume on movement and chat
       - action bar notification during AFK
       - dynamic version in console messages and help menu

v1.0.0 - initial release
       - automatic staff tracking on join
       - AFK pause system
       - moderation action tracking
       - /vanguard stats
       - /vanguard top
       - /afk command
```

---

## Planned

- [ ] Weekly leaderboard reset
- [ ] Discord webhook integration
- [ ] Per-world tracking
- [ ] Detailed action log

---

## License

This project is licensed under the **MIT License**.

---

## Notes

- This is a standalone **Java plugin**, no additional dependencies required
- Designed for **Paper/Spigot 1.21+** environments
- Data is stored in `plugins/Vanguard/data.yml`
- Lightweight and easy to maintain

---

## Official Links

### Profiles
- **GitHub:** https://github.com/NTHFCS
- **SpigotMC Profile:** https://www.spigotmc.org/members/nathanfcs.2521802/
- **skUnity Profile:** https://forums.skunity.com/members/nathanfcs.40131/
- **Modrinth Profile:** https://modrinth.com/user/NTHFCS

### Resource Pages
- **GitHub Repository:** https://github.com/NTHFCS/Vanguard
- **SpigotMC:** [Cooming Soon]
- **Modrinth:** [Cooming Soon]

### Community
- **Discord:** https://discord.gg/V8dpGdsMeT

---

## Support

For setup help, bug reports, or suggestions:
- Open a discussion on SpigotMC or Modrinth
- Join the official Discord server

---

## Maintained By

**NathanFCS Studio**
Releases • Support • Product Development
