package me.nov.schnapsenai.utils;

import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardColor;
import me.nov.schnapsenai.card.CardValue;
import me.nov.schnapsenai.card.Deck;

public class CardUtils {
	public static Deck playableCardsClosedDeck(CardColor trumpColor, Deck hand, Card playedCard) {
		Deck playable = new Deck();
		for (Card c : hand) {
			if (c.getColor() == playedCard.getColor()) {
				if (c.getValue().getValue() > playedCard.getValue().getValue()) {
					playable.add(c);
				}
			}
		}
		if (playable.size() > 0) {
			return playable;
		}
		for (Card c : hand) {
			if (c.getColor() == playedCard.getColor()) {
				if (c.getValue().getValue() < playedCard.getValue().getValue()) {
					playable.add(c);
				}
			}
		}
		if (playable.size() > 0) {
			return playable;
		}
		for (Card c : hand) {
			if (c.getColor() == trumpColor) {
				playable.add(c);
			}
		}
		if (playable.size() > 0) {
			return playable;
		}
		playable.addAll(hand);
		return playable;
	}

	public static Deck playableCardsClosedDeckOpponent(CardColor trumpColor, Deck known,
			Deck possibleCards, Card playedCard, int handSize) {
//		assert (possibleCards.containsAll(known));
		int size = possibleCards.size();
		assert (size >= handSize);
		if (size == handSize) {
			return playableCardsClosedDeck(trumpColor, possibleCards, playedCard);
		}
		Deck playable = new Deck();
		boolean colorTrick = false;
		boolean colorGive = false;
		boolean trumpTrick = false;

		int playedCardValue = playedCard.getValue().getValue();
		CardColor playedCardColor = playedCard.getColor();
		// known cards define playwise
		for (Card c : known) {
			if (c.getColor() == playedCardColor) {
				if (c.getValue().getValue() > playedCardValue) {
					colorTrick = true;
					break;
				} else {
					colorGive = true;
				}
			}
			if (c.getColor() == trumpColor) {
				trumpTrick = true;
			}
		}
		Deck colorTricks = new Deck();
		Deck colorGives = new Deck();
		Deck trumpTricks = new Deck();
		Deck otherCards = new Deck();
		for (Card c : possibleCards) {
			if (c.getColor() == playedCardColor) {
				if (c.getValue().getValue() > playedCardValue) {
					colorTricks.add(c);
				} else {
					colorGives.add(c);
				}
			} else if (c.getColor() == trumpColor) {
				trumpTricks.add(c);
			} else {
				otherCards.add(c);
			}
		}
		if (!colorTrick) {
			int colorGivez = colorGives.size();
			int trumpTrickz = trumpTricks.size();
			int otherCardz = otherCards.size();
			if (otherCardz >= handSize && !trumpTrick && !colorGive) {
				// all
				playable.addAll(possibleCards);
				return playable;
			} else if (otherCardz + trumpTrickz >= handSize && !colorGive) {
				// color gives and color tricks and trump tricks
				playable.addAll(possibleCards);
				playable.removeAll(otherCards);
				return playable;
			} else if (otherCardz + trumpTrickz + colorGivez >= handSize) {
				// color gives and color tricks
				playable.addAll(possibleCards);
				playable.removeAll(otherCards);
				playable.removeAll(trumpTricks);
				return playable;
			}
		}
		//color tricks
		playable.addAll(colorTricks);
		return playable;
	}

	public static boolean hasMarriage(Deck hand, Card card) {
		if (card.getValue() != CardValue.OBER && card.getValue() != CardValue.KOENIG) {
			return false;
		}
		CardValue contraryValue = card.getValue() == CardValue.OBER ? CardValue.KOENIG : CardValue.OBER;
		Card contraryCard = new Card(card.getColor(), contraryValue);
		return hand.contains(card) && hand.contains(contraryCard);
	}

	public static boolean switchTrumpIfPossible(Card trumpCard, Deck playableHand) {
		for (Card c : new Deck(playableHand)) {
			if (c.isTrump(trumpCard.getColor()) && c.getValue() == CardValue.UNTER) {
				playableHand.remove(c);
				playableHand.add(trumpCard);
				return true;
			}
		}
		return false;
	}

	public static Deck allCardsFromColor(CardColor c) {
		Deck d = new Deck();
		for (CardValue v : CardValue.values()) {
			d.add(new Card(c, v));
		}
		return d;
	}

	public static Deck allCardsHigher(Card card) {
		Deck d = new Deck();
		for (CardValue v : card.getValue().valuesAboveMe()) {
			d.add(new Card(card.getColor(), v));
		}
		return d;
	}
}
