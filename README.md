# Velocity Secure-Proxy Plugin

## Overview
This plugin allows **Offline-Mode players** and **Online-Mode players** to play together on a **Velocity Proxy running in Online Mode**. It uses a MySQL database to securely authenticate players and grant access to Offline players.

Additionally, the plugin features a **Votekick System (Beta)**, which enables players to vote to remove a specific player from the server. Players who are successfully votekicked are temporarily banned for a predefined duration.

---

## Requirements
- Velocity Proxy running in Online Mode
- A MySQL database
- Minecraft servers connected to the Velocity Proxy

---

## Installation
1. Download the latest version of the plugin from [Releases](https://gitlab.marylieh.social/md-public/secure-proxy/-/releases).
2. Place the `.jar` file in the `plugins` folder of your Velocity Proxy.
3. Restart the proxy to load the plugin.
4. Edit the generated `config.toml` file in the `plugins/SecureProxy` directory to configure the MySQL database and other settings.

---

## Configuration
The `config.toml` includes the following key settings:

```toml
[database]
host = "db.example.com"
port = "3306"
name = "example-db"
user = "root"
password = "secret-password"

[proxypass]
server = "lobby"

[features]
enableVoteKick = false
voteKickTimeInHours = 4
```

---

## Commands

| Command                                            | Description                                                                                       | Permission              |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------|-------------------------|
| /sw list                                           | Lists all Players that are currently on the secureproxy whitelist.                                | `secureproxy.whitelist` |
| /sw add <player> <desiredMode = online or offline> | Adds a Player to the secureproxy whitelist. The online mode type is important for login handling. | `secureproxy.whitelist` |
| /sw remove <player>                                | Removes a Player from the secureproxy whitelist.                                                  | `secureproxy.whitelist` |
| /vote <player>                                     | Initiates a new Votekick against the targeted Player.                                             | `secureproxy.votekick`  |
| /vote yes                                          | Votes `yes` for the current votekick.                                                             | `secureproxy.votekick`  | 
| /vote no                                           | Vote `no` for the current votekick.                                                               | `secureproxy.votekick`  |
| /vote cancel                                       | As the initiator you have the ability to cancel the votekick.                                     | `secureproxy.votekick`  |

---

## Votekick System (Beta)
The votekick system allows players to democratically remove disruptive players from the game.

### How it Works:
#### 1. Initiating a Votekick:
A player can start a Votekick against another player using the command:
```plaintext
/vote <player>
```

#### 2. Voting:
All players can cast their vote by using:
* `/vote yes` to vote in favor of kicking the player.
* `/vote no` to vote against the kick.

The time left of the votekick will be broadcast in an irregular rhythm.

#### 3. Outcome:
* If the majority votes "yes", the targeted player will be temporarily banned for the duration specified in the configuration.
* If the majority votes "no" or if the votekick fails to reach a majority within the time limit, the targeted player will not be removed.

#### 4. Cancelling a Votekick:
The votekick initiator can cancel the current votekick using the command:
```plaintext
/vote cancel
```

#### 5. Unbanning a Player:
If needed, a database administrator can be manually unban a player who was previously votekicked with the following SQL Statement:
```SQL
DELETE FROM bans WHERE uuid = '<PlayerUUID>';
```

---

## Beta Notice

The **Votekick System** is currently in Beta. Some issues may occur, and feedback is appreciated. Please report bugs or suggest improvements in the [issues section](https://gitlab.marylieh.social/md-public/secure-proxy/-/issues).

---

## License
This plugin is licensed under the [MIT License](./LICENSE).