import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokerServer {
    private static final int PORT = 5000;
    private static List<Poker.Player> connectedPlayers = new ArrayList<>();
    private static List<String> playerNames;

    public static void main(String arg[]) {
        String serverName = "Server";

        try (Scanner scanner = new Scanner(System.in)) {
            List<String> playerNames = new ArrayList<>();
            try {
                /* 通信の準備をする */
                ServerSocket server = new ServerSocket(PORT); // ポート番号を指定し、クライアントとの接続の準備を行う


                
                playerNames.add(serverName);


                Socket socket = server.accept(); // クライアントからの接続要求を待つ

                System.out.println("接続しました。相手の入力を待っています......");

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                try {
                    String clientPlayerName = (String) ois.readObject();
                    if (clientPlayerName.equals("exit") || clientPlayerName.equals("quit")) {
                        ois.close();
                        oos.close();
                        socket.close();
                        server.close();
                        return;
                    }
                    playerNames.add(clientPlayerName);
                    System.out.println("クライアント " + clientPlayerName + " が参加しました。");
                } catch (Exception e) {
                    System.err.println("通信中にエラーが発生しました");
                    e.printStackTrace();
                }

                // サーバーとクライアントの2人でゲーム開始
                System.out.println("2人揃いました。ゲームを開始します。");
                
                Poker poker = new Poker();
                poker.startGame(playerNames);
                Action actionProcessor = new Action(poker);
                

                // クライアントの手札を取得し送信
                // サーバーのプレイヤーとクライアントのプレイヤーを取得
                Poker.Player serverPlayer = poker.getPlayers().get(0);
                Poker.Player clientPlayer = poker.getPlayers().get(1);
                oos.writeObject(clientPlayer.hand);
                oos.flush();

                // --- サーバー側のアクション ---
                System.out.println(serverPlayer.name + "さん、アクションを選んでください（bet / check）:");
                String serverAction = scanner.next();
                int serverAmount = 0;

                if (serverAction.equalsIgnoreCase("bet")) {
                    System.out.println("ベット額を入力してください:");
                    serverAmount = scanner.nextInt();
                }

                // アクション処理
                boolean ok = actionProcessor.processAction(serverPlayer, serverAction, serverAmount);
                if (!ok) {
                    System.out.println("無効なアクションです。");
                    // 再度入力待ちにするなどの処理を書く
                }

                // サーバーアクション送信
                oos.writeObject(serverAction);
                oos.flush();

                // --- クライアント側のアクション受信 ---
                ActionData clientActionData = (ActionData) ois.readObject();
                String clientAction = clientActionData.action;
                int clientAmount = clientActionData.amount;

                // Actionクラスで処理
                Action actionHandler = new Action(poker);
                boolean success = actionHandler.processAction(clientPlayer, clientAction, clientAmount);
                if (!success) {
                    System.out.println("クライアントのアクションが不正です: " + clientAction);
                }


                poker.printPlayerStatus();

                ois.close();
                oos.close();
                socket.close();
                server.close();
            } catch (BindException be) {
                be.printStackTrace();
                System.out.println("ポート番号が不正、ポートが使用中です");
                System.err.println("別のポート番号を指定してください(6000など)");
            } catch (Exception e) {
                System.err.println("エラーが発生したのでプログラムを終了します");
                throw new RuntimeException(e);
            }
        }
    }
}