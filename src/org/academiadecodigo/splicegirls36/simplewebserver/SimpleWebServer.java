package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleWebServer {

    public static String CHARSET = "UTF8";

    public static final File NOT_FOUND_HTML_FILE = new File("www/404.html");
    public static final File NOT_IMPLEMENTED_FILE = new File("www/501.html");

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private BufferedReader inputBufferedReader;
    private DataOutputStream outputStream;

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

        String line;
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

    private String buildResponseHeaders (File resource, byte[] content, StatusCode statusCode) throws IOException {

        StringBuilder headers = new StringBuilder();
        String mimeType = null;

        switch(statusCode) {
            case OK:
            case NOT_FOUND:

                headers.append("HTTP/1.1 " + statusCode.getCode() + statusCode + "\r\n");
                headers.append("Content-Type: " + getFileMimeType(resource) + ((getFileType(resource).equals("text")) ? ";charset=UTF-8 \r\n" : ""));
                headers.append("Content-Length: " + content.length + "\r\n");
                headers.append("\r\n");
                break;
            default:
                headers.append("Content-Type: " + getFileMimeType(resource) + ((getFileType(resource).equals("text")) ? ";charset=UTF-8 \r\n" : ""));
                headers.append("Content-Length: " + content.length + "\r\n");
                headers.append("\r\n");
                break;
        }

        return headers.toString();
    }

    private String getFileMimeType(File resource) {

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
    private String getFileType(File resource) {

        switch (getFileMimeType(resource).split("/")[0]) {

            case "text":
                return "text";
            default:
                return "binary";
        }
    }

    private boolean isValidFile (File file) {

        return file.exists() && file.isFile() && file.canRead();
    }

    private byte[] readResource(File resource) {

        BufferedInputStream fileInput;
        byte[] fileToBytes = new byte[(int) resource.length()];      // Dangerous, may result in program breakage if a file too big is specified in the resource url path
        int bytesRead = 0;

        if (!isValidFile(resource)) {
            System.err.println("ERROR: Resource specified by url path provided in the request not able to be read.");
        }

        try {
            fileInput = new BufferedInputStream(new FileInputStream(resource.getPath()));
            bytesRead = fileInput.read(fileToBytes, 0, fileToBytes.length);
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

    private void sendResponse (File resource, StatusCode statusCode) throws IOException {

        byte[] content = readResource(resource);

        String responseHeaders = buildResponseHeaders(resource, content, statusCode);

        byte[] responseInBytes = responseHeaders.getBytes(CHARSET);

        outputStream.write(responseInBytes);
        System.out.println("INFO: Uploading resource: " + resource.getName());
        outputStream.write(content, 0, content.length);
        System.out.println("INFO: " + (outputStream.size() - responseInBytes.length) + " bytes written.");
        outputStream.flush();
    }

    private void parseRequestHeaders (String request) throws IOException {

        String[] requestLines = request.split("(\r|\n)");
        String[] requestLine = requestLines[0].split(" ");
        String httpVerb;
        String resourcePath;
        String httpVersion;
        String response = null;
        File resource = null;

        // Currently ignoring all other headers other than the initial request line
        // Only responding to a GET Request

        if (requestLine.length == 3) {
            httpVerb = requestLine[0];
            resourcePath = requestLine[1];
            httpVersion = requestLine[2];

            // Remove first "/" from resource path so it doesn't error out
            resourcePath = resourcePath.substring(1);
            resource = new File(resourcePath);

            switch (httpVerb) {

                case "GET":
                    if (!isValidFile(resource)) {
                        System.err.println("ERROR: Invalid file. Returning 404 page.");
                        sendResponse(NOT_FOUND_HTML_FILE, StatusCode.NOT_FOUND);
                    } else {
                        sendResponse(resource, StatusCode.OK);
                    }
                    break;
                default:
                    System.err.println("ERROR: HTTP Request not yet implemented!");
                    sendResponse(NOT_IMPLEMENTED_FILE, StatusCode.NOT_IMPLEMENTED);
            }
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
