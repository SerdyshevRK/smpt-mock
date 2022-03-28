package me.home.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Worker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Worker.class.getName());
    private final Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    private String sendResponse(CommandResponses response, OutputStreamWriter outputStream) throws IOException {
        outputStream.write(response.getMessage());
        outputStream.flush();
        return response.getMessage();
    }

    @Override
    public void run() {
        int read;
        byte[] buffer = new byte[1024];
        String input;
        String client = String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
        log.info("Receive connection from: {}", client);
        try (OutputStreamWriter outputStream = new OutputStreamWriter(socket.getOutputStream()); InputStream inputStream = socket.getInputStream()) {
            outputStream.write("220 smtp.mock.com SMTP\r\n");
            outputStream.flush();
            String command;
            String response = "";
            while (true) {
                read = inputStream.read(buffer);
                if (read > 0) {
                    input = new String(buffer);
                    command = input.substring(0, 4);
                    switch (command) {
                        case "EHLO":
                            response = sendResponse(CommandResponses.EHLO, outputStream);
                            break;
                        case "HELO":
                            response = sendResponse(CommandResponses.HELO, outputStream);
                            break;
                        case "MAIL":
                            response = sendResponse(CommandResponses.MAIL, outputStream);
                            break;
                        case "RCPT":
                            response = sendResponse(CommandResponses.RCPT, outputStream);
                            break;
                        case "DATA":
                            response = sendResponse(CommandResponses.DATA, outputStream);
                            break;
                    }
                    if ("QUIT".equals(command)) {
                        response = sendResponse(CommandResponses.QUIT, outputStream);
                        log.debug("({}) Receive command: {}", client, input);
                        log.debug("({}) Send response: {}", client, response);
                        break;
                    }
                    if (input.trim().endsWith("\r\n.")) {
                        response = sendResponse(CommandResponses.ACCEPT_MESSAGE, outputStream);
                        saveMessageToFile(client, input.trim());
                    }
                    log.debug("({}) Receive command: {}", client, input);
                    log.debug("({}) Send response: {}", client, response);
                } else {
                    log.warn("({}) Connection lost", client);
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            try {
                log.info("({}) Closing connection", client);
                socket.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void saveMessageToFile(String fileName, String message) {
        String filePath = fileName.replaceAll(":", "_") + ".txt";
        try {
            Files.write(Paths.get(filePath), message.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        log.info("Message saved in file: {}", filePath);
    }
}
