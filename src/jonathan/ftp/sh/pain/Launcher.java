package jonathan.ftp.sh.pain;

import jonathan.ftp.sh.pain.model.BoardModel;
import jonathan.ftp.sh.pain.view.BoardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

public class Launcher {

    private static ImageIcon img = new ImageIcon("./f05.png");
    //this.setIconImage(img.getImage());

    public static void main(String[] args) {

        SwingUtilities.invokeLater(Launcher::createAndShowGUI);

    }

    private static void createAndShowGUI() {

        JFrame.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        String chosenBoard;
        Socket socket;
        while (true) {
            try {

                String input = JOptionPane.showInputDialog("Enter in server address:", "localhost");
                if (input == null) System.exit(0);

                socket = new Socket(InetAddress.getByName(input), 50432);
                System.out.println("Socket connected successfully");

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                LinkedList<String> listOfBoards;
                listOfBoards = (LinkedList<String>) ois.readObject();

                listOfBoards.add("New Board...");
                chosenBoard = (String) JOptionPane.showInputDialog(
                        null, // we don't have a parent component in this example
                        "We found these boards; select New Board... to create another", // the message that will appear above the selection
                        "Board Picker", // the title that will appear in the window's caption
                        JOptionPane.QUESTION_MESSAGE, // style is question
                        null, // we don't show an Icon here, it's just a gimmick
                        listOfBoards.toArray(), // the values which can be selected from
                        "" // the initially selected value
                );
                if (chosenBoard.equals("New Board...")) chosenBoard = JOptionPane.showInputDialog("Name for new board:",
                        "Board 1");

                break;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        JFrame board = new JFrame("Board");
        board.setSize(640, 480);
        board.setLocationRelativeTo(null);
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //board.add(new BoardClient(socket, chosenBoard));
        board.add(new BoardView(new BoardModel(socket, chosenBoard)));

        JFrame tools = new JFrame("Instruments");
        tools.setSize(150, 400);
        tools.setLayout(new GridLayout(5, 1));

        JButton colors = new JButton("Pick Color...");
        colors.addActionListener(e -> BoardView.current = JColorChooser.showDialog(null, "Choose a Color", BoardView.current));
        colors.setSize(150, 80);
        tools.add(colors);

        JButton stroke = new JButton("Stroke Width");
        stroke.addActionListener(e -> BoardView.stroke = (byte) Integer.parseInt(JOptionPane.showInputDialog("How would you like your stroke thicc?", BoardView.stroke)));
        stroke.setSize(150, 80);
        tools.add(stroke);

        JToggleButton draw = new JToggleButton("Draw");
        draw.setSize(150, 80);
        tools.add(draw);
        draw.setSelected(true);

        JToggleButton erase = new JToggleButton("Erase");
        erase.setSize(150, 80);
        tools.add(erase);

        JToggleButton chooseBG = new JToggleButton("Set background");
        chooseBG.addActionListener(e -> BoardModel.updateBG(JColorChooser.showDialog(null, "Choose a Background Color", BoardModel.bg)));
        chooseBG.setSize(150, 80);
        tools.add(chooseBG);

        ActionListener actionListener = actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

            switch (abstractButton.getText()) {
                case "Erase":
                    draw.setSelected(false);
                    erase.setSelected(true);
                    BoardView.tool = "Erase";
                    break;

                case "Draw":
                    draw.setSelected(true);
                    erase.setSelected(false);
                    BoardView.tool = "Draw";
                    break;
            }
            System.out.println("Changed tool to " + BoardView.tool);
        };
        draw.addActionListener(actionListener);
        erase.addActionListener(actionListener);

        tools.setLocationRelativeTo(board);
        Point toolsLocation = board.getLocation();
        toolsLocation.x = toolsLocation.x + 650;
        tools.setLocation(toolsLocation);
        tools.setVisible(true);
        board.setVisible(true);

        board.setIconImage(img.getImage());
    }

}
