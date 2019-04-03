package jonathan.ftp.sh.pain.model;

import jonathan.ftp.sh.pain.Line;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class BoardModel {
    public static Color bg = Color.WHITE;
    private static Color updatedBG;
    final private ArrayList<Line> lines;
    private Socket clientSocket;

    public BoardModel(Socket socket, String boardName) {

        clientSocket = socket;

        ArrayList<Line> tmp = null;
        try {
            tmp = handshake(clientSocket, boardName);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        lines = tmp;

        //getting updates
        Thread getUpdates = new Thread(() -> {
            ObjectInputStream ois;

            while (true) {
                try {
                    ois = new ObjectInputStream(clientSocket.getInputStream());
                    Object received = ois.readObject();
                    if (received instanceof Line) {
                        assert lines != null;
                        synchronized (lines) {
                            lines.add((Line) received);
                        }
                        //System.out.println("Got new path from server");
                    } else if (received instanceof Point) {
                        assert lines != null;
                        synchronized (lines) {
                            lines.removeIf(line -> line.getPath().contains((Point) received));
                        }
                        //System.out.println("Got new kebab removal point from server");
                    } else if (received instanceof Color) {
                        updateBG((Color) received);
                    }
                    //update view somehow
                } catch (EOFException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        //getUpdates.setDaemon(true);
        getUpdates.start();

        final Timer timer = new Timer(1000, e -> pushBG());//in case bg changed
        timer.start();
    }

    public static void updateBG(Color x) {
        updatedBG = x;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void addLine(Line x) {
        try {
            ObjectOutputStream ois = new ObjectOutputStream(clientSocket.getOutputStream());
            ois.writeObject(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeLine(Point x) {
        synchronized (lines) {
            lines.removeIf(line -> line.getPath().contains(x));
        }
        try {
            ObjectOutputStream ois = new ObjectOutputStream(clientSocket.getOutputStream());
            ois.writeObject(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushBG() {
        if (updatedBG != bg) {
            bg = updatedBG;
            try {
                ObjectOutputStream ois = new ObjectOutputStream(clientSocket.getOutputStream());
                ois.writeObject(updatedBG);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private ArrayList<Line> handshake(Socket clientSocket, String boardName) throws IOException, ClassNotFoundException {
        //sending board name to server
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        oos.writeUTF(boardName);
        oos.flush();
        System.out.println("Asked for " + boardName + " from server");

        //restoring board from server
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("Got " + boardName + " from server");
        return (ArrayList<Line>) ois.readObject();
    }
}
