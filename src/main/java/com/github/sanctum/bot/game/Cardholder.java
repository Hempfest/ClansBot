package com.github.sanctum.bot.game;

import java.util.List;

public interface Cardholder {

	boolean isUser();

	String getId();

	void add(Card card);

	List<Card> getDeck();

	int getPoints();

	boolean isTurn();

	int color();

	void setColor(int color);

	void setTurn(boolean turn);

}
