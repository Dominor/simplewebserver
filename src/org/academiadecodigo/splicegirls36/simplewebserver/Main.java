package org.academiadecodigo.splicegirls36.simplewebserver;

public class Main {

    public static void main(String[] args) {

        SimpleWebServer webServer = null;

        try {
            // try to create an instance of the ChatServer at port specified at args[0]
            if (args.length == 1) {
                webServer = new SimpleWebServer(Integer.parseInt(args[0]));
                webServer.start();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number " + args[0]);
        }
    }
}
