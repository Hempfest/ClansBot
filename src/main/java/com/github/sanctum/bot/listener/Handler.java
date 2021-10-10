package com.github.sanctum.bot.listener;

import com.github.sanctum.bot.Bot;
import com.github.sanctum.bot.BotLoader;
import com.github.sanctum.bot.api.UserLink;
import com.github.sanctum.bot.game.BlackJack;
import com.github.sanctum.bot.game.Card;
import com.github.sanctum.bot.game.Cardholder;
import com.github.sanctum.bot.game.SystemCardholder;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.string.DefaultColor;
import com.github.sanctum.labyrinth.formatting.string.RandomHex;
import com.github.sanctum.labyrinth.library.StringUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class Handler extends ListenerAdapter implements Listener, Message.Factory {

	@Subscribe
	public void onChat(DefaultEvent.Communication e) {
		if (e.getCommunicationType() == DefaultEvent.Communication.Type.CHAT) {
			DefaultEvent.Communication.ChatMessage message = e.getMessage().get();

			TextChannel channel = DiscordSRV.getPlugin().getJda().getTextChannelById(Bot.CHAT);

			if (channel != null) {
				Clan.Associate a = ClansAPI.getInstance().getAssociate(e.getPlayer()).orElse(null);
				if (a != null) {
					if (a.getChat().equalsIgnoreCase("global")) {
						channel.sendTyping().queue();
						channel.sendMessage("**" + e.getPlayer().getName() + "** : " + message.getText()).queue();
					}
				} else {
					channel.sendTyping().queue();
					channel.sendMessage("**" + e.getPlayer().getName() + "** : " + message.getText()).queue();
				}
			}
		}
		if (e.getCommunicationType() == DefaultEvent.Communication.Type.COMMAND) {
			DefaultEvent.Communication.ChatCommand cmd = e.getCommand().get();
			String label = cmd.getText();

			if (label.equals("cortex")) {

				if (UserLink.get(e.getPlayer()) == null) {
					UserLink.identify(e.getPlayer()).deploy();
					e.setCancelled(true);
				}

			}

		}
	}


	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent e) {
		if (e.getAuthor().isBot() || e.isWebhookMessage()) {
			return;
		}

		TextChannel channel = e.getChannel();

		if (channel.getId().equals(Bot.CHAT)) {

			UserLink link = UserLink.get(e.getAuthor());

			if (link == null) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("Error");
				builder.setThumbnail("https://cdn4.iconfinder.com/data/icons/refresh_cl/256/Symbols/Warning.png");
				builder.setDescription(e.getAuthor().getAsMention() + " **Only linked accounts can talk in game**");
				builder.addField("How to:", "Log in-game and simply run '/cortex'.", true);
				channel.sendMessage(builder.build()).queue();
				channel.deleteMessageById(e.getMessageId()).submit(true);
				return;
			}

			OfflinePlayer user = link.toPlayer();

			String message = e.getMessage().getContentRaw();
			message().append(text("[").color(Color.LIME))
					.append(text("Discord").color(Color.FUCHSIA))
					.append(text("]").color(Color.LIME))
					.append(text(" "))
					.append(text(user.getName()).style(DefaultColor.GALAXY))
					.append(text(" "))
					.append(text(":").style(ChatColor.RESET))
					.append(text(" "))
					.append(text(message))
					.send(player -> true)
					.deploy();
		}

		if (channel.getId().equals(Bot.VOTE)) {
			EmbedBuilder suggest = new EmbedBuilder();
			suggest.setDescription(e.getMessage().getContentRaw());
			suggest.setColor(new RandomHex().intValue());
			String url = Arrays.stream(e.getMessage().getContentRaw().split(" ")).filter(s -> StringUtils.use(s).containsIgnoreCase("http", "https")).findFirst().orElse(null);
			suggest.setThumbnail("https://i.imgur.com/vz4q9mF.png");
			if (url != null && !url.isEmpty()) {
				suggest.setImage(url);
			} else {
				if (StringUtils.use(e.getMessage().getContentRaw()).containsIgnoreCase("http", "https")) {
					suggest.setImage(e.getMessage().getContentRaw());
				}
			}
			suggest.setAuthor(e.getAuthor().getAsTag(), e.getAuthor().getAvatarUrl(), e.getAuthor().getEffectiveAvatarUrl());
			channel.sendTyping().queue();
			channel.sendMessage(suggest.build()).queue(message1 -> {
				message1.addReaction("U+1F44D").queue();
				message1.addReaction("U+270A").queue();
				message1.addReaction("U+1F44E").queue();
			});
			channel.deleteMessageById(e.getMessageIdLong()).submit(true);
		}

		if (channel.getId().equals(Bot.COMMANDS)) {

			String message = e.getMessage().getContentRaw();
			if (message.startsWith("/")) {

				if (message.replace("/", "").split(" ").length == 1) {

					if (message.replace("/", "").equalsIgnoreCase("blackjack")) {
						BlackJack game = BotLoader.getGame(e.getAuthor());

						if (game != null) {
							channel.sendMessage(e.getAuthor().getAsMention() + " You are already playing a game.").queue();
						} else {

							BlackJack newGame = BotLoader.newGame(e.getAuthor());
							newGame.getPlayer1().setTurn(true);
							newGame.setPlayer2(new SystemCardholder());
							newGame.startSolo();

							channel.sendMessage(e.getAuthor().getAsMention() + " The game has started, you start! Use /hit or /stand").queue();

						}

					}

					if (message.replace("/", "").equalsIgnoreCase("hit")) {
						BlackJack game = BotLoader.getGame(e.getAuthor());
						if (game != null) {
							if (!game.isRunning()) {
								channel.sendMessage(e.getAuthor().getAsMention() + " The game hasnt started yet!").queue();
								return;
							}
							Cardholder holder = game.getUser(e.getAuthor());
							if (holder.isTurn()) {
								String name = DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName();
								Card card = Card.random();
								holder.add(card);
								int total = holder.getPoints();
								EmbedBuilder msg = new EmbedBuilder();
								int first = Integer.parseInt(String.valueOf(total).split("")[0]);
								int first1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[0]);
								if (String.valueOf(total).length() == 2) {
									int last = Integer.parseInt(String.valueOf(total).split("")[1]);
									msg.addField(DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
									if (String.valueOf(game.getOpponent(holder).getPoints()).length() == 2) {
										int last1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[1]);
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										}
									} else {
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
								} else {
									msg.addField(DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName(), ":" + BotLoader.getLabel(first) + ":", true);
									if (String.valueOf(game.getOpponent(holder).getPoints()).length() == 2) {
										int last1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[1]);
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										}
									} else {
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
								}
								if (total > 21) {
									msg.setTitle(name + " ***Lost***");
									msg.setDescription("**HIT** card " + card);
									msg.setColor(0xf54245);
								} else {
									if (total == 21) {
										msg.setTitle("***BLACKJACK!***");
										msg.setDescription("**" + name + "** got a ***BLACKJACK***");
									} else {
										msg.setTitle(name + " **HIT**");
										msg.setDescription("**HIT** card " + card);
									}
									msg.setColor(holder.color());
								}
								msg.setAuthor(e.getAuthor().getAsTag(), e.getAuthor().getAvatarUrl(), e.getAuthor().getEffectiveAvatarUrl());
								channel.sendMessage(msg.build()).queue();
								holder.setTurn(false);
								game.getOpponent(holder).setTurn(true);
							} else {
								channel.sendMessage(e.getAuthor().getAsMention() + " You must wait your turn!").queue();
							}
						}
					}

					if (message.replace("/", "").equalsIgnoreCase("stand")) {
						BlackJack game = BotLoader.getGame(e.getAuthor());
						if (game != null) {
							if (!game.isRunning()) {
								channel.sendMessage(e.getAuthor().getAsMention() + " The game hasnt started yet!").queue();
								return;
							}
							Cardholder holder = game.getUser(e.getAuthor());
							if (holder.isTurn()) {
								String name = DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName();
								int total = holder.getPoints();
								EmbedBuilder msg = new EmbedBuilder();
								int first = Integer.parseInt(String.valueOf(total).split("")[0]);
								int first1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[0]);
								if (String.valueOf(total).length() == 2) {
									int last = Integer.parseInt(String.valueOf(total).split("")[1]);
									msg.addField(DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName(), ":" + BotLoader.getLabel(first) + ": :" + BotLoader.getLabel(last) + ":", true);
									if (String.valueOf(game.getOpponent(holder).getPoints()).length() == 2) {
										int last1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[1]);
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										}
									} else {
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
								} else {
									msg.addField(DiscordSRV.getPlugin().getJda().getUserById(holder.getId()).getName(), ":" + BotLoader.getLabel(first) + ":", true);
									if (String.valueOf(game.getOpponent(holder).getPoints()).length() == 2) {
										int last1 = Integer.parseInt(String.valueOf(game.getOpponent(holder).getPoints()).split("")[1]);
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ": :" + BotLoader.getLabel(last1) + ":", true);
										}
									} else {
										if (game.getOpponent(holder).isUser()) {
											msg.addField(DiscordSRV.getPlugin().getJda().getUserById(game.getOpponent(holder).getId()).getName(), ":" + BotLoader.getLabel(first1) + ":", true);
										} else {
											msg.addField(game.getOpponent(holder).getId(), ":" + BotLoader.getLabel(first1) + ":", true);
										}
									}
								}
								msg.setAuthor(e.getAuthor().getAsTag(), e.getAuthor().getAvatarUrl(), e.getAuthor().getEffectiveAvatarUrl());
								msg.setTitle(name + " **STAND**");
								msg.setColor(holder.color());
								msg.setDescription("**STAND**");
								channel.sendMessage(msg.build()).queue();
								holder.setTurn(false);
								game.getOpponent(holder).setTurn(true);
							} else {
								channel.sendMessage(e.getAuthor().getAsMention() + " You must wait your turn!").queue();
							}
						}
					}
				}

				if (message.replace("/", "").split(" ").length == 2) {
					if (message.replace("/", "").split(" ")[0].equalsIgnoreCase("blackjack")) {
						BlackJack game = BotLoader.getGame(e.getAuthor());

						if (game != null) {
							channel.sendMessage(e.getAuthor().getAsMention() + " You are already playing a game.").queue();
						} else {
							String user = message.replace("/", "").replace("<", "").replace("@", "").replace(">", "").replace("!", "").split(" ")[1];
							User us = channel.getJDA().getUserById(user);
							if (us != null) {

								if (BotLoader.getGame(us) != null) {
									channel.sendMessage(e.getAuthor().getAsMention() + " " + user + " is already in a game!").queue();
									return;
								}

								BlackJack newGame = BotLoader.newGame(e.getAuthor(), us);
								newGame.getPlayer1().setTurn(true);
								newGame.startMulti();
								channel.sendMessage(e.getAuthor().getAsMention() + " The game has started, " + e.getAuthor().getName() + " starts! Use /hit or /stand").queue();
							} else {
								channel.sendMessage(e.getAuthor().getAsMention() + " No user was found by the name: " + user).queue();
							}

						}

					}
				}

			} else {
				OfflinePlayer test = UserLink.identify(e.getMessage().getContentRaw());
				if (test != null) {
					channel.deleteMessageById(e.getMessageIdLong()).submit(true);
					UserLink.add(test, e.getAuthor()).deploy();

					EmbedBuilder builder = new EmbedBuilder();
					builder.setDescription("***Congrats! Your discord and minecraft is now linked.***");

					channel.sendMessage(builder.build()).queue();

				}
			}

		}

	}
}
