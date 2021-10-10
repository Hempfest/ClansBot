package com.github.sanctum.bot.game;

import java.util.Random;

public enum Card {

	ACE(11),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8),
	NINE(9),
	JACK(10),
	KING(10),
	QUEEN(10);

	private final int worth;

	Card(int worth) {
		this.worth = worth;
	}

	public int getWorth() {
		return worth;
	}

	public static Card random() {
		Random r = new Random();
		int cap = r.nextInt(24);
		switch (cap) {
			case 0:
			case 18:
				return ACE;
			case 1:
			case 12:
				return THREE;
			case 2:
			case 14:
				return NINE;
			case 3:
			case 5:
				return SEVEN;
			case 17:
			case 7:
				return FOUR;
			case 4:
			case 21:
				return JACK;
			case 6:
			case 22:
				return EIGHT;
			case 19:
			case 20:
				return TWO;
			case 8:
			case 16:
				return SIX;
			case 9:
			case 23:
				return KING;
			case 10:
			case 15:
				return FIVE;
			case 11:
			case 13:
				return QUEEN;
		}
		return ACE;
	}

}
