package Part2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;
import javax.swing.JPanel;

/**
 * @author aakash
 */
public class GraphPanel extends JPanel {

  private static final int WIDTH = 400;
  private static final int HEIGHT = 300;
  private static final int BORDER_GAP = 30;
  private static final int X_OFFSET = 50;
  private static final int Y_OFFSET = 50;
  private static final int GRAPH_WIDTH = WIDTH - BORDER_GAP - X_OFFSET;
  private static final int GRAPH_HEIGHT = HEIGHT - BORDER_GAP - Y_OFFSET;
  private static final Color GRAPH_COLOR = Color.green;
  private static final int POINT_SIZE = 10;
  private static final double SCALE_FACTOR = 0.01;

  private Map<Long, Integer> data;

  public GraphPanel(Map<Long, Integer> data) {
    this.data = data;
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.black);
    g.drawLine(X_OFFSET, HEIGHT - Y_OFFSET, X_OFFSET, BORDER_GAP);
    g.drawLine(X_OFFSET, HEIGHT - Y_OFFSET, WIDTH - BORDER_GAP, HEIGHT - Y_OFFSET);
    g.setColor(GRAPH_COLOR);
    int x = X_OFFSET;
    int y = HEIGHT - Y_OFFSET;
    for (int i = 0; i < data.size(); i++) {
      int x1 = (int)(i);
      int y1 = (int)(data.get(Long.parseLong(String.valueOf(i))));
      System.out.println(x1 + " : " + y1);
      g.fillOval(x1, y1, POINT_SIZE, POINT_SIZE);
    }
  }
}
