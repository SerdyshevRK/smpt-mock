package me.home.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmtpMock {
    private static final Logger log = LoggerFactory.getLogger(SmtpMock.class.getName());
    private static final int DEFAULT_PORT = 8025;
    private final int port;

    public SmtpMock(int port) {
        this.port = port;
    }

    public void start() {
        log.info("Starting mock SMTP me.home.server");
        ServerSocket serverSocket = null;
        Socket socket;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            serverSocket = new ServerSocket(port);
            log.info("Mock SMTP me.home.server listening on port: {}", port);
            while (true) {
                socket = serverSocket.accept();
                executorService.execute(new Worker(socket));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    log.info("Closing me.home.server socket");
                    serverSocket.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (!executorService.isShutdown())
                executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0 && args[0].equals("-p")) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port parameter: {}. Setting default port: {}", args[1], DEFAULT_PORT);
            }
        }
        SmtpMock server = new SmtpMock(port);
        server.start();
    }
}
