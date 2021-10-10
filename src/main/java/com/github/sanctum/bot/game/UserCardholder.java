package com.github.sanctum.bot.game;

import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserCardholder implements Cardholder {

	private final User user;
	private boolean turn;
	private int color;
	private final List<Card> deck = new ArrayList<>();

	public UserCardholder(User user) {
		this.user = user;
	}

	@Override
	public boolean isUser() {
		return true;
	}

	@Override
	public String getId() {
		return user.getId();
	}

	@Override
	public void add(Card card) {
		deck.add(card);
	}

	@Override
	public List<Card> getDeck() {
		return deck;
	}

	@Override
	public int getPoints() {
		int points = 0;
		for(Card c : deck) {
			if (c == Card.ACE) {
				int test = points + c.getWorth();
				if (test > 21) {
					points += 1;
				} else {
					points += 11;
				}
			} else {
				points += c.getWorth();
			}
		}
		return points;
	}

	@Override
	public boolean isTurn() {
		return this.turn;
	}

	@Override
	public int color() {
		return this.color;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void setTurn(boolean turn) {
		this.turn = turn;
	}
}
