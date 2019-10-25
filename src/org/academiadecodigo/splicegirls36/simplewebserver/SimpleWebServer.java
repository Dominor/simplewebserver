package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleWebServer {

    public static String CHARSET = "UTF8";
    public static int HEADERS_LENGTH = 6;

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private BufferedReader inputBufferedReader;
    private DataOutputStream outputStream;
    // private PrintWriter outputStream;
    private byte[] content;

    public SimpleWebServer(int port) {

        try {

            //bind the socket to specified port
            System.out.println("INFO: Binding to port " + port);
            serverSocket = new ServerSocket(port);

            System.out.println("INFO: Server started: " + serverSocket);

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            close();
        }
    }

    public void start() {

        String line = null;
        String[] requestLine = null;
        String verb = null;
        String resource = null;
        String version = null;
        String response = null;
        byte[] responseInBytes = null;

        try {

            // block waiting for a client to connect
            System.out.println("INFO: Waiting for a client connection.");
            clientSocket = serverSocket.accept();

            // handle client connection
            System.out.println("INFO: Client accepted: " + clientSocket);
            setupStreams();

            while (serverSocket.isBound()) {

                // read line from socket input reader
                line = inputBufferedReader.readLine();
                if (line != null) {
                    requestLine = line.split(" ");

                    // Process Request from the client and return appropriate headers and payload

                    if (requestLine.length == 3) {
                        verb = requestLine[0];
                        resource = requestLine[1];
                        version = requestLine[2];

                        // Remove first "/" from resource path so it doesn't error out
                        resource = resource.substring(1);

                        if (verb.equals("GET")) {
                            content = readResource(resource);
                            response = buildResponseHeaders(resource);

                            responseInBytes = response.getBytes(CHARSET);
                            outputStream.write(responseInBytes);
                            System.out.println("INFO: Uploading resource: " + resource);
                            outputStream.write(content, 0, content.length);
                            System.out.println("INFO: " + (outputStream.size() - responseInBytes.length) + " bytes written.");
                            // outputStream.write(content);
                            // outputStream.print(response);
                            outputStream.flush();

                        }
                    }

                } else {
                    System.out.println("INFO: Client closed, exiting");
                    break;
                }

                System.out.println("INFO: Processed request ");

            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        finally {
            close();
        }
    }

    private String buildResponseHeaders (String resource) throws IOException {

        StringBuilder headers = new StringBuilder();
        //String content = "<p> Hello World </p>";

        //content = Files.readAllBytes(Paths.get(resource));

        headers.append("HTTP/1.1 200 OK \r\n");
        // headers.append("Content-Type: text/html;charset=UTF-8 \r\n");
        headers.append("Content-Type: image/png \r\n");
        headers.append("Content-Length: " + content.length + "\r\n");
        headers.append("\r\n");
        //headers.append(content);

        return headers.toString();
    }

    private byte[] readResource(String path) {

        File file = new File(path);
        BufferedInputStream resourceInput;
        byte[] fileToBytes = new byte[(int) file.length()];      // Very dangerous, may result in program breakage if a file too big is specified in the resource url path
        int bytesRead = 0;

        if (!(file.exists() && file.isFile() && file.canRead())) {
            System.err.println("ERROR: Resource specified by url path provided in the request not able to be read.");
        }

        try {
            resourceInput = new BufferedInputStream(new FileInputStream(path));
            bytesRead = resourceInput.read(fileToBytes, 0, fileToBytes.length);
            System.out.println("INFO: " + bytesRead + " bytes read");

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return fileToBytes;
    }

    /**
     * Instantiate a buffered reader from the input stream of client socket
     */

    private void setupStreams() {

        try {
            inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), CHARSET));
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            // outputStream = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

    }

    /**
     * Closes the client socket and the buffered input reader
     */

    private void close () {

        try {

            if (clientSocket != null) {
                System.out.println("INFO: Closing client connection");
                clientSocket.close();
            }

            if (serverSocket != null) {
                System.out.println("INFO: Closing server socket");
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("ERROR: Error closing connection " + e.getMessage());
        }
    }
}
