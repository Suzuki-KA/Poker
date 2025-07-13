package kadai;

import javax.swing.*;

public class TexasHoldemGame extends JFrame {

    public TexasHoldemGame() {
        setTitle("Texas Hold'em Poker - Unified Clickable Actions");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        GameState state = new GameState();
        PokerCanvas canvas = new PokerCanvas(state);

        add(canvas);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TexasHoldemGame::new);
    }
}
