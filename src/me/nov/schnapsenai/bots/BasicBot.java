package me.nov.schnapsenai.bots;

import java.util.ArrayList;

import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardColor;
import me.nov.schnapsenai.card.CardValue;
import me.nov.schnapsenai.card.Deck;
import me.nov.schnapsenai.card.Trick;
import me.nov.schnapsenai.utils.CardUtils;

/**
 * Basic bot that counts points and remembers cards the opponent plays or shows
 */
public abstract class BasicBot extends Bot {

	public BasicBot(Card trumpCard) {
		super(trumpCard);
		this.tricksDeckClosed = new ArrayList<>();
	}

	protected Card cardOnTable;
	protected boolean tableCardBelongsToBot;
	protected ArrayList<Trick> tricksDeckClosed;

	@Override
	public void opponentSwitchesTrump() {
		this.knownCardsOpponent.add(trumpCard);
		this.trumpCard = new Card(trumpColor, CardValue.UNTER);
	}

	public void opponentClosesDeck() {
		this.deckClosed = true;
		this.botClosedDeck = false;
		this.closedDeckScore = points();
	}

	/**
	 * Returns true if opponent played first card
	 */
	protected boolean opponentCard(Card cardOpponent, boolean marriage) {
		if (remainingCards > 0 && !deckClosed)
			this.remainingCards--;
		CardColor color = cardOpponent.getColor();
		if (knownCardsOpponent.contains(cardOpponent)) {
			knownCardsOpponent.remove(cardOpponent);
		}
		if (cardOnTable == null) {
			// opponent plays first card
			this.cardOnTable = cardOpponent;
			this.tableCardBelongsToBot = false;
			if (marriage) {
				this.extraPointsOpponent += (color == trumpColor) ? 40 : 20;
				if (cardOpponent.getValue() == CardValue.OBER) {
					this.knownCardsOpponent.add(new Card(color, CardValue.KOENIG));
				} else if (cardOpponent.getValue() == CardValue.KOENIG) {
					this.knownCardsOpponent.add(new Card(color, CardValue.OBER));
				} else {
					throw new RuntimeException("Wrong Marriage");
				}
			}
			return true;
		} else {
			handleTrick(cardOpponent); // opponent plays second card
			return false;
		}
	}

	protected void botCard(Card botCard) {
		if (botCard == null) {
			//computer knows he can't make a trick anymore
			return; //throw new IllegalArgumentException("Computer can't play nothing");
		}
		if (remainingCards > 0 && !deckClosed)
			this.remainingCards--;
		if (cardOnTable == null) {
			if (hasMarriage(botCard)) {
				this.extraPoints += (botCard.getColor() == trumpColor) ? 40 : 20;
				System.out.println("Extra points for marriage!");
			}
			// bot plays first card
			this.cardOnTable = botCard;
			this.tableCardBelongsToBot = true;
		} else {
			handleTrick(botCard); // bot plays second card
		}
	}

	private void handleTrick(Card cardOnTop) {
		if (!cardOnTable.isStungBy(cardOnTop, trumpColor)) {
			if (tableCardBelongsToBot) {
				this.tricks.add(cardOnTable);
				this.tricks.add(cardOnTop);
				if (deckClosed && remainingCards > 0) {
					this.tricksDeckClosed.add(new Trick(cardOnTable, true, cardOnTop));
				}
			} else {
				this.tricksOpponent.add(cardOnTable);
				this.tricksOpponent.add(cardOnTop);
				if (deckClosed && remainingCards > 0) {
					this.tricksDeckClosed.add(new Trick(cardOnTable, false, cardOnTop));
				}
			}
		} else {
			if (!tableCardBelongsToBot) {
				this.tricks.add(cardOnTable);
				this.tricks.add(cardOnTop);
				if (deckClosed && remainingCards > 0) {
					this.tricksDeckClosed.add(new Trick(cardOnTable, false, cardOnTop));
				}
			} else {
				this.tricksOpponent.add(cardOnTable);
				this.tricksOpponent.add(cardOnTop);
				if (deckClosed && remainingCards > 0) {
					this.tricksDeckClosed.add(new Trick(cardOnTable, true, cardOnTop));
				}
			}
		}
		cardOnTable = null;
		if (remainingCards == 0) {
			deckClosed = true;
		}
	}

	public boolean hasMarriage(Card card) {
		if (card.getValue() != CardValue.OBER && card.getValue() != CardValue.KOENIG) {
			return false;
		}
		CardValue contraryValue = card.getValue() == CardValue.OBER ? CardValue.KOENIG : CardValue.OBER;
		Card contraryCard = new Card(card.getColor(), contraryValue);
		return hand.contains(card) && hand.contains(contraryCard);
	}

	protected Deck impossibleCardsDeckClosed() {
		Deck d = new Deck();
		for (Trick t : tricksDeckClosed) {
			if (t.botPlayedFirstCard() && t.card1WinsTrick(trumpColor)) {
				CardColor fcolor = t.getFirstCard().getColor();
				CardColor scolor = t.getSecondCard().getColor();
				CardValue fvalue = t.getFirstCard().getValue();
				CardValue svalue = t.getSecondCard().getValue();
				if (fcolor == scolor) {
					if (svalue.getValue() < fvalue.getValue()) {
						d.addAll(CardUtils.allCardsHigher(t.getFirstCard()));
					}
				} else {
					d.addAll(CardUtils.allCardsFromColor(fcolor));
					if (scolor != trumpColor) {
						d.addAll(CardUtils.allCardsFromColor(trumpColor));
					}
				}
			}
		}
		if(d.isEmpty()) {
//			return null; only if OPC TODO
		}
		return d;
	}

}
