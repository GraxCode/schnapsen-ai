package me.nov.schnapsenai.mcts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.CardValue;
import me.nov.schnapsenai.tree.GameState;
import me.nov.schnapsenai.tree.WinState;
import me.nov.schnapsenai.utils.XorShift128PlusRandom;

/**
 * Expectiminimax variant for imperfect games
 * @author GraxCode
 *
 */
public class ImperfectExpectiMiniMax extends TreeSearch {

	public static final XorShift128PlusRandom r = new XorShift128PlusRandom();
	private static final double pointWeighting = 1;
	private static final double pruningRange = 0.02;

	@Override
	public double winPercentage(GameState root, GameState gs, int i, long maxTime, boolean absolute) {
		GameState.CACHING = false;
		WinState winState = gs.getWinState();
		if (winState != WinState.DRAW)
			return getWeighting(gs);
		long ms = System.currentTimeMillis();
		double calculated = 0;
		double calculations = 0;
		while (System.currentTimeMillis() - ms < maxTime) {
			calculations++;
			long ms2 = System.currentTimeMillis();
			double wr = getWinInterval(gs, 0);
			calculated += wr;
			long calculationTime = System.currentTimeMillis() - ms2;
			if (System.currentTimeMillis() - ms + calculationTime > maxTime || calculations > 100)
				break;
		}
		return calculated / calculations;
	}

	private double getWinInterval(GameState gs, double depth) {
		WinState winState = gs.getWinState();
		if (winState != WinState.DRAW) {
			double weighting = getWeighting(gs);
			return weighting;
		}
		boolean botTurn = gs.isBotTurn();
		boolean newCardsDrawn = gs.getCurrentTrick().oneCardPlayed() && !gs.isDeckClosed();

		if (depth > 2 && !newCardsDrawn && !gs.isDeckClosed()) {
			return evaluation(gs);
		}
		ArrayList<GameState> subs = gs.getSubordinates();
		double bestPossible = bestPossibleResult(gs);

		bestPossible *= (1 - pruningRange);

		double totalSize = subs.size();

		double midSum = 0;

		double depthIncreasement = Math.log10(totalSize);

		double bestMid = Double.NEGATIVE_INFINITY;
		double worstMid = Double.POSITIVE_INFINITY;
		ArrayList<Double> bestOpponentChoices = !botTurn ? new ArrayList<>() : null;

		int remainingCards = gs.getRemainingCards();
		for (GameState sub : subs) {
			double wr = getWinInterval(sub, depth + depthIncreasement);
			if (!newCardsDrawn) {
				if (botTurn) {
					if (wr >= bestPossible)
						return wr;
					if (wr >= bestMid) {
						bestMid = wr;
					}
				} else {
					bestOpponentChoices.add(wr);
					if (wr <= worstMid) {
						worstMid = wr;
					}
				}
			}
			midSum += wr;
		}
		if (!newCardsDrawn) {
			if (botTurn) {
				return bestMid;
			} else {
				if (remainingCards == 0) {
					int possibleOptions = (int) totalSize;
					Collections.sort(bestOpponentChoices);
					return calculateRatingLow(bestOpponentChoices, 0, 0, totalSize, possibleOptions)
							/ (double) binomialCoeff((int) totalSize, possibleOptions);
				} else if (gs.getCurrentTrick().oneCardPlayed()) {
					int possibleOptions = (int) Math.min(5, totalSize);
					Collections.sort(bestOpponentChoices);
					return calculateRatingLow(bestOpponentChoices, 0, 0, totalSize, possibleOptions)
							/ (double) binomialCoeff((int) totalSize, possibleOptions);
				} else {
					Card lowestTrump = new Card(gs.getTrumpColor(), CardValue.UNTER);
					boolean switchable = gs.trumpSwitchable() && !gs.getHand().contains(lowestTrump);
					boolean closeable = gs.deckClosePossible();
					int possibleOptions = switchable ? 6 : 5; //estimation
					if (closeable) {
						possibleOptions *= 2;
						if (switchable) {
							possibleOptions *= 2;
							possibleOptions -= 2 * (closeable ? 2 : 1);
						}
					}
					if (possibleOptions > totalSize) {
						possibleOptions = (int) totalSize;
					}
					Collections.sort(bestOpponentChoices);
					return calculateRating(bestOpponentChoices, BigDecimal.ZERO, 0, totalSize, possibleOptions)
							.divide(binomialCoeffBig((int) totalSize, possibleOptions), RoundingMode.HALF_UP).doubleValue();
				}
			}
		}

		return midSum / totalSize;
	}

	public static BigDecimal calculateRating(List<Double> list, BigDecimal avg, int i, double listSize,
			double possibleOptions) {
		if (listSize < possibleOptions) {
			return avg;
		}
		BigDecimal diff = binomialCoeffBig((int) listSize, (int) possibleOptions)
				.multiply(BigDecimal.valueOf((((possibleOptions - listSize) / (double) listSize) + 1)));
		avg = avg.add(BigDecimal.valueOf(list.get(i)).multiply(diff));
		return calculateRating(list, avg, i + 1, listSize - 1, possibleOptions);
	}

	public static double calculateRatingLow(List<Double> list, double avg, int i, double listSize,
			double possibleOptions) {
		if (listSize < possibleOptions) {
			return avg;
		}
		double diff = binomialCoeff((int) listSize, (int) possibleOptions)
				* (((possibleOptions - listSize) / (double) listSize) + 1);
		if (diff < 0)
			throw new RuntimeException("overflow");
		avg += list.get(i) * (double) diff;
		return calculateRatingLow(list, avg, i + 1, listSize - 1, possibleOptions);
	}

	private static BigDecimal binomialCoeffBig(final int N, final int K) {
		if (N < K)
			return BigDecimal.ZERO;
		BigDecimal ret = BigDecimal.ONE;
		for (int k = 0; k < K; k++) {
			ret = ret.multiply(BigDecimal.valueOf(N - k)).divide(BigDecimal.valueOf(k + 1));
		}
		return ret;
	}

	private static long binomialCoeff(int n, int k) {
		if (n < k)
			return 0;
		if (k > n - k)
			k = n - k;

		long b = 1;
		for (int i = 1, m = n; i <= k; i++, m--)
			b = b * m / i;
		return b;
	}

	private double evaluation(GameState gs) {
		double sum = 0;
		double tries = 8;
		for (int i = 0; i < tries; i++) {
			sum += randomEnd(gs);
		}
		double weighting = sum / tries;
		return weighting;
	}

	private double bestPossibleResult(GameState gs) {
		int points;
		if (!gs.isDeckClosed()) {
			points = gs.pointsOpponent();
		} else {
			points = gs.getClosedDeckScore();
		}
		if (points == 0) {
			return 1;
		}
		if (points < 33) {
			return 1 - pointWeighting / 6;
		}
		return 1 - pointWeighting / 3;
	}

	public double randomEnd(GameState gs) {
		WinState winState = gs.getWinState();
		if (winState != WinState.DRAW) {
			return getWeighting(gs);
		}
		ArrayList<GameState> subs = gs.getSubordinates();
		double nonDrawsAvg = 0;
		Label: {
			for (GameState sub : subs) {
				if (sub.getWinState() == WinState.DRAW) {
					break Label;
				}
				nonDrawsAvg += getWeighting(sub);
			}
			return nonDrawsAvg / (double) subs.size();
		}
		return randomEnd(subs.get(r.nextInt(subs.size())));
	}

	public double getWeighting(GameState gs) {
		switch (gs.getWinState()) {
		case WIN1:
			return 1 - pointWeighting / 3;
		case WIN2:
			return 1 - pointWeighting / 6;
		case WIN3:
			return 1;
		case LOSE1:
			return pointWeighting / 3;
		case LOSE2:
			return pointWeighting / 6;
		case LOSE3:
			return 0;
		default:
			throw new RuntimeException();
		}
	}
}
