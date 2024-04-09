# SafeServer Fabric Mod

SafeServer is a Minecraft Fabric mod that adds a password-based login system to your Minecraft server. Players are required to set a password and log in before they can start playing in survival mode.

## Features

- Password-based login system: Players must set a password and log in to play in survival mode.
- Spectator mode for unauthenticated players: Players who haven't logged in are placed in spectator mode and cannot interact with the world.
- Secure password storage: Player passwords are stored in a separate file and are not accessible to other players.
- Easy-to-use commands: Players can set their password using `/setPassword <password>` and log in using `/login <password>`.
- First-time player instructions: New players are automatically sent instructions on how to set their password and log in.

## Installation

1. Make sure you have Fabric Loader and Fabric API installed on your Minecraft server.
2. Download the latest release of the SafeServer mod from the [releases page]([https://github.com/your-repo/releases](https://modrinth.com/mod/antiwurst/versions#all-versions)).
3. Place the downloaded JAR file in the `mods` directory of your Minecraft server.
4. Start or restart your Minecraft server.

## Usage

### Setting a Password

To set a password, players can use the `/setPassword <password>` command. For example:

```
/setPassword mySecurePassword123
```

Players can only set their password once and cannot modify it afterward.

### Logging In

To log in and start playing in survival mode, players need to use the `/login <password>` command with their previously set password. For example:

```
/login mySecurePassword123
```

If the entered password matches the stored password, the player will be logged in and placed in survival mode. Otherwise, an error message will be displayed.

### First-Time Players

When a player joins the server for the first time, they will receive instructions on how to set their password and log in. They will be placed in spectator mode until they successfully log in.

## Configuration

The mod creates a `passwords.properties` file in the server directory to store player passwords. Each player's password is associated with their UUID to ensure unique identification.

## License

This mod is released under the [MIT License](LICENSE).

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvements, please open an issue or submit a pull request on the [GitHub repository](https://github.com/your-repo).

## Credits

- Developed by YourAverageDev
- Built with Fabric Loader and Fabric API

Enjoy playing on a safer Minecraft server with the SafeServer mod!
