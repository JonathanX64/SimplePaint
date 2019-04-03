package jonathan.ftp.sh.pain.server;

import jonathan.ftp.sh.pain.Line;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class Storage {
    private HashMap<String, ArrayList<Line>> storage = new HashMap<>();

    Storage(String path) {

        File f = new File(path);
        if (!f.exists()) {
            try {
                f.createNewFile();
                this.create("Default", new ArrayList<>());
                updateFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
                storage = (HashMap<String, ArrayList<Line>>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void updateBoard(String boardName, Line anotherPath) {
        storage.get(boardName).add(anotherPath);
    }

    void removeFromBoard(String boardName, Point pathToDelete) {
        storage.get(boardName).removeIf(line -> line.getPath().contains(pathToDelete));
    }

    private void create(String name, ArrayList<Line> board) {
        storage.put(name, board);
    }

    LinkedList<String> list() {
        LinkedList<String> res = new LinkedList<>();

        for (Map.Entry a : storage.entrySet()) {
            res.add((String) a.getKey());
        }

        return res;
    }

    ArrayList<Line> retrieve(String boardName) {
        if (storage.containsKey(boardName)) {
            return storage.get(boardName);
        } else {
            create(boardName, new ArrayList<>());
            return storage.get(boardName);
        }
    }

    void updateFile() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream("data.ser"));
            out.writeObject(storage);
            out.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            System.out.println("no success :/");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}