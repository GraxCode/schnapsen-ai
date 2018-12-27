package me.nov.schnapsenai.bots;

import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardColor;
import me.nov.schnapsenai.card.Deck;

public abstract class Bot {
	protected CardColor trumpColor;
	protected Card trumpCard;

	protected Deck hand;
	protected Deck knownCardsOpponent;
	
	protected Deck tricks;
	protected Deck tricksOpponent;

	protected int extraPoints;
	protected int extraPointsOpponent;

	protected int remainingCards;

	protected boolean deckClosed;
	protected boolean botClosedDeck;
	protected int closedDeckScore;

	public Bot(Card trumpCard) {
		super();
		this.trumpCard = trumpCard;
		this.trumpColor = trumpCard.getColor();
		this.remainingCards = 10;
		this.deckClosed = false;
		this.botClosedDeck = false;
		this.knownCardsOpponent = new Deck();
		this.tricks = new Deck();
		this.tricksOpponent = new Deck();
	}

	public abstract Card opponentPlaysCard(Card cardOpponent, boolean marriage);

	public abstract Card botPlaysCard();

	public abstract boolean closesDeck();

	public abstract boolean switchesTrump();

	public abstract void opponentSwitchesTrump();

	public abstract void opponentClosesDeck();

	public Deck getHand() {
		return hand;
	}

	public void setHand(Deck hand) {
		this.hand = hand;
	}

	public Card getTrumpCard() {
		return trumpCard;
	}

	public void setTrumpCard(Card trumpCard) {
		this.trumpCard = trumpCard;
		this.trumpColor = trumpCard.getColor();
	}

	public Deck getKnownCardsOpponent() {
		return knownCardsOpponent;
	}

	public void setKnownCardsOpponent(Deck knownCardsOpponent) {
		this.knownCardsOpponent = knownCardsOpponent;
	}

	public int getExtraPoints() {
		return extraPoints;
	}

	public void setExtraPoints(int extraPoints) {
		this.extraPoints = extraPoints;
	}

	public int getExtraPointsOpponent() {
		return extraPointsOpponent;
	}

	public void setExtraPointsOpponent(int extraPointsOpponent) {
		this.extraPointsOpponent = extraPointsOpponent;
	}

	public int getRemainingCards() {
		return remainingCards;
	}

	public void setRemainingCards(int remainingCards) {
		this.remainingCards = remainingCards;
	}

	public Deck getTricks() {
		return tricks;
	}

	public Deck getTricksOpponent() {
		return tricksOpponent;
	}

	public boolean isDeckClosed() {
		return deckClosed;
	}

	public void setDeckClosed(boolean deckClosed) {
		this.deckClosed = deckClosed;
	}

	public int points() {
		return tricks.size() > 0 ? tricks.points() + extraPoints : 0;
	}

	public int pointsOpponent() {
		return tricksOpponent.size() > 0 ? tricksOpponent.points() + extraPointsOpponent : 0;
	}
}
