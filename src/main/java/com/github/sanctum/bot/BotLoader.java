package com.github.sanctum.bot;

import com.github.sanctum.bot.game.BlackJack;
import com.github.sanctum.bot.game.UserCardholder;
import com.github.sanctum.bot.listener.Handler;
import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.entities.VoiceChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class BotLoader {

	private static final Set<BlackJack> games = new HashSet<>();

	public static void start(ClanAddon addon) {
		Schedule.sync(() -> {
			Listener l = new Handler();
			LabyrinthProvider.getService(Service.VENT).subscribe(addon.getPlugin(), l);
			DiscordSRV.getPlugin().getJda().addEventListener(l);

			LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).repeat(task -> {
				VoiceChannel channel = DiscordSRV.getPlugin().getJda().getVoiceChannelById(Bot.ONLINE);
				if (channel != null) {
					channel.getManager().setName("├ | Online: " + Bukkit.getOnlinePlayers().size()).queue();
				}
			}, HUID.randomID().toString(), TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(2));

		}).waitReal(10);
	}

	public static BlackJack getGame(User user) {
		return games.stream().filter(b -> b.getPlayer1().getId().equals(user.getId()) || b.getPlayer2().getId().equals(user.getId())).findFirst().orElse(null);
	}

	public static BlackJack newGame(User player1) {
		if (getGame(player1) != null) return getGame(player1);
		BlackJack game = new BlackJack();
		game.setPlayer1(new UserCardholder(player1));
		games.add(game);
		return game;
	}

	public static BlackJack newGame(User player1, User player2) {
		if (getGame(player1) != null) {
			if (getGame(player1).getPlayer2().getId().equals(player2.getId())) {
				return getGame(player1);
			}
		}
		BlackJack game = new BlackJack();
		game.setPlayer1(new UserCardholder(player1));
		game.setPlayer2(new UserCardholder(player2));
		games.add(game);
		return game;
	}

	public static String getLabel(int num) {
		switch (num) {
			case 0:
				return "zero";
			case 1:
				return "one";
			case 2:
				return "two";
			case 3:
				return "three";
			case 4:
				return "four";
			case 5:
				return "five";
			case 6:
				return "six";
			case 7:
				return "seven";
			case 8:
				return "eight";
			case 9:
				return "nine";
		}
		return null;
	}


	public static void endGame(BlackJack blackJack) {
		games.remove(blackJack);
	}
}
