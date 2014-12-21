package com.gmail.dajinchu.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * Created by Da-Jin on 12/20/2014.
 */
public class EchoClient {
    public static void main(String[] args) throws IOException {
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        ServerSocket serverSocket = null;
        String localhost = "127.0.0.1";
        try {
            // Create a listener socket for the server
            serverSocket = new ServerSocket(4000);
            echoSocket = new Socket(localhost, 4000);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            // Accept ANY activity going to port 4000
            Socket sock = serverSocket.accept();
            // Take the InputStream from the "sock" socket
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + localhost + ".");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to: " + localhost + ".");
            e.printStackTrace();
            System.exit(1);
        }
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            System.out.println("echo: " + in.readLine());
        }
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
        serverSocket.close();
    }
}