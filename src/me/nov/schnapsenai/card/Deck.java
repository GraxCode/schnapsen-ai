package me.nov.schnapsenai.card;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Deck implements Iterable<Card> {
	private int deck;
	public Deck(List<Card> list) {
		for (Card c : list)
			this.add(c);
	}

	public Deck(int deck) {
		this.deck = deck;
	}

	public Deck() {
	}

	public Deck(Deck d) {
		this.deck = d.deck;
	}

	public boolean add(Card e) {
		this.deck |= 1 << getPos(e);
		return true;
	}

	public boolean addAll(List<Card> e) {
		for (Card c : e)
			add(c);
		return true;
	}

	public boolean addAll(Deck e) {
		int d = deck;
		deck |= e.deck;
		return d != deck;
	}

	public boolean remove(Card e) {
		this.deck &= ~(1 << getPos(e));
		return true;
	}

	public boolean removeAll(List<Card> e) {
		for (Card c : e)
			remove(c);
		return true;
	}

	public boolean removeAll(Deck e) {
		int d = deck;
		deck &= ~e.deck;
		return d != deck;
	}

	public boolean contains(Card e) {
		return (deck & (1 << getPos(e))) != 0;
	}

	public Deck difference(Deck e) {
		return new Deck(deck & ~e.deck);
	}

	public boolean isEmpty() {
		return deck == 0;
	}

	private int getPos(Card e) {
		return e.getColor().ordinal() * 5 + e.getValue().ordinal();
	}

	public Deck cloneAndRemove(Card card) {
		Deck deck2 = (Deck) this.clone();
		if (card != null)
			deck2.remove(card); // deck doesn't have to contain card
		return deck2;
	}

	public Deck cloneAndRemove2(Card card, Card card2) {
		Deck deck2 = (Deck) this.clone();
		if (card != null)
			deck2.remove(card);
		if (card2 != null)
			deck2.remove(card2);
		return deck2;
	}

	public int size() {
		return Integer.bitCount(deck);
	}

	public int points() {

		int pts = 0;
		for (int i = 19; i >= 0; i--) {
			if ((deck & (1 << i)) != 0)
				switch (i % 5) {
				case 4:
					pts += 11;
					break;
				case 3:
					pts += 10;
					break;
				case 2:
					pts += 4;
					break;
				case 1:
					pts += 3;
					break;
				default:
					pts += 2;
					break;
				}
		}
		return pts;
	}

	public Deck clone() {
		return new Deck(deck);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Deck) {
			Deck d = (Deck) o;
			return d.deck == deck;
		}
		return false;
	}

	protected Card getCard(int pos) {
		return new Card(CardColor.values()[pos / 5], CardValue.values()[pos % 5]);
	}

	@Override
	public Iterator<Card> iterator() {
		return new Iterator<Card>() {

			private int pointer = 20;

			@Override
			public boolean hasNext() {
				return pointer >= 0 && (deck & (1 << pointer) - 1) > 0;
			}

			@Override
			public Card next() {
				while (pointer >= 0) {
					pointer--;
					if ((deck & (1 << pointer)) != 0) {
						Card card = getCard(pointer);
						return card;
					}
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
			}
		};
	}

	public int hash() {
		return deck;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Card s : this) {
			sb.append(s).append(" ");
		}
		return sb.append("]").toString();
	}
}
