package me.nov.schnapsenai.tree;

import java.util.ArrayList;

import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardColor;
import me.nov.schnapsenai.card.CardValue;
import me.nov.schnapsenai.card.Deck;
import me.nov.schnapsenai.card.Trick;
import me.nov.schnapsenai.utils.CardUtils;

public class GameState {

	private static final Deck cardDeck;

	static {
		cardDeck = new Deck();
		for (CardColor c : CardColor.values()) {
			for (CardValue v : CardValue.values()) {
				cardDeck.add(new Card(c, v));
			}
		}
	}
	protected CardColor trumpColor;
	protected Card trumpCard;

	protected Deck hand;
	protected Deck knownCardsOpponent;
	protected Deck possibleCardsOpponent;

	protected Deck tricks;
	protected Deck tricksOpponent;

	protected int extraPoints;
	protected int extraPointsOpponent;

	protected int remainingCards;

	protected boolean deckClosed;
	protected boolean botClosedDeck;
	protected int closedDeckScore;
	protected Trick currentTrick;

	protected boolean botTurn;

	public GameState(Card trumpCard, Deck hand, Deck knownCardsOpponent, Deck tricks, Deck tricksOpponent,
			Deck possibleCardsOpponent, int extraPoints, int extraPointsOpponent, int remainingCards, boolean deckClosed,
			boolean botClosedDeck, int closedDeckScore, Trick currentTrick, boolean botTurn) {
		super();
		this.trumpCard = trumpCard;
		this.trumpColor = trumpCard.getColor();
		this.hand = hand;
		this.knownCardsOpponent = knownCardsOpponent;
		this.tricks = tricks;
		this.tricksOpponent = tricksOpponent;
		this.extraPoints = extraPoints;
		this.remainingCards = Math.max(remainingCards, 0);
		this.deckClosed = deckClosed || remainingCards == 0;
		this.botClosedDeck = botClosedDeck;
		this.currentTrick = currentTrick;
		this.botTurn = botTurn;
		this.possibleCardsOpponent = possibleCardsOpponent == null ? unknownCards() : possibleCardsOpponent;
	}

	public GameState clone() {
		GameState gs = new GameState(trumpCard, (Deck) hand.clone(), (Deck) knownCardsOpponent.clone(),
				(Deck) tricks.clone(), (Deck) tricksOpponent.clone(), (Deck) possibleCardsOpponent.clone(), extraPoints,
				extraPointsOpponent, remainingCards, deckClosed, botClosedDeck, closedDeckScore, currentTrick.clone(), botTurn);
		return gs;
	}

	public int hash() {
		int result = 1;
		result = 31 * result + (botClosedDeck ? 1231 : 1237);
		result = 31 * result + (botTurn ? 1231 : 1237);
		result = 31 * result + closedDeckScore;
		result = 31 * result + ((currentTrick == null) ? 0 : currentTrick.hash());
		result = 31 * result + (deckClosed ? 1231 : 1237);
		result = 31 * result + extraPoints;
		result = 31 * result + extraPointsOpponent;
		result = 31 * result + ((hand == null) ? 0 : hand.hash());
		result = 31 * result + ((knownCardsOpponent == null) ? 0 : knownCardsOpponent.hash());
		result = 31 * result + remainingCards;
		result = 31 * result + ((tricks == null) ? 0 : tricks.hash());
		result = 31 * result + ((tricksOpponent == null) ? 0 : tricksOpponent.hash());
		result = 31 * result + ((trumpCard == null) ? 0 : trumpCard.hash());
		return result;
	}

	@Override
	public String toString() {
		return "GameState [trumpColor=" + trumpColor + ", trumpCard=" + trumpCard + ", hand=" + hand
				+ ", knownCardsOpponent=" + knownCardsOpponent + ", allPossibleCardsOpponent=" + possibleCardsOpponent
				+ ", tricks=" + tricks + ", tricksOpponent=" + tricksOpponent + ", extraPoints=" + extraPoints
				+ ", extraPointsOpponent=" + extraPointsOpponent + ", remainingCards=" + remainingCards + ", deckClosed="
				+ deckClosed + ", botClosedDeck=" + botClosedDeck + ", closedDeckScore=" + closedDeckScore + ", currentTrick="
				+ currentTrick + ", botTurn=" + botTurn + ", points=" + points() + ", pointsOpponent=" + pointsOpponent() + "]";
	}

	public ArrayList<GameState> cachedSubs = null;
	public static boolean CACHING = true;

	public ArrayList<GameState> getSubordinates() {
		if (CACHING && cachedSubs != null) {
			return cachedSubs;
		}
		ArrayList<GameState> subNodes = new ArrayList<>();
		if (getWinState() != WinState.DRAW || noCardLeft()) {
			// leaf, game result
			return subNodes;
		}
		if (currentTrick.isEmpty()) {
			// all possible card plays
			Deck playableHand = botTurn ? new Deck(hand) : new Deck(possibleCardsOpponent);
			boolean switchPossible = canSwitch(playableHand);
			assert (!(playableHand.contains(trumpCard) && remainingCards > 0));
			if (switchPossible) {
				playableHand.add(trumpCard);
			}
			boolean closePossible = deckClosePossible();
			for (Card card : playableHand) {
				assert (!tricks.contains(card) && !tricksOpponent.contains(card));
				boolean cardSwitchable = switchPossible && !card.equals(new Card(trumpColor, CardValue.UNTER));
				boolean marriagePossible = CardUtils.hasMarriage(playableHand, card);
				addNewStatesPlayout(subNodes, card, cardSwitchable, closePossible, false);
				if (marriagePossible) {
					addNewStatesPlayout(subNodes, card, cardSwitchable, closePossible, true);
				}
			}
		} else {
			assert (currentTrick.oneCardPlayed());
			Deck playableCards;
			if (remainingCards <= 0) {
				playableCards = CardUtils.playableCardsClosedDeck(trumpColor, botTurn ? hand : possibleCardsOpponent,
						currentTrick.getFirstCard());
			} else if (deckClosed) {
				if (botTurn) {
					playableCards = CardUtils.playableCardsClosedDeck(trumpColor, hand, currentTrick.getFirstCard());
				} else {
					playableCards = CardUtils.playableCardsClosedDeckOpponent(trumpColor, knownCardsOpponent,
							possibleCardsOpponent, currentTrick.getFirstCard(), hand.size() + 1);
				}
			} else {
				playableCards = botTurn ? hand : possibleCardsOpponent;
			}
			if (playableCards.isEmpty()) {
				throw new RuntimeException("No playable card found: " + this + "  " + (tricks.size() + tricksOpponent.size())
						+ " " + (20 - remainingCards) + " " + unknownCards() + " " + possibleCardsOpponent);
			}
			for (Card playableCard : playableCards) {
				Trick t = currentTrick.clone();
				t.addCard(playableCard, botTurn);
				GameState gs = createNewStateWithTrick(t);
				if (!deckClosed) {
					endMoveDrawCards(subNodes, gs);
				} else {
					subNodes.add(gs);
				}
			}
		}
		if (CACHING)
			this.cachedSubs = subNodes;
		return subNodes;
	}

	public Deck unknownCards() {
		Deck cards = (Deck) cardDeck.clone();
		cards.removeAll(tricksOpponent);
		cards.removeAll(tricks);
		cards.removeAll(hand);

		if (!currentTrick.isEmpty())
			cards.remove(currentTrick.getFirstCard());

		if (remainingCards > 0)
			cards.remove(trumpCard);
		return cards;
	}

	private void endMoveDrawCards(ArrayList<GameState> subNodes, GameState trickHandled) {
		Deck remainingStack = (Deck) trickHandled.possibleCardsOpponent.clone();
		if (this.remainingCards > 0) {
			remainingStack.remove(trickHandled.trumpCard);
		}
		boolean lastCard = remainingCards <= 2;
		if (lastCard && trickHandled.botTurn) {
			trickHandled.hand.add(trumpCard);
			trickHandled.possibleCardsOpponent.remove(trumpCard); // should not contains; actually unnecessary
			assert (!trickHandled.tricks.contains(trumpCard) && !trickHandled.tricksOpponent.contains(trumpCard));
			subNodes.add(trickHandled);
		} else {
			if (lastCard) {
				trickHandled.possibleCardsOpponent.add(trumpCard);
			}
			assert (!remainingStack.contains(trumpCard));
			// we can't draw cards that opponent has in his hand
			remainingStack.removeAll(knownCardsOpponent);
			// add all possible card drawings
			for (Card drawnCard : remainingStack) {
				GameState cardDrawing = trickHandled.clone();
				cardDrawing.hand.add(drawnCard);
				cardDrawing.possibleCardsOpponent.remove(drawnCard);
				subNodes.add(cardDrawing);
			}
		}
	}

	private void addNewStatesPlayout(ArrayList<GameState> subNodes, Card card, boolean switchable, boolean closePossible,
			boolean marriage) {
		Trick t = new Trick();
		t.addCard(card, botTurn);
		GameState gs = new GameState(trumpCard, (Deck) hand.clone(), (Deck) knownCardsOpponent.clone(),
				(Deck) tricks.clone(), (Deck) tricksOpponent.clone(), (Deck) possibleCardsOpponent.clone(), extraPoints,
				extraPointsOpponent, remainingCards, deckClosed, botClosedDeck, closedDeckScore, t, !botTurn);
		if (botTurn) {
			gs.hand.remove(card);
			if (marriage) {
				gs.extraPoints += card.getColor() == trumpColor ? 40 : 20;
			}
		} else {
			gs.possibleCardsOpponent.remove(card);
			gs.knownCardsOpponent.remove(card);
			if (marriage) {
				gs.extraPointsOpponent += card.getColor() == trumpColor ? 40 : 20;
				Card otherMarriageCard = new Card(card.getColor(),
						card.getValue() == CardValue.KOENIG ? CardValue.OBER : CardValue.KOENIG);
				Card switchCard = new Card(trumpColor, CardValue.UNTER);
				if (otherMarriageCard.equals(gs.trumpCard) && switchable) { //!gs.possibleCardsOpponent.contains(otherMarriageCard) && gs.possibleCardsOpponent.contains(switchCard)
					
					// this case happens when opponent plays out 40, but has UNTER instead of OBER or KOENIG
					assert(!(gs.possibleCardsOpponent.contains(otherMarriageCard) && remainingCards > 0));
					
					gs.trumpCard = switchCard;
					gs.possibleCardsOpponent.remove(switchCard);
					gs.possibleCardsOpponent.add(otherMarriageCard);
					
					switchable = false;
				}
				gs.knownCardsOpponent.add(otherMarriageCard);
			}
		}
		if (switchable) {
			if (card.equals(trumpCard)) { // if card is old trump card
				// switch and playout in one move
				if (botTurn) {
					if (!gs.hand.remove(new Card(trumpColor, CardValue.UNTER))) {
						throw new RuntimeException("playout: " + card + ", can't switch without unter " + this);
					}
				} else {
					if (!gs.possibleCardsOpponent.remove(new Card(trumpColor, CardValue.UNTER))) {
						throw new RuntimeException("playout: " + card + ", can't switch without unter " + this);
					}
				}
				gs.trumpCard = new Card(trumpColor, CardValue.UNTER);
			} else {
				subNodes.add(switchedTrumpClone(gs, botTurn));
			}
		}
		subNodes.add(gs);
		if (closePossible) {
			// case if deck is closed
			GameState clone = gs.clone();
			clone.deckClosed = true;
			clone.botClosedDeck = botTurn;
			clone.closedDeckScore = botTurn ? (pointsOpponent()) : (points());
			subNodes.add(clone);
			if (switchable && !card.equals(trumpCard)) {
				subNodes.add(switchedTrumpClone(clone, botTurn));
			}
		}
	}

	public boolean deckClosePossible() {
		return !deckClosed && remainingCards > 0;
	}

	private boolean noCardLeft() {
		return tricks.size() + tricksOpponent.size() >= 19 - remainingCards;
	}

	private static GameState switchedTrumpClone(GameState gs, boolean botSwitches) {
		GameState clone = gs.clone();
		Card newCard = new Card(clone.trumpColor, CardValue.UNTER);
		if (botSwitches) {
			if (!CardUtils.switchTrumpIfPossible(clone.trumpCard, clone.hand)) {
				throw new RuntimeException("card switch failed " + gs.botTurn + " " + clone.hand + " " + gs.trumpCard);
			}
		} else {
			clone.possibleCardsOpponent.add(clone.trumpCard);
			clone.knownCardsOpponent.add(clone.trumpCard);
			clone.possibleCardsOpponent.remove(newCard);
			// switch handled automatically
		}
		clone.trumpCard = newCard;
		return clone;
	}

	public boolean canSwitch(Deck playableHand) {
		return trumpSwitchable() && playableHand.contains(new Card(trumpColor, CardValue.UNTER)) && remainingCards >= 1
				&& !deckClosed;
	}

	public boolean trumpSwitchable() {
		return trumpCard.getValue() != CardValue.UNTER;
	}

	protected GameState createNewStateWithTrick(Trick t) {
		boolean botOwnsFirstCard = t.botPlayedFirstCard();
		boolean botWinsTrick = t.card1WinsTrick(trumpColor) == botOwnsFirstCard;
		if (botWinsTrick) {
			Deck newTricks = (Deck) tricks.clone();
			newTricks.add(t.getFirstCard());
			newTricks.add(t.getSecondCard());
			GameState gs = new GameState(trumpCard, hand.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()),
					knownCardsOpponent.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()), newTricks,
					(Deck) tricksOpponent.clone(), possibleCardsOpponent.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()),
					extraPoints, extraPointsOpponent, deckClosed ? remainingCards : Math.max(remainingCards - 2, 0), deckClosed,
					botClosedDeck, closedDeckScore, t, true);
			if (deckClosed && botOwnsFirstCard && gs.possibleCardsOpponent.size() > 0) {
				assert (possibleCardsOpponent.size() > hand.size());
				gs.closedDeckLogic(t);
			}
			return gs;
		} else {
			Deck newTricksOpponent = (Deck) tricksOpponent.clone();
			newTricksOpponent.add(t.getFirstCard());
			newTricksOpponent.add(t.getSecondCard());
			GameState gs = new GameState(trumpCard, hand.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()),
					knownCardsOpponent.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()), (Deck) tricks.clone(),
					newTricksOpponent, (Deck) possibleCardsOpponent.cloneAndRemove2(t.getSecondCard(), t.getFirstCard()),
					extraPoints, extraPointsOpponent, deckClosed ? remainingCards : Math.max(remainingCards - 2, 0), deckClosed,
					botClosedDeck, closedDeckScore, t, false);
			if (deckClosed && botOwnsFirstCard && gs.possibleCardsOpponent.size() > 0) {
				assert (possibleCardsOpponent.size() > hand.size());
				gs.closedDeckLogicLose(t);
			}
			return gs;
		}
	}

	private void closedDeckLogic(Trick t) {
		Card botCard = t.getFirstCard();
		Card opponentCard = t.getSecondCard();
		CardColor color = botCard.getColor();
		if (opponentCard.getColor() != botCard.getColor()) {
			for (CardValue value : CardValue.values()) {
				possibleCardsOpponent.remove(new Card(color, value));
			}
		} else {
			// opponentCard is lower than botCard
			assert (opponentCard.getValue().getValue() < botCard.getValue().getValue());
			for (CardValue higher : botCard.getValue().valuesAboveMe()) {
				possibleCardsOpponent.remove(new Card(color, higher));
			}
			assert (!(possibleCardsOpponent.isEmpty() && !hand.isEmpty()));
		}
		assert (possibleCardsOpponent.size() >= hand.size());
	}

	private void closedDeckLogicLose(Trick t) {
		Card botCard = t.getFirstCard();
		Card opponentCard = t.getSecondCard();
		CardColor color = botCard.getColor();
		if (opponentCard.getColor() != color) {
			// opponent played trump card
			for (CardValue value : CardValue.values()) {
				possibleCardsOpponent.remove(new Card(color, value));
			}
		}
		assert (possibleCardsOpponent.size() >= hand.size());
	}

	public int points() {
		if (tricks.isEmpty()) {
			return 0;
		}
		return tricks.points() + extraPoints;
	}

	public int pointsOpponent() {
		if (tricksOpponent.isEmpty()) {
			return 0;
		}
		return tricksOpponent.points() + extraPointsOpponent;
	}

	public WinState getWinState() {
		int points = points();
		int pointsOpponent = pointsOpponent();

		if (points >= 66) {
			if (pointsOpponent >= 33) {
				return WinState.WIN1;
			}
			if (pointsOpponent == 0) {
				return WinState.WIN3;
			}
			return WinState.WIN2;
		}
		if (pointsOpponent >= 66) {
			if (points >= 33) {
				return WinState.LOSE1;
			}
			if (points == 0) {
				return WinState.LOSE3;
			}
			return WinState.LOSE2;
		}
		if (points + pointsOpponent == 120) {
			assert (points >= 33 && pointsOpponent >= 33);
			// no card left to play, last trick
			// points are always 1 here
			return botTurn ? WinState.WIN1 : WinState.LOSE1;
		}
		if (deckClosed && remainingCards > 0 && noCardLeft()) {
			if (botClosedDeck) {
				if (closedDeckScore >= 33) {
					return WinState.LOSE1;
				}
				if (closedDeckScore == 0) {
					return WinState.LOSE3;
				}
				return WinState.LOSE2;
			}
			if (closedDeckScore >= 33) {
				return WinState.WIN1;
			}
			if (closedDeckScore == 0) {
				return WinState.WIN3;
			}
			return WinState.WIN2;
		}
		return WinState.DRAW;
	}

	// TREE STUFF

	public GameState parent;
	public double visits;
	public double wins1;
	public double wins2;
	public double wins3;

	public double losses1;
	public double losses2;
	public double losses3;

	public boolean fullyExtended;

	public void backpropagate(WinState state) {
		switch (state) {
		case LOSE1:
			this.losses1++;
			break;
		case LOSE2:
			this.losses2++;
			break;
		case LOSE3:
			this.losses3++;
			break;
		case WIN1:
			this.wins1++;
			break;
		case WIN2:
			this.wins2++;
			break;
		case WIN3:
			this.wins3++;
			break;
		default:
			throw new RuntimeException();
		}
		this.visits++;
		if (cachedSubs != null && !fullyExtended) {
			boolean fullyExtended = true;
			for (GameState sub : cachedSubs) {
				if (!sub.fullyExtended) {
					fullyExtended = false;
					break;
				}
			}
			if (fullyExtended) {
				this.fullyExtended = true;
				this.cachedSubs = null;
			}
		}
		if (parent != null)
			parent.backpropagate(state);
	}

	public double getConfidenceUCB1Tuned(double rootVisits) {
		double payoff = deckClosed ? getAbsoluteExploitation() : getExploitation();
		// variance can be replaced with payoff - (payoff * payoff) to speed up
		return payoff + Math.sqrt((Math.log(rootVisits) / visits)
				* Math.min(0.25, (getVariance() + Math.sqrt(2 * (Math.log(rootVisits)) / visits))));
	}

	private double getVariance() {
		if (visits <= 1) {
			return 0;
		}
		double sum = 0;
		sum += losses1 * 0.33333333333;
		sum += losses2 * 0.16666666666;
		// sum += losses3 * 0;
		sum += wins1 * 0.66666666666;
		sum += wins2 * 0.83333333333;
		sum += wins3;
		double mean = sum / visits;
		double temp = 0;
		for (int i = 0; i < losses1; i++)
			temp += (0.33333333333 - mean) * (0.33333333333 - mean);
		for (int i = 0; i < losses2; i++)
			temp += (0.16666666666 - mean) * (0.16666666666 - mean);
		for (int i = 0; i < losses3; i++)
			temp += (0 - mean) * (0 - mean);
		for (int i = 0; i < wins1; i++)
			temp += (0.66666666666 - mean) * (0.66666666666 - mean);
		for (int i = 0; i < wins2; i++)
			temp += (0.83333333333 - mean) * (0.83333333333 - mean);
		for (int i = 0; i < wins3; i++)
			temp += (1 - mean) * (1 - mean);
		return temp / (visits - 1);
	}

	public double getExploitation() {
		return (wins1 / visits) * 0.333 + (wins2 / visits) * 0.666 + (wins3 / visits);
	}

	public double getAbsoluteExploitation() {
		return (wins1 + wins2 + wins3) / visits;
	}

	public CardColor getTrumpColor() {
		return trumpColor;
	}

	public void setTrumpColor(CardColor trumpColor) {
		this.trumpColor = trumpColor;
	}

	public Card getTrumpCard() {
		return trumpCard;
	}

	public void setTrumpCard(Card trumpCard) {
		this.trumpCard = trumpCard;
	}

	public Deck getHand() {
		return hand;
	}

	public void setHand(Deck hand) {
		this.hand = hand;
	}

	public Deck getTricks() {
		return tricks;
	}

	public void setTricks(Deck tricks) {
		this.tricks = tricks;
	}

	public Deck getTricksOpponent() {
		return tricksOpponent;
	}

	public void setTricksOpponent(Deck tricksOpponent) {
		this.tricksOpponent = tricksOpponent;
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

	/**
	 * @return int from 0 to 10
	 */
	public int getRemainingCards() {
		return remainingCards;
	}

	public void setRemainingCards(int remainingCards) {
		this.remainingCards = remainingCards;
	}

	public boolean isDeckClosed() {
		return deckClosed;
	}

	public void setDeckClosed(boolean deckClosed) {
		this.deckClosed = deckClosed;
	}

	public Trick getCurrentTrick() {
		return currentTrick;
	}

	public void setCurrentTrick(Trick currentTrick) {
		this.currentTrick = currentTrick;
	}

	public boolean isBotTurn() {
		return botTurn;
	}

	public void setBotTurn(boolean botTurn) {
		this.botTurn = botTurn;
	}

	public boolean isBotClosedDeck() {
		return botClosedDeck;
	}

	public void setBotClosedDeck(boolean botClosedDeck) {
		this.botClosedDeck = botClosedDeck;
	}

	public int getClosedDeckScore() {
		return closedDeckScore;
	}

	public void setClosedDeckScore(int closedDeckScore) {
		this.closedDeckScore = closedDeckScore;
	}

	public Deck getPossibleCardsOpponent() {
		return possibleCardsOpponent;
	}
}
