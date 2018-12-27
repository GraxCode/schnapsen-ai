package me.nov.schnapsenai.card;

public enum CardValue {
	UNTER(2), OBER(3), KOENIG(4), ZEHNER(10), SAU(11);

	private int value;

	private CardValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	
	/**
	 * Returns CardValues higher than current value
	 */
	public CardValue[] valuesAboveMe() {
		int valuesAbove = values().length - 1 - this.ordinal();
		CardValue[] vals = new CardValue[valuesAbove];
		System.arraycopy(values(), this.ordinal(), vals, 0, valuesAbove);
		return vals;
	}
	/**
	 * Returns CardValues lower than current value
	 */
	public CardValue[] valuesBelowMe() {
		CardValue[] vals = new CardValue[this.ordinal()];
		System.arraycopy(values(), 0, vals, 0, this.ordinal());
		return vals;
	}
}
