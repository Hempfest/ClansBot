package com.github.sanctum.bot.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SystemCardholder implements Cardholder {

	private final List<Card> deck = new ArrayList<>();
	private boolean turn;
	private int color;

	@Override
	public boolean isUser() {
		return false;
	}

	@Override
	public String getId() {
		return "SYSTEM";
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
		return color;
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
