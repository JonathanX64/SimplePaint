package jonathan.ftp.sh.pain.view;

import jonathan.ftp.sh.pain.Line;
import jonathan.ftp.sh.pain.model.BoardModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class BoardView extends JPanel implements MouseListener, MouseMotionListener {
    public static Color current = Color.PINK;
    public static Byte stroke = 2;
    //TODO replace with BasicStroke at some point
    public static String tool = "Draw";
    private static Color previous;
    private final ArrayList<Line> link;
    private BoardModel x;
    private GeneralPath p = new GeneralPath();

    public BoardView(BoardModel data) {
        x = data;
        link = x.getLines();

        addMouseListener(this);
        addMouseMotionListener(this);

        final Timer timer = new Timer(1000, e -> this.repaint());//in case something happens on another client
        //haven't figured this out yet :(
        timer.start();
    }

    @Override
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D g = (Graphics2D) gfx;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        setBackground(BoardModel.bg);
        g.setColor(Color.BLACK);
        g.drawString("Draw something by dragging the mouse!", 10, 20);

        synchronized (link) {
            for (Line old_path : x.getLines()) {
                g.setColor(old_path.getColor());
                g.setStroke(new BasicStroke(old_path.getThiccness()));
                g.draw(old_path.getPath());
            }
        }

        previous = current;
        g.setColor(current);
        g.setStroke(new BasicStroke(stroke));
        g.draw(p);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        switch (tool) {
            case "Draw":
                p = new GeneralPath();
                p.moveTo(me.getX(), me.getY());
                break;

            case "Erase":
                x.removeLine(me.getPoint());
                break;

            default:
                break;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {

        switch (tool) {
            case "Draw":
                x.addLine(new Line(p, stroke, previous));
                break;

            case "Erase":
                x.removeLine(me.getPoint());
                break;

            default:
                break;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent me) {

        switch (tool) {
            case "Draw":
                p.lineTo(me.getX(), me.getY());
                break;

            case "Erase":
                synchronized (link) {
                    link.removeIf(line -> line.getPath().contains(me.getPoint()));
                }
                x.removeLine(me.getPoint());
                break;

            default:
                break;
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

}
