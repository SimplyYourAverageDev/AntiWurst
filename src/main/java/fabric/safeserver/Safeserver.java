package fabric.safeserver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Safeserver implements ModInitializer {
	private static final String PASSWORD_FILE = "passwords.properties";
	private final Map<UUID, Boolean> loggedInPlayers = new HashMap<>();
	private final Map<UUID, BlockPos> playerJoinPositions = new HashMap<>();
	private Properties passwordProperties;

	@Override
	public void onInitialize() {
		// Load password properties from file
		passwordProperties = new Properties();
		try {
			File file = new File(PASSWORD_FILE);
			if (file.exists()) {
				FileReader reader = new FileReader(file);
				passwordProperties.load(reader);
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerSetPasswordCommand(dispatcher);
			registerLoginCommand(dispatcher);
		});

		// Register player join event
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			UUID playerId = player.getUuid();
			loggedInPlayers.put(playerId, false);
			playerJoinPositions.put(playerId, player.getBlockPos());
			player.changeGameMode(GameMode.SPECTATOR);
			System.out.println("Player " + player.getName().getString() + " joined the game.");

			// Send instructions to first-time players
			if (!hasPassword(player)) {
				player.sendMessage(Text.of("Welcome to the server! To play, you need to set a password."), false);
				player.sendMessage(Text.of("Use the command /setPassword <password> to set your password."), false);
				player.sendMessage(Text.of("After setting your password, use /login <password> to log in and start playing."), false);
			}else{
				player.sendMessage(Text.of("Please enter your password using /login <your password> start playing."), false);
			}
		});

		// Register player quit event
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			UUID playerId = player.getUuid();
			loggedInPlayers.remove(playerId);
			playerJoinPositions.remove(playerId);
			System.out.println("Player " + player.getName().getString() + " left the game.");
		});

		// Register server tick event
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				UUID playerId = player.getUuid();
				if (!loggedInPlayers.getOrDefault(playerId, false)) {
					BlockPos joinPos = playerJoinPositions.get(playerId);
					if (joinPos != null) {
						player.teleport(joinPos.getX(), joinPos.getY(), joinPos.getZ());
						player.changeGameMode(GameMode.SPECTATOR);
					}
				}
			}
		});
	}

	private void registerSetPasswordCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("setPassword")
				.then(CommandManager.argument("password", StringArgumentType.greedyString())
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayer();
							String password = StringArgumentType.getString(context, "password");
							if (hasPassword(player)) {
								player.sendMessage(Text.of("You have already set a password and cannot modify it."), false);
								System.out.println("Player " + player.getName().getString() + " tried to set a password but already has one.");
							} else {
								setPassword(player, password);
							}
							return 1;
						})));
	}

	private void registerLoginCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("login")
				.then(CommandManager.argument("password", StringArgumentType.greedyString())
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayer();
							String password = StringArgumentType.getString(context, "password");
							login(player, password);
							return 1;
						})));
	}

	private boolean hasPassword(ServerPlayerEntity player) {
		UUID playerId = player.getUuid();
		String playerIdString = playerId.toString();
		return passwordProperties.containsKey(playerIdString);
	}

	private void setPassword(ServerPlayerEntity player, String password) {
		UUID playerId = player.getUuid();
		String playerIdString = playerId.toString();
		passwordProperties.setProperty(playerIdString, password);
		try {
			FileWriter writer = new FileWriter(PASSWORD_FILE);
			passwordProperties.store(writer, "Player Passwords");
			writer.close();
			player.sendMessage(Text.of("Password set successfully!"), false);
			player.sendMessage(Text.of("You can now log in using /login <password>."), false);
			System.out.println("Player " + player.getName().getString() + " set their password.");
		} catch (IOException e) {
			e.printStackTrace();
			player.sendMessage(Text.of("Failed to set password. Please try again."), false);
		}
	}

	private void login(ServerPlayerEntity player, String password) {
		UUID playerId = player.getUuid();
		String playerIdString = playerId.toString();
		String storedPassword = passwordProperties.getProperty(playerIdString);
		if (storedPassword != null && storedPassword.equals(password)) {
			loggedInPlayers.put(playerId, true);
			player.changeGameMode(GameMode.SURVIVAL);
			player.sendMessage(Text.of("Login successful! Enjoy playing on the server."), false);
			System.out.println("Player " + player.getName().getString() + " logged in successfully.");
		} else {
			player.sendMessage(Text.of("Invalid password. Please try again."), false);
			System.out.println("Player " + player.getName().getString() + " entered an invalid password.");
		}
	}
}
