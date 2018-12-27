package me.nov.schnapsenai.mcts;

import me.nov.schnapsenai.tree.GameState;

public abstract class TreeSearch {
	public abstract double winPercentage(GameState root, GameState gs, int i, long maxTime, boolean absolute);
}
