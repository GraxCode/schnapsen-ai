package me.nov.schnapsenai.bots.impl;

import me.nov.schnapsenai.bots.BasicBot;
import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardValue;
import me.nov.schnapsenai.card.Deck;
import me.nov.schnapsenai.card.GiveUpCard;
import me.nov.schnapsenai.card.Trick;
import me.nov.schnapsenai.mcts.ImperfectExpectiMiniMax;
import me.nov.schnapsenai.tree.GameState;
import me.nov.schnapsenai.tree.GameStateDifferences;
import me.nov.schnapsenai.tree.GameStateMaths;
import me.nov.schnapsenai.tree.WinState;

public class TreeSearchBot extends BasicBot {

	public TreeSearchBot(Card trumpCard) {
		super(trumpCard);
	}

	private GameStateDifferences lastDif;

	@Override
	public Card botPlaysCard() {
		System.gc();
		Card botCard = null;
		try {
			GameState gs = new GameState(trumpCard, hand, knownCardsOpponent, tricks, tricksOpponent, null, extraPoints,
					extraPointsOpponent, remainingCards, deckClosed, botClosedDeck, closedDeckScore, new Trick(), true);
			gs.getPossibleCardsOpponent().removeAll(impossibleCardsDeckClosed());
			if (gs.getWinState() != WinState.DRAW) {
				return null;
			}
			GameStateMaths gsm = new GameStateMaths(gs);
			lastDif = gsm.calculateBestMove(new ImperfectExpectiMiniMax(),
					deckClosed && !botClosedDeck && remainingCards > 0);
			botCard = lastDif.cardDifference();
		} catch (Exception e) {
			e.printStackTrace();
			botCard = this.hand.iterator().next();
		}
		if (botCard == null) {
			throw new RuntimeException(remainingCards + " " + deckClosed);
		}
		this.botCard(botCard);
		this.hand.remove(botCard);
		return botCard;
	}

	private boolean canGetTrick(Card onTable, GameState gs) {
		Deck cards = gs.unknownCards();
		cards.add(onTable);
		for (Card c : cards) {
			for (Card hc : gs.getHand())
				if (c.isStungBy(hc, gs.getTrumpColor())) {
					return true;
				}
		}
		return false;
	}

	@Override
	public Card opponentPlaysCard(Card cardOpponent, boolean marriage) {
		if (this.opponentCard(cardOpponent, marriage)) {
			System.gc();
			Card botCard = null;
			try {
				GameState gs = new GameState(trumpCard, hand, knownCardsOpponent, tricks, tricksOpponent, null, extraPoints,
						extraPointsOpponent, remainingCards, deckClosed, botClosedDeck, closedDeckScore,
						new Trick(cardOpponent, false, null), true);
				System.out.println(gs.getPossibleCardsOpponent());
				gs.getPossibleCardsOpponent().removeAll(impossibleCardsDeckClosed());
				if (gs.getWinState() != WinState.DRAW) {
					return null;
				}
				if (remainingCards == 0 && !canGetTrick(cardOpponent, gs)) {
					return new GiveUpCard();
				}
				GameStateMaths gsm = new GameStateMaths(gs);
				lastDif = gsm.calculateBestMove(new ImperfectExpectiMiniMax(),
						deckClosed && !botClosedDeck && remainingCards > 0);
				botCard = lastDif.cardDifference();
			} catch (Exception e) {
				e.printStackTrace();
				botCard = this.hand.iterator().next();
			}
			if (botCard == null) {
				throw new RuntimeException(remainingCards + " " + deckClosed);
			}
			this.botCard(botCard);
			this.hand.remove(botCard);
			return botCard;
		}
		return null;
	}

	@Override
	public boolean closesDeck() {
		if (deckClosed || remainingCards <= 0) {
			return false;
		}
		boolean closeDeck = lastDif.closedDeck();
		if (closeDeck) {
			this.deckClosed = true;
			this.botClosedDeck = true;
			this.closedDeckScore = pointsOpponent();
		}
		return closeDeck;
	}

	@Override
	public boolean switchesTrump() {
		if (remainingCards > 0 && !deckClosed) {
			if (lastDif == null)
				return false;
			boolean switchesTrump = lastDif.switchedTrump();
			if (switchesTrump) {
				for (Card c : new Deck(hand)) {
					if (c.isTrump(trumpColor) && c.getValue() == CardValue.UNTER) {
						hand.remove(c);
						hand.add(trumpCard);
						this.trumpCard = new Card(trumpColor, CardValue.UNTER);
						return true;
					}
				}
			}
		}
		return false;
	}

	public GameState getGS() {
		return new GameState(trumpCard, hand, new Deck(), tricks, tricksOpponent, null, extraPoints, extraPointsOpponent,
				remainingCards, deckClosed, botClosedDeck, closedDeckScore, new Trick(), true);
	}

	public GameState getGSClone() {
		return new GameState(trumpCard, (Deck) hand.clone(), new Deck(), (Deck) tricks.clone(),
				(Deck) tricksOpponent.clone(), null, extraPoints, extraPointsOpponent, remainingCards, deckClosed,
				botClosedDeck, closedDeckScore, new Trick(), true);
	}
}
