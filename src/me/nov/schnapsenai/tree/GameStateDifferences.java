package me.nov.schnapsenai.tree;

import me.nov.schnapsenai.card.Card;

public class GameStateDifferences {
	private GameState originalState;
	private GameState newState;

	public GameStateDifferences(GameState originalState, GameState newState) {
		super();
		this.originalState = originalState;
		this.newState = newState;
	}

	public GameState getOriginalState() {
		return originalState;
	}

	public void setOriginalState(GameState originalState) {
		this.originalState = originalState;
	}

	public GameState getNewState() {
		return newState;
	}

	public void setNewState(GameState newState) {
		this.newState = newState;
	}

	public Card cardDifference() {
		boolean secondMove = originalState.getCurrentTrick().oneCardPlayed();
		if (!secondMove) {
			// bot plays card
			return newState.currentTrick.getFirstCard();
		} else {
			return newState.currentTrick.getSecondCard();
		}
	}

	public boolean closedDeck() {
		return !originalState.deckClosed && newState.deckClosed && newState.remainingCards >= 2;
	}

	public boolean switchedTrump() {
		return !originalState.trumpCard.equals(newState.trumpCard);
	}

	public boolean isMarriage() {
		return originalState.extraPoints < newState.extraPoints
				|| originalState.extraPointsOpponent < newState.extraPointsOpponent;
	}

	public boolean is40Marriage() {
		return originalState.extraPoints + 40 == newState.extraPoints
				|| originalState.extraPointsOpponent + 40 == newState.extraPointsOpponent;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (cardDifference().hash());
		result = 31 * result + (closedDeck() ? 1231 : 1237);
		result = 31 * result + (switchedTrump() ? 1231 : 1237);
		result = 31 * result + (isMarriage() ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GameStateDifferences))
			return false;
		GameStateDifferences gsd = (GameStateDifferences) obj;
		return this.closedDeck() == gsd.closedDeck() && this.switchedTrump() == gsd.switchedTrump()
				&& gsd.cardDifference().equals(this.cardDifference()) && this.isMarriage() == gsd.isMarriage();
	}

	@Override
	public String toString() {
		return "GameStateDifferences [cardDifference()=" + cardDifference() + ", closedDeck()=" + closedDeck()
				+ ", switchedTrump()=" + switchedTrump() + ", isMarriage()=" + isMarriage() + "]";
	}

}
