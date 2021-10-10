package com.github.sanctum.bot.api;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.library.Deployable;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class UserLink {

	public static final Set<UserLink> SET = new HashSet<>();
	public static final Map<String, OfflinePlayer> DATA = new HashMap<>();

	private final String id;

	public UserLink(String id) {
		this.id = id;
	}

	public final String getId() {
		return id;
	}

	public abstract String getLink();

	public final OfflinePlayer toPlayer() {
		return Bukkit.getOfflinePlayer(UUID.fromString(getLink()));
	}

	public final User toUser() {
		return DiscordSRV.getPlugin().getJda().getUserById(getLink());
	}

	public final void save(ClanAddon addon) {
		FileManager file = addon.getFile(FileType.JSON, "discord");
		if (!file.getRoot().exists()) {
			try {
				file.getRoot().create();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		file.write(t -> t.set(getId(), getLink()));

	}

	public static UserLink get(@NotNull OfflinePlayer player) {
		return SET.stream().filter(l -> l.getId().equals(player.getUniqueId().toString())).findFirst().orElse(null);
	}

	public static UserLink get(@NotNull User user) {
		return SET.stream().filter(l -> l.getId().equals(user.getId())).findFirst().orElse(null);
	}

	public static OfflinePlayer identify(String key) {
		return DATA.get(key);
	}

	public static Deployable<Void> identify(Player player) {
		final String id = new RandomID(24).generate().toLowerCase();
		return Deployable.of(null, unused -> {
			DATA.put(id, player);
			new FancyMessage().then("Copy this code and paste it into the #commands channel:").then(" ").then(id).color(Color.ORANGE).style(ChatColor.UNDERLINE, ChatColor.ITALIC).hover("Click to copy the code.").copy(id).send(player).deploy();
		});
	}

	public static Deployable<Void> add(@NotNull OfflinePlayer player, @NotNull User user) {
		return Deployable.of(null, unused -> {
			UserLink n = new UserLink(player.getName()) {
				@Override
				public String getLink() {
					return user.getId();
				}
			};
			SET.add(n);
			UserLink l = new UserLink(user.getId()) {
				@Override
				public String getLink() {
					return player.getUniqueId().toString();
				}
			};
			SET.add(l);
		});
	}


}
