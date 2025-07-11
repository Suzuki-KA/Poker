import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class GameRound {
    private final Poker poker;
    private final Action actionHandler;

    public GameRound(Poker poker) {
        this.poker = poker;
        this.actionHandler = new Action(poker);
    }

    
    // サーバー側でラウンドを進行するメソッド
    public void runServerRound(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        // 最初のcall,bet
        runServerAction(scanner, ois, oos);
        // フロップ
        serverFlop(oos);
        // 2回目のcall,bet
        runServerAction(scanner, ois, oos);
        // ターン
        serverDraw(oos, "ターン");
        // 3回目のcall,bet
        runServerAction(scanner, ois, oos);
        // リバー
        serverDraw(oos, "リバー");
        // 最後のcall,bet
        runServerAction(scanner, ois, oos);
        // 勝敗
        judge(oos);
    }
    public void runServerAction(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException{
        Poker.Player serverPlayer = poker.getPlayers().get(0);
        Poker.Player clientPlayer = poker.getPlayers().get(1);

        boolean actionFinished = false;

        while (!actionFinished) {
            // サーバーのアクション
            System.out.println("あなたの手札:");
            for (Poker.Card card : serverPlayer.getHand()) {
                System.out.println(card);
            }
            System.out.print(serverPlayer.name + "（サーバー） アクションを選んでください（bet / check）: ");
            String serverAction = scanner.next();
            int serverAmount = 0;
            if (serverAction.equals("bet")) {
                System.out.print("ベット額を入力してください: ");
                serverAmount = scanner.nextInt();
            }

            actionHandler.processAction(serverPlayer, serverAction, serverAmount);
            oos.writeObject(new ActionData(serverAction, serverAmount));
            oos.flush();

            // クライアントのアクション受信
            ActionData clientResponse = (ActionData) ois.readObject();
            actionHandler.processAction(clientPlayer, clientResponse.action, clientResponse.amount);
            System.out.println("クライアントが " + clientResponse.action + " しました");

            if (clientResponse.action.equals("raise")) {
                // サーバーが応答する
                System.out.print("クライアントがレイズしました。サーバー側の応答（call / raise / fold）: ");
                String responseAction = scanner.next();
                int responseAmount = 0;
                if (responseAction.equals("raise")) {
                    System.out.print("追加レイズ額を入力: ");
                    responseAmount = scanner.nextInt();
                }

                actionHandler.processAction(serverPlayer, responseAction, responseAmount);
                oos.writeObject(new ActionData(responseAction, responseAmount));
                oos.flush();

                // 終了条件
                if (responseAction.equals("call") || responseAction.equals("fold")) {
                    actionFinished = true;
                }
            } else {
                actionFinished = true;
            }
        }
    }


    // クライアント側でラウンドを進行するメソッド
    public void runClientRound(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos, List<Poker.Card> hand) throws ClassNotFoundException, IOException{
        // 最初のbet,call
        runClientAction(scanner, ois, oos, hand);
        // フロップ
        clientFlop(ois);
        // 2回目のbet,call
        runClientAction(scanner, ois, oos, hand);
        // ターン
        clientDraw(ois, "ターン");
        // 3回目のbet,call
        runClientAction(scanner, ois, oos, hand);
        // リバー
        clientDraw(ois, "リバー");
        // 最後のbet,call
        runClientAction(scanner, ois, oos, hand);
    }
    public void runClientAction(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos, List<Poker.Card> hand) throws IOException, ClassNotFoundException {
        // サーバーのアクション受信、アクションするまで待ち
        ActionData serverAction = (ActionData) ois.readObject();
        System.out.println("サーバーが " + serverAction.action + (serverAction.amount > 0 ? "（" + serverAction.amount + "）" : "") + " しました");

        System.out.println("あなたの手札:");
        for (Poker.Card card : hand) {
            System.out.println(card);
        }
        System.out.print("アクションを選んでください（call / raise / fold）: ");
        String clientAction = scanner.next();
        int amount = 0;
        if (clientAction.equals("raise")) {
            System.out.print("レイズ額を入力: ");
            amount = scanner.nextInt();
        }

        oos.writeObject(new ActionData(clientAction, amount));
        oos.flush();

        // もしクライアントが raise したら、サーバーからの再アクションを受信
        if (clientAction.equals("raise")) {
            ActionData serverResponse = (ActionData) ois.readObject();
            System.out.println("サーバーが " + serverResponse.action + (serverResponse.amount > 0 ? "（" + serverResponse.amount + "）" : "") + " しました");
        }
    }
    public void serverFlop(ObjectOutputStream oos) throws IOException{
        for(int i = 0; i < 3; i++){
            poker.dealTableCards();
        }
        List<Poker.Card> flop = poker.getTableCards();
        oos.writeObject(flop);
        System.out.println("フロップ:");
        for (Poker.Card card : flop) {
            System.out.println(card);
        }
    }
    public void clientFlop(ObjectInputStream ois) throws ClassNotFoundException, IOException{
        // フロップを受信して表示
        List<Poker.Card> flop = (List<Poker.Card>) ois.readObject();
        System.out.println("フロップ:");
        for (Poker.Card card : flop) {
            System.out.println(card);
        }
    }

    public void serverDraw(ObjectOutputStream oos, String action) throws IOException{
        poker.dealTableCards();
        List<Poker.Card> flop = poker.getTableCards();
        oos.writeObject(flop);
        System.out.println(action+":");
        for (Poker.Card card : flop) {
            System.out.println(card);
        }
    }

    public void clientDraw(ObjectInputStream ois, String action) throws ClassNotFoundException, IOException{
        // フロップを受信して表示
        List<Poker.Card> flop = (List<Poker.Card>) ois.readObject();
        System.out.println(action+":");
        for (Poker.Card card : flop) {
            System.out.println(card);
        }
    }

    public void judge(ObjectOutputStream oos) throws IOException, InterruptedException, ExecutionException {
        List<Poker.Player> winners = poker.evaluateHandsParallel(); // 勝者リスト（複数可）

        // 勝者の名前を文字列で送信（複数名の場合は連結）
        StringBuilder sb = new StringBuilder();
        if (winners.size() == 1) {
            sb.append("勝者: ").append(winners.get(0).name);
        } else {
            sb.append("引き分け: ");
            for (int i = 0; i < winners.size(); i++) {
                sb.append(winners.get(i).name);
                if (i < winners.size() - 1) sb.append(", ");
            }
        }

        oos.writeObject(sb.toString());
        oos.flush();

        System.out.println("[サーバー] " + sb.toString());
    }

}
