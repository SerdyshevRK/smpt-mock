package me.home.server;

public enum CommandResponses {
    EHLO("550 not supported\r\n"),
    HELO("250 smtp.mock.com\r\n"),
    MAIL("250 FROM info saved\r\n"),
    RCPT("250 TO info saved\n"),
    DATA("354 enter email\r\n"),
    QUIT("221 closing connection\r\n"),
    ACCEPT_MESSAGE("250 message accepted\r\n");

    private final String message;

    public String getMessage() {
        return this.message;
    }

    CommandResponses(String message) {
        this.message = message;
    }
}
