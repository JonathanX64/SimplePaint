package jonathan.ftp.sh.pain.server;

import jonathan.ftp.sh.pain.Line;

import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

//TODO packages and jar
//TODO Ant/Maven
//TODO tests

//TODO readme.md
//TODO keep connection alive & try to reconnect
//TODO update every few seconds, but only if smthn happened
//TODO properly store bg on server
//TODO better line removal algorithm with approximation
//TODO db instead of ser

//promised but failed:
//TODO quick erase?

public class BoardServer {
    private ArrayList<ConnectionToClient> clientList;// = new ArrayList<>();
    private ServerSocket serverSocket;
    private Storage boards;
    private Log log = new Log();

    private BoardServer(int port, Storage boards) {
        this.boards = boards;
        clientList = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread clientConnections = new Thread(() -> {
            while (true) {
                try {
                    clientList.add(new ConnectionToClient(serverSocket.accept()));
                    log.write("Connection with client established.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread admin = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String input;
            while (true) {
                input = scanner.nextLine();
                switch (input) {
                    case "list":
                        for (String name : boards.list()) {
                            int count = 0;
                            for (ConnectionToClient a : clientList) {
                                if (Objects.equals(a.nameOfServedBoard, name)) count++;
                            }
                            System.out.println("Board " + name + ": " + count + " clients");
                        }
                        break;
                    case "erase":
                        System.out.println("Which board do you want to erase?");
                        input = scanner.nextLine();
                        boards.retrieve(input).removeAll(boards.retrieve(input));//erase on clients
                        break;
                    case "create":
                        System.out.println("How do we name it m8?");
                        input = scanner.nextLine();
                        boards.retrieve(input);
                        break;
                    case "exit":
                        System.exit(0);
                    default:
                        break;
                }
            }
        });

        clientConnections.start();
        admin.start();

    }

    public static void main(String[] args) {
        Storage data = new Storage("data.ser");
        BoardServer a = new BoardServer(50432, data);
    }

    private void sendToAll(String boardName, Object message) {
        for (ConnectionToClient client : clientList)
            if (client.nameOfServedBoard.equals(boardName)) {
                client.write(message);
            }
        //send back only to users on the same board
    }

    private class ConnectionToClient {
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket socket;
        private String nameOfServedBoard;

        ConnectionToClient(Socket socket) {
            this.socket = socket;

            //handshake
            try {
                //pushing list of boards
                write(boards.list());
                log.write("Client " + socket.getInetAddress() + " connected, sent them list of boards");

                //waiting for board selection
                in = new ObjectInputStream(socket.getInputStream());
                nameOfServedBoard = in.readUTF();
                log.write("Client chose " + nameOfServedBoard);

                //feeding selected board
                write(boards.retrieve(nameOfServedBoard));
                log.write("Pushed " + nameOfServedBoard + " to client" + socket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread read = new Thread(() -> {
                Object received;

                while (true) {
                    try {
                        in = new ObjectInputStream(socket.getInputStream());
                        received = in.readObject();

                        if (received instanceof Line) {
                            boards.updateBoard(nameOfServedBoard, (Line) received);
                            log.write("Received path from client" + socket.getInetAddress());
                        } else if (received instanceof Point) {
                            boards.removeFromBoard(nameOfServedBoard, (Point) received);
                            log.write("Received kebab removal coordinates from client" + socket.getInetAddress());
                        }
                        sendToAll(nameOfServedBoard, received);
                        log.write("Sent data back to all clients");
                        boards.updateFile();
                    } catch (EOFException |SocketException e) {
                        clientList.remove(this);
                        break;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

            read.setDaemon(true); // terminate when main ends
            read.start();
        }

        void write(Object obj) {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(obj);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}