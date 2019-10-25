package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleWebServer {

    public static String CHARSET = "UTF8";
    public static int HEADERS_LENGTH = 6;

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private BufferedReader inputBufferedReader;
    private DataOutputStream outputStream;
    // private PrintWriter outputStream;
    private byte[] content;
    private File resource = null;

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
        StringBuilder requestBuilder = new StringBuilder();

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
                if (!line.isEmpty()) {

                    requestBuilder = requestBuilder.append(line);


                } else {
                    System.out.println("INFO: Client closed, exiting");
                    break;
                }

                parseRequestHeaders(requestBuilder.toString());
                System.out.println("INFO: Processed request ");

            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        finally {
            close();
        }
    }

    private String buildResponseHeaders (StatusCode statusCode) throws IOException {

        StringBuilder headers = new StringBuilder();
        String mimeType = null;

        switch(statusCode) {
            case OK:

                headers.append("HTTP/1.1 " + statusCode.getCode() + statusCode + "\r\n");
                headers.append("Content-Type: " + getFileMimeType() + ((getFileType().equals("text")) ? ";charset=UTF-8 \r\n" : ""));
                headers.append("Content-Length: " + content.length + "\r\n");
                headers.append("\r\n");
                break;
            case NOT_FOUND:
                headers.append("HTTP/1.1 " + statusCode.getCode() + statusCode + "\r\n");
                break;
            default:
                break;
        }

        return headers.toString();
    }

    private String getFileMimeType() {

        String [] resource_split = resource.getName().split("/");
        String fileName = resource_split[resource_split.length - 1];
        String extension = fileName.split("\\.")[1];

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpg";
            case "png":
                return "image/png";
            case "txt":
                return "text/plain";
            case "htm":
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }

    /**
     *
     * @return The file type (text or binary) of the file referenced by the property resource of the type File.
     */
    private String getFileType() {

        switch (getFileMimeType().split("/")[0]) {

            case "text":
                return "text";
            case "image" :
                return "binary";
            default:
                return "binary";
        }
    }

    private byte[] readResource(String path) {

        resource = new File(path);
        BufferedInputStream resourceInput;
        byte[] fileToBytes = new byte[(int) resource.length()];      // Very dangerous, may result in program breakage if a file too big is specified in the resource url path
        int bytesRead = 0;

        if (!(resource.exists() && resource.isFile() && resource.canRead())) {
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

    private void parseRequestHeaders (String request) throws IOException {

        String[] requestLines = request.split("(\r|\n)");
        String[] requestLine = requestLines[0].split(" ");
        String httpVerb;
        String resource;
        String httpVersion;
        String response = null;
        byte[] responseInBytes = null;

        // Currently ignoring all other headers other than the initial request line
        // Only responding to a GET Request

        if (requestLine.length == 3) {
            httpVerb = requestLine[0];
            resource = requestLine[1];
            httpVersion = requestLine[2];

            // Remove first "/" from resource path so it doesn't error out
            resource = resource.substring(1);

            switch (httpVerb) {

                case "GET":
                    content = readResource(resource);
                    resource = buildResponseHeaders(StatusCode.OK);

                    responseInBytes = resource.getBytes(CHARSET);
                    outputStream.write(responseInBytes);
                    System.out.println("INFO: Uploading resource: " + resource);
                    outputStream.write(content, 0, content.length);
                    System.out.println("INFO: " + (outputStream.size() - responseInBytes.length) + " bytes written.");
                    // outputStream.write(content);
                    // outputStream.print(response);
                    outputStream.flush();
                    break;
                default:

            }
        }

        /** for (int i = 0; i < requestLines.length; i++) {

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
             } */
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
