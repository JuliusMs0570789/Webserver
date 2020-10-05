package com.jetbrains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RechenanfrageAnHTTP {

    static public void main(String[] args) {
        new Rechenanfrage("127.0.0.1", 80).getData("/rechne?3+6");
    }

    private String host;
    private int port;

    public RechenanfrageAnHTTP(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void getData(String ressource) {
        String response = "";
        try {
            Socket s = new Socket(host, port);
            PrintWriter s_out = null;
            BufferedReader s_in = null;

            try {
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
            String request_Line = "GET " + ressource;
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
