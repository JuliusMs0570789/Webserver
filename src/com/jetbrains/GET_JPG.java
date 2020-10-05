package com.jetbrains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class GET_JPG {
    static public void main(String[] args) {
        // new HTTP_GET(args[0], 80).getData(args[1]);
        new HTTP_GET("127.0.0.1", 80).getData("orchid.jpg");
    }

    private String host;
    private int port;

    public GET_JPG(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void getData(String ressource) {
        try {
            Socket s = new Socket(host, port);
            PrintWriter s_out = null;
            BufferedReader s_in = null;

            try {
                //Ermittle Adresse aus Hostname und Port
                //InetSocketAddress addr = new InetSocketAddress(host, port);
                //System.out.println("[Connecting to:" + addr + "]");
                //über den Soket öffne Verbindung zu Host und Port
                //s.connect(addr);
                System.out.println("[Connected]");

                //Writer and Reader for Socket
                s_out = new PrintWriter(
                        s.getOutputStream(), true);
                s_in = new BufferedReader(
                        new InputStreamReader(
                                s.getInputStream()));
            } catch (UnknownHostException e) {
                System.err.println("[Host not found: " + host + "]");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Build request Line
            String request_Line = "GET " + ressource + " HTTP/1.0\r\n";
            // Build header_Lines
            String host_header = "Host: " + host + "\r\n";
            //Log request
            System.out.println("[Request:]");

            s_out.println(request_Line);

            while (true) {
                try {
                    String input = s_in.readLine();
                    if (input != null) {
                        System.out.println(input);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

