package me.nov.schnapsenai;

import java.util.Scanner;

import me.nov.schnapsenai.bots.impl.TreeSearchBot;
import me.nov.schnapsenai.card.Card;
import me.nov.schnapsenai.card.Deck;

public class SchnapsenAI {

	private static Scanner input;
	private static TreeSearchBot bot;

	public static void main(String[] args) {
		Card trumpCard = null;
		String[] hand = null;
		try {
			trumpCard = new Card(args[0]);
			hand = args[1].split(",");
		} catch (Exception e) {
			log("Wrong arguments, sample input: \"ho hu,ss,sk,lo,ez\"");
			e.printStackTrace();
			return;
		}
		initScanning();
		initBot(trumpCard, hand);
		log("Bot initialized, waiting for commands...");
		log("Note: Bot always plays marriages");

		while (input.hasNext()) {
			String command = input.nextLine();
			String[] split = command.split(" ");
			String argument = split[0];
			try {
				if (!handleCommand(argument.toLowerCase(), split)) {
					log("Command not found");
				}
			} catch (Exception e) {
				throw new RuntimeException("Command execution failed", e);
			}
		}
	}

	private static void initBot(Card trumpCard, String[] handCards) {
		bot = new TreeSearchBot(trumpCard);

		Deck hand = new Deck();
		bot.setHand(hand);
		for (String handCard : handCards) {
			hand.add(new Card(handCard));
		}
	}

	private static boolean handleCommand(String argument, String[] split) {
		if (argument.equals("settrump")) {
			bot.setTrumpCard(new Card(split[1]));
		} else if (argument.equals("playcard") || argument.equals("pc")) {
			Card card = bot.botPlaysCard();
			if (bot.switchesTrump()) {
				log("Bot switches trump");
			}
			if (bot.closesDeck()) {
				log("Bot closes deck");
			}
			log("Bot plays card " + card);
		} else if (argument.equals("draw") || argument.equals("d")) {
			Deck hand = bot.getHand();
			if (hand.size() == 4) {
				hand.add(new Card(split[1]));
			} else {
				log("Cannot draw card, bot already has " + hand.size() + " cards");
			}
			log("Bot draws card");
		} else if (argument.equals("opponentcard") || argument.equals("oc")) {
			Card botCard = bot.opponentPlaysCard(new Card(split[1]),
					split.length > 2 ? split[2].equalsIgnoreCase("marriage")|| split[2].equalsIgnoreCase("m") : false);
			if (botCard != null) {
				log("Bot plays card " + botCard);
			} else {
				log("Trick recorded");
			}
		} else if (argument.equals("opponenttrumpchange") || argument.equals("otc")) {
			bot.opponentSwitchesTrump();
			log("Opponent switches trump");
		} else if (argument.equals("opponentclosedeck")|| argument.equals("ocd")) {
			bot.opponentClosesDeck();
			log("Opponent closes deck");
		} else if (argument.equals("stats")) {
			log("Bot: " + (bot.getTricks().points() + bot.getExtraPoints()) + " Points (Extra: " + bot.getExtraPoints()
					+ "), Hand: " + bot.getHand() + ", Won Tricks: "
					+ bot.getTricks());
			log("Opponent: " + (bot.getTricksOpponent().points() + bot.getExtraPointsOpponent())
					+ " Points, Known Cards on Hand: " + bot.getKnownCardsOpponent() + ", Won Tricks: "
					+ bot.getTricksOpponent());
			log("Remaining Cards: " + bot.getRemainingCards() + ", Deck Closed: " + bot.isDeckClosed());

		} else {
			return false;
		}
		return true;
	}

	private static void initScanning() {
		input = new Scanner(System.in);
	}

	public static void log(String text) {
		System.out.println(text);
	}
}
