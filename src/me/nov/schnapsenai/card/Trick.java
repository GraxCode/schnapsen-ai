package me.nov.schnapsenai.card;

public class Trick {
	private Card card1;
	private boolean botPlayedFirst;
	private Card card2;

	public Trick(Card card1, Card card2) {
		super();
		this.card1 = card1;
		this.card2 = card2;
	}

	public Trick(Card card1, boolean belongingDeck1, Card card2) {
		super();
		this.card1 = card1;
		this.botPlayedFirst = belongingDeck1;
		this.card2 = card2;
	}

	public Trick() {
	}

	public Card getFirstCard() {
		return card1;
	}

	public void setFirstCard(Card card1) {
		this.card1 = card1;
	}

	public Card getSecondCard() {
		assert (!card2.equals(card1));
		return card2;
	}

	public void setSecondCard(Card card2) {
		this.card2 = card2;
	}

	public void setFirstCard(Card card1, boolean deck) {
		this.card1 = card1;
		this.botPlayedFirst = deck;
	}

	public boolean card1WinsTrick(CardColor trumpColor) {
		return !card1.isStungBy(card2, trumpColor);
	}

	public void addCard(Card c) {
		if (card1 == null) {
			card1 = c;
		} else if (card2 == null) {
			card2 = c;
		} else {
			throw new RuntimeException();
		}
	}

	public void addCard(Card c, boolean botMove) {
		if (card1 == null) {
			card1 = c;
			botPlayedFirst = botMove;
		} else if (card2 == null) {
			card2 = c;
			assert (botPlayedFirst != botMove);
		} else {
			throw new RuntimeException();
		}
	}

	public boolean botPlayedFirstCard() {
		return botPlayedFirst;
	}

	public int points() {
		if (card1 == null || card2 == null)
			return 0;
		return card1.getValue().getValue() + card2.getValue().getValue();
	}

	private boolean complete() {
		return card1 != null && card2 != null;
	}

	public boolean isEmpty() {
		return (card1 == null && card2 == null) || complete();
	}

	public boolean oneCardPlayed() {
		return card1 != null && card2 == null;
	}

	public Trick clone() {
		return new Trick(card1, botPlayedFirst, card2);
	}

	@Override
	public String toString() {
		return "Trick [card1=" + card1 + ", belongingDeck1=" + botPlayedFirst + ", card2=" + card2 + ", points()="
				+ points() + "]";
	}

	public int hash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (botPlayedFirst ? 1231 : 1237);
		result = prime * result + ((card1 == null) ? 0 : card1.hash());
		result = prime * result + ((card2 == null) ? 0 : card2.hash());
		return result;
	}
}
