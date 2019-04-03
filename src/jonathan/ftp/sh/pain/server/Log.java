package jonathan.ftp.sh.pain.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

class Log {
    private PrintWriter writer = null;

    Log() {
        try {
            writer = new PrintWriter("log.txt", StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write(String loggedData) {
        writer.println(java.time.LocalDateTime.now() + " " + loggedData);
        System.out.println(loggedData);
        writer.flush();
    }

}
