import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class GameRound {
    private final Poker poker;
    private final Action actionHandler;

    public GameRound(Poker poker) {
        this.poker = poker;
        this.actionHandler = new Action(poker);
    }

    /**
     * サーバー側でラウンドを進行するメソッド
     */
    public void runServerRound(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
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

    /**
     * クライアント側でラウンドを進行するメソッド
     */
    public void runClientRound(Scanner scanner, ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        // サーバーのアクション受信、アクションするまで待ち
        ActionData serverAction = (ActionData) ois.readObject();
        System.out.println("サーバーが " + serverAction.action + (serverAction.amount > 0 ? "（" + serverAction.amount + "）" : "") + " しました");

        // クライアントの応答アクション
        /*Poker.Player clientPlayer = poker.getPlayers().get(1);
        System.out.println("あなたの手札:");
            for (Poker.Card card : clientPlayer.getHand()) {
                System.out.println(card);
            }*/
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
}


