import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Dealer {
    private final List<Player> players;
    private final Map<Player, HashSet<Card>> hands;
    private final Map<Suit, Tower> towers;

    private boolean gameStarted = false;
    private int playerCount = 0;
    private int nextPlayerIndex = 0;

    public Dealer() {
        players = new ArrayList<Player>();
        hands = new HashMap<Player, HashSet<Card>>();
        towers = new HashMap<Suit, Tower>();
        for (Suit suit : Suit.values())
            towers.put(suit, new Tower());
    }

    public void addPlayer(Player player) throws GregNoobException {
        if (!gameStarted || player == null)
            if (!players.contains(player)) {
                players.add(player);
                hands.put(player, new HashSet<Card>());
                playerCount++;
            } else
                throw new GregNoobException();
    }

    public void deal() {
        if (playerCount >= 3) {
            gameStarted = true;

            int[] playerCardCount = new int[playerCount];

            // Initialize an unshuffled deck
            List<Card> pack = new ArrayList<Card>();
            for (int i = 1; i <= 52; i++) {
                pack.add(new Card(i));
            }

            // Pick random card from the deck, and give it to the next player
            Random rng = new Random();
            for (int i = 0; i < 52; i++) {
                int playerNum = i % playerCount;
                Player player = players.get(playerNum);

                Card nextCard = pack.remove(rng.nextInt(52 - i));
                hands.get(player).add(nextCard);
                playerCardCount[playerNum]++;
            }

            GameConfig config = new GameConfig(playerCount, playerCardCount);
            for (Player player : players) {
                player.initialize(config);
                player.deal((Set<Card>) hands.get(player).clone());
            }
        }
    }

    private Player playStep() throws SomeoneNoobException {
        if (!gameStarted || gameFinished())
            throw new GregNoobException();

        Player player = players.get(nextPlayerIndex);

        Card resort;
        for (Card card : hands.get(player)){
            Tower tower = towers.get(card.getSuit());
            if (tower.canPlay(card.getCardIndex()))
                resort = card;
        }


        Card nextCard = player.play();

        if (nextCard == null ||
            !hands.get(player).contains(nextCard) ||
            !towers.get(nextCard.getSuit()).canPlay(nextCard.getCardIndex())) {
            nextCard = resort;
            System.out.println(player.getName() + " is such an movl. You can't play " + nextCard.toString() + " now!");
        }
            
        if (nextCard != null) {
            towers.get(nextCard.getSuit()).play(nextCard.getCardIndex());
            System.out.println(player.getName() + " plays " + nextCard.toString());
            for (Player p : players)
                p.movePlayed(nextPlayerIndex, nextCard);
        } else {
            System.out.println(player.getName() + " cannot play");
        }

        nextPlayerIndex = (nextPlayerIndex + 1) % playerCount;
        return player;
    }

    public Player playAll() throws SomeoneNoobException {
        Player currentPlayer = null;
        while (!gameFinished()) {
            currentPlayer = playStep();
            if (hands.get(currentPlayer).isEmpty())
                break;
        }
        return currentPlayer;
    }

    public boolean gameFinished() {
        return (towers.get(Suit.DIAMOND).isClosed() && towers.get(Suit.CLUB).isClosed() && towers.get(Suit.HEART).isClosed() && towers
                .get(Suit.SPADE).isClosed());
    }

    private class Tower {
        int upperBound = 7;
        int lowerBound = 7;

        public void play(int card) throws GregNoobException {
            if (!canPlay(card))
                throw new GregNoobException();

            if (card == upperBound)
                upperBound += 1;

            if (card == lowerBound)
                lowerBound -= 1;
        }

        public boolean canPlay(int cardIndex) {
            return !isClosed() && (cardIndex == lowerBound || cardIndex == upperBound);
        }

        public boolean isClosed() {
            return lowerBound <= 0 && upperBound >= 14;
        }
    }
}
