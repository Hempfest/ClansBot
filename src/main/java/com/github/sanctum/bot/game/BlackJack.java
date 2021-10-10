package com.github.sanctum.bot.game;

import com.github.sanctum.bot.Bot;
import com.github.sanctum.bot.BotLoader;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Task;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BlackJack {

	private Cardholder player1;
	private Cardholder player2;
	private long started;
	private final HUID id;

	public BlackJack() {
		this.id = HUID.randomID();
	}

	public boolean isRunning() {
		return LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).get("blackjack-" + id.toString()) != null;
	}

	public boolean isComplete() {
		return getPlayer1() != null && getPlayer1().getPoints() >= 21 || getPlayer2() != null && getPlayer2().getPoints() >= 21;
	}

	public boolean isDraw() {
		return getPlayer1() != null && getPlayer2() != null && getPlayer1().getPoints() == getPlayer2().getPoints();
	}

	public Cardholder getWinner() {
		if (!isDraw() && isComplete()) {
			List<Cardholder> holders = new ArrayList<>(Arrays.asList(player1, player2));
			return holders.stream().filter(c -> c.getPoints() <= 21).max(Comparator.comparingInt(Cardholder::getPoints)).orElse(null);
		}
		return null;
	}

	public TextChannel getChannel() {
		return DiscordSRV.getPlugin().getJda().getTextChannelById(Bot.COMMANDS);
	}

	public void startSolo() {
		if (!isRunning()) {
			started = System.currentTimeMillis();
			LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).repeat(task -> {

				TimeWatch.Recording r = TimeWatch.Recording.subtract(this.started);
				if (r.getMinutes() == 1) {
					if (getPlayer1().getPoints() == 0 && getPlayer2().getPoints() == 0) {
						getChannel().sendMessage("No winner was decided. Game inactivity detected, shutting down.").queue();
						getPlayer1().setTurn(false);
						getPlayer2().setTurn(false);
						task.cancel();
						LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).purge();
						BotLoader.endGame(BlackJack.this);
						return;
					}
				}

				if (BlackJack.this.isComplete()) {
					//TODO announce game over give out money
					getPlayer1().setTurn(false);
					getPlayer2().setTurn(false);
					task.cancel();
					LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).purge();
					BotLoader.endGame(BlackJack.this);
					return;
				}
				if (!getPlayer1().isUser()) {
					if (getPlayer1().isTurn()) {

						Task waiting = LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).get("blackjack-" + id.toString() + "-waiting");

						if (waiting == null) {
							LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).wait(() -> {
								String name = getPlayer1().getId();
								Card card = Card.random();

								int chance = new Random().nextInt(2);

								if (card.getWorth() + getPlayer2().getPoints() > 21 && chance == 1) {
									int total = getPlayer1().getPoints();
									EmbedBuilder msg = new EmbedBuilder();
									int first = Integer.parseInt(String.valueOf(total).split("")[0]);
									int first1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[0]);
									if (String.valueOf(total).length() == 2) {
										int last = Integer.parseInt(String.valueOf(total).split("")[1]);
										msg.addField(getPlayer1().getId(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
										if (String.valueOf(getOpponent(getPlayer1()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									} else {
										msg.addField(getPlayer1().getId(), ":" + BotLoader.getLabel(first) + ":", true);
										if (String.valueOf(getOpponent(getPlayer1()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
									msg.setTitle(name + " **STAND**");
									msg.setColor(getPlayer1().color());
									msg.setDescription("**STAND**");
									getChannel().sendMessage(msg.build()).queue();
								} else {

									getPlayer1().add(card);
									int total = getPlayer1().getPoints();
									EmbedBuilder msg = new EmbedBuilder();
									int first = Integer.parseInt(String.valueOf(total).split("")[0]);
									int first1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[0]);
									if (String.valueOf(total).length() == 2) {
										int last = Integer.parseInt(String.valueOf(total).split("")[1]);
										msg.addField(getPlayer1().getId(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
										if (String.valueOf(getOpponent(getPlayer1()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									} else {
										msg.addField(getPlayer1().getId(), ":" + BotLoader.getLabel(first) + ":", true);
										if (String.valueOf(getOpponent(getPlayer1()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer1()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer1()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
									if (total > 21) {
										msg.setTitle(name + " ***Lost***");
									} else {
										msg.setTitle(name + " **HIT**");
									}
									msg.setColor(getPlayer1().color());
									msg.setDescription("**HIT** card " + card);
									getChannel().sendMessage(msg.build()).queue();
								}
								getPlayer1().setTurn(false);
								getPlayer2().setTurn(true);
							}, "blackjack-" + id.toString() + "-waiting", TimeUnit.SECONDS.toMillis(3));
						}  //getChannel().sendTyping().queue();

					}
				}
				if (!getPlayer2().isUser()) {
					if (getPlayer2().isTurn()) {

						Task waiting = LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).get("blackjack-" + id.toString() + "-waiting");

						if (waiting == null) {
							LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).wait(() -> {
								String name = getPlayer2().getId();
								Card card = Card.random();

								int chance = new Random().nextInt(2);

								if (card.getWorth() + getPlayer2().getPoints() > 21 && chance == 1) {
									int total = getPlayer2().getPoints();
									EmbedBuilder msg = new EmbedBuilder();
									int first = Integer.parseInt(String.valueOf(total).split("")[0]);
									int first1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[0]);
									if (String.valueOf(total).length() == 2) {
										int last = Integer.parseInt(String.valueOf(total).split("")[1]);
										msg.addField(getPlayer2().getId(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
										if (String.valueOf(getOpponent(getPlayer2()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									} else {
										msg.addField(getPlayer2().getId(), ":" + BotLoader.getLabel(first) + ":", true);
										if (String.valueOf(getOpponent(getPlayer2()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
									msg.setTitle(name + " **STAND**");
									msg.setColor(getPlayer2().color());
									msg.setColor(0xf54245);
									msg.setDescription("**STAND**");
									getChannel().sendMessage(msg.build()).queue();
								} else {
									getPlayer2().add(card);
									int total = getPlayer2().getPoints();
									EmbedBuilder msg = new EmbedBuilder();
									int first = Integer.parseInt(String.valueOf(total).split("")[0]);
									int first1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[0]);
									if (String.valueOf(total).length() == 2) {
										int last = Integer.parseInt(String.valueOf(total).split("")[1]);
										msg.addField(getPlayer2().getId(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
										if (String.valueOf(getOpponent(getPlayer2()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									} else {
										msg.addField(getPlayer2().getId(), ":" + BotLoader.getLabel(first) + ":", true);
										if (String.valueOf(getOpponent(getPlayer2()).getPoints()).length() == 2) {
											int last1 = Integer.parseInt(String.valueOf(getOpponent(getPlayer2()).getPoints()).split("")[1]);
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(getOpponent(getPlayer2()).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
									if (total > 21) {
										msg.setTitle(name + " ***Lost***");
									} else {
										msg.setTitle(name + " **HIT**");
									}
									msg.setColor(getPlayer2().color());
									msg.setColor(0xf54245);
									msg.setDescription("**HIT** card " + card);
									getChannel().sendMessage(msg.build()).queue();
								}
								getPlayer2().setTurn(false);
								getPlayer1().setTurn(true);
							}, "blackjack-" + id.toString() + "-waiting", TimeUnit.SECONDS.toMillis(3));
						}  //getChannel().sendTyping().queue();


					}
				}
			}, "blackjack-" + id.toString(), 5, 5);
		}
	}

	public void startMulti() {
		if (!isRunning()) {
			started = System.currentTimeMillis();
			LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).repeat(task -> {
				TimeWatch.Recording r = TimeWatch.Recording.subtract(this.started);
				if (r.getMinutes() == 1) {
					if (getPlayer1().getPoints() == 0 || getPlayer2().getPoints() == 0) {
						getChannel().sendMessage("No winner was decided. Game inactivity detected, shutting down.").queue();
						getPlayer1().setTurn(false);
						getPlayer2().setTurn(false);
						task.cancel();
						LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).purge();
						BotLoader.endGame(BlackJack.this);
						return;
					}
				}
				if (BlackJack.this.isComplete()) {
					//TODO announce game over give out money
					getPlayer1().setTurn(false);
					getPlayer2().setTurn(false);
					task.cancel();
					LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).purge();
					BotLoader.endGame(BlackJack.this);
				}
			}, "blackjack-" + id.toString(), 5, 5);
		}
	}

	public Cardholder getOpponent(Cardholder holder) {
		if (!getPlayer2().equals(holder)) {
			return getPlayer2();
		}
		if (!getPlayer1().equals(holder)) {
			return getPlayer1();
		}
		return null;
	}

	public Cardholder getUser(User user) {
		if (getPlayer1().getId().equals(user.getId())) {
			return getPlayer1();
		}
		if (getPlayer2().getId().equals(user.getId())) {
			return getPlayer2();
		}
		return null;
	}

	public void setPlayer1(Cardholder player1) {
		player1.setColor(0x3cde85);
		this.player1 = player1;
	}

	public void setPlayer2(Cardholder player2) {
		player2.setColor(0x3c5fde);
		this.player2 = player2;
	}

	public Cardholder getPlayer1() {
		return player1;
	}

	public Cardholder getPlayer2() {
		return player2;
	}
}
