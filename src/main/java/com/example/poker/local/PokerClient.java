import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class PokerClient {
    public static void main(String[] args) {
        try (
            Scanner scanner = new Scanner(System.in);
        ) {
            System.out.print("ポートを入力してください(5000など) → ");
            int port = scanner.nextInt();

            Socket socket = new Socket("localhost", port);
            System.out.println("接続されました");

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            System.out.print("名前を入力してください: ");
            String name = scanner.next();
            if (name.equalsIgnoreCase("exit") || name.equalsIgnoreCase("quit")) {
                socket.close();
                return;
            }

            oos.writeObject(name);
            oos.flush();

            // 手札の受信
            List<Poker.Card> hand = (List<Poker.Card>) ois.readObject();
            System.out.println("あなたの手札:");
            for (Poker.Card card : hand) {
                System.out.println(card);
            }

            GameRound round = new GameRound(null);
            // クライアントがアクションするまで待ち
            round.runClientRound(scanner, ois, oos);

            // 終了処理
            ois.close();
            oos.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("エラーが発生したのでプログラムを終了します");
            e.printStackTrace();
        }
    }
}