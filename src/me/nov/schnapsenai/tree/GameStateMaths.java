package me.nov.schnapsenai.tree;

import java.util.ArrayList;
import java.util.HashMap;

import me.nov.schnapsenai.mcts.TreeSearch;

public class GameStateMaths {

	private GameState currentState;

	public GameStateMaths(GameState currentState) {
		this.currentState = currentState;
	}

	public GameStateDifferences calculateBestMove(TreeSearch algorithm, boolean absolute) {
		HashMap<GameStateDifferences, Double> scores = new HashMap<>();
		HashMap<GameStateDifferences, Integer> vals = new HashMap<>();
		GameState pgs = currentState.clone();
		ArrayList<GameState> subs = pgs.getSubordinates();
		if (subs.isEmpty()) {
			System.out.println("No subs, returning null");
			return null;
		}
		int i = 0;
		long maxTime = 5000 / subs.size();
		long ms = System.currentTimeMillis();
		for (GameState gs : subs) {
			double score = algorithm.winPercentage(pgs, gs, i, maxTime, absolute);
			GameStateDifferences gsd = new GameStateDifferences(currentState, gs);
			scores.put(gsd, (double) getOrDefault(scores, gsd, 0.0d) + score);
			vals.put(gsd, (int) getOrDefault(vals, gsd, (int) 0) + 1);
			i++;
		}
		double bestScore = Double.NEGATIVE_INFINITY;
		GameStateDifferences bestDif = null;
		for (GameStateDifferences gsd : scores.keySet()) {
			double score = scores.get(gsd) / (double) vals.get(gsd);

			System.out.println("Card: " + gsd.cardDifference() + " Close: " + gsd.closedDeck() + " Switch: "
					+ gsd.switchedTrump() + " Marriage: " + gsd.isMarriage() + " Weighting: "
					+ (Math.round((score * 10000d)) / 100d) + "%" + " " + scores.get(gsd) + " " + vals.get(gsd));
			if (score >= bestScore) {
				bestScore = score;
				bestDif = gsd;
			}
		}
		System.out.println("Calculation time: " + (System.currentTimeMillis() - ms) + "ms");
		if (bestDif == null) {
			throw new RuntimeException(scores.size() + " " + subs.size());
		}
		return bestDif;
	}

	private Object getOrDefault(HashMap<?, ?> m, Object obj, Object def) {
		Object d = m.get(obj);
		if (d == null) {
			return def;
		}
		return d;
	}
}
