
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.util.*;

class PokerCanvas extends JPanel implements MouseListener {

    private final GameState gameState;
    private final Map<Rectangle, Integer> playerCardAreas = new HashMap<>();
    private final Map<Rectangle, String> actionAreas = new HashMap<>();

    public PokerCanvas(GameState gameState) {
        this.gameState = gameState;
        setBackground(new Color(0, 102, 0));
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCommunityCards(g);
        drawPlayerCards(g);
        drawPot(g);
        drawActionArea(g);
        drawLastAction(g);
    }

    private void drawCommunityCards(Graphics g) {
        List<String> cards = gameState.getCommunityCards();
        int startX = 250;
        int y = 200;
        for (int i = 0; i < cards.size(); i++) {
            drawCard(g, cards.get(i), startX + i * 90, y, false);
        }
    }

    private void drawPlayerCards(Graphics g) {
        List<String> cards = gameState.getPlayerCards();
        Set<Integer> selected = gameState.getSelectedCardIndexes();
        int startX = 400;
        int y = 450;
        playerCardAreas.clear();

        for (int i = 0; i < cards.size(); i++) {
            int x = startX + i * 90;
            drawCard(g, cards.get(i), x, y, selected.contains(i));
            playerCardAreas.put(new Rectangle(x, y, 70, 100), i);
        }
    }

    private void drawPot(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Pot: $" + gameState.getPot(), 450, 150);
    }

    private void drawLastAction(Graphics g) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Last Action: " + gameState.getLastAction(), 20, 30);
    }

    private void drawCard(Graphics g, String label, int x, int y, boolean selected) {
        if (selected) {
            g.setColor(Color.ORANGE);
            g.fillRoundRect(x - 3, y - 3, 76, 106, 12, 12);
        }

        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, 70, 100, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, 70, 100, 10, 10);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(label, x + 10, y + 50);
    }

    private void drawActionArea(Graphics g) {
        actionAreas.clear();
        String[] actions = { "Fold", "Check", "Call", "Raise" };
        int startX = 100;
        int y = 650;
        int width = 130;
        int height = 50;
        int gap = 20;

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        for (int i = 0; i < actions.length; i++) {
            int x = startX + i * (width + gap);
            Rectangle rect = new Rectangle(x, y, width, height);
            actionAreas.put(rect, actions[i]);

            if (actions[i].equals(gameState.getSelectedAction())) {
                g.setColor(Color.ORANGE);
                g.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 12, 12);
            }

            g.setColor(Color.LIGHT_GRAY);
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, width, height, 10, 10);
            g.drawString(actions[i], x + 30, y + 30);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();

        // カード選択
        for (Rectangle rect : playerCardAreas.keySet()) {
            if (rect.contains(p)) {
                int index = playerCardAreas.get(rect);
                gameState.toggleCardSelection(index);
                repaint();
                return;
            }
        }

        // アクション選択
        for (Rectangle rect : actionAreas.keySet()) {
            if (rect.contains(p)) {
                String action = actionAreas.get(rect);
                if (action.equals("Raise")) {
                    String input = JOptionPane.showInputDialog(this, "Enter raise amount:");
                    if (input != null && input.matches("\\d+")) {
                        gameState.setLastAction("Raise $" + input);
                        gameState.setSelectedAction(action);
                    }
                } else {
                    gameState.setLastAction(action);
                    gameState.setSelectedAction(action);
                }
                repaint();
                return;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
