import java.util.List;
import java.util.concurrent.*;


public class Main {
    public static void main(String[] args) {
        Poker poker = new Poker();
        poker.startGame(List.of("Alice", "Bob"));
        poker.dealCards();
        poker.dealTableCards();

        System.out.println("テーブルカード: " + poker.getTableCards());

        for (Poker.Player p : poker.getPlayers()) {
            System.out.println(p.name + "の手札: " + p.hand);
        }

        List<Poker.Player> winners;
        try {
            // 並列評価に変更
            winners = poker.evaluateHandsParallel();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("勝者:");
        for (Poker.Player p : winners) {
            System.out.println(p.name);
        }
    }
}