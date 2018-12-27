package me.nov.schnapsenai.card;

public class Card {
	private CardColor color;
	private CardValue value;

	public Card(CardColor color, CardValue value) {
		super();
		this.color = color;
		this.value = value;
	}

	// public Card(String color, String value) {
	// super();
	// this.color = CardColor.valueOf(color);
	// this.value = CardValue.valueOf(value);
	// }

	public Card(String string) {
		string = string.toLowerCase();
		char color = string.charAt(0);
		char value = string.charAt(1);
		for (CardColor c : CardColor.values()) {
			if (c.name().toLowerCase().charAt(0) == color) {
				for (CardValue v : CardValue.values()) {
					if (v.name().toLowerCase().charAt(0) == value) {
						this.color = c;
						this.value = v;
						return;
					}
				}
			}
		}
		throw new RuntimeException("Unknown card: " + string);
	}

	public boolean isTrump(CardColor trumpColor) {
		return color == trumpColor;
	}

	@Override
	public String toString() {
		return color.name() + " " + value.getValue();
	}

	public CardColor getColor() {
		return color;
	}

	public void setColor(CardColor color) {
		this.color = color;
	}

	public CardValue getValue() {
		return value;
	}

	public void setValue(CardValue value) {
		this.value = value;
	}

	public boolean isStungBy(Card c, CardColor trumpColor) {
		if (trumpColor != null) {
			if (this.color == trumpColor) {
				return c.color == trumpColor && this.value.getValue() < c.value.getValue();
			} else if (c.color == trumpColor) {
				// trump tricks normal card
				return true;
			}
		}
		return this.color == c.color && this.value.getValue() < c.value.getValue();
	}

	@Override
	protected Object clone()  {
		return new Card(color, value);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card) {
			Card c = (Card) obj;
			return value == c.value && color == c.color;
		}
		return false;
	}

	public int hash() {
		int result = 1;
		result = 31 * result + getColorPrime();
		result = 31 * result + getValuePrime();
		return result;
	}

	public int getValuePrime() {
		switch (value) {
		case KOENIG:
			return 1249;
		case OBER:
			return 1259;
		case SAU:
			return 1277;
		case UNTER:
			return 1279;
		case ZEHNER:
			return 1283;
		default:
			return -1;
		}
	}

	public int getColorPrime() {
		switch (color) {
		case EICHEL:
			return 1289;
		case HERZ:
			return 1291;
		case LAUB:
			return 1297;
		case SCHELL:
			return 1301;
		default:
			return -1;
		}
	}
}
