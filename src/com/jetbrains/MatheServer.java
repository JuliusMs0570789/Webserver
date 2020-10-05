package com.jetbrains;

import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

public class MatheServer {
    public static void main(String args[])
            throws IOException {
        //if (argv.length > 0) {
        //ServerSocket ss = new ServerSocket (Integer.parseInt (argv [0]));
        ServerSocket ss = new ServerSocket(1025);


        while (true) {
            // bearbeite die Verbindung asynchron
            new MatheServerConnection(ss.accept());
        }
        //}
    }
}

class MatheServerConnection
        extends Thread {
    Socket sock;

    MatheServerConnection(Socket s) {
        sock = s;
        setPriority(NORM_PRIORITY - 1);
        start(); // starte run() als neuen Thread
    }

    public void run() {

        try {
            // hole die Input- und Output-Streams der Verbindung
            InputStream httpin = sock.getInputStream();
            OutputStream httpout = sock.getOutputStream();
            String parameter = "";

            // ein Text-Leser für den Request zum bequemeren Lesen
            BufferedReader httpreader =
                    new BufferedReader(new InputStreamReader(httpin));
            // ein Text-Schreiber für die Antwort zum Schreiben der Header
            PrintWriter httpwriter = new PrintWriter(httpout);

            try {
                // lies die erste Zeile des Requests ("Request-Line")
                // Format: "GET /xy.html HTTP/1.0"
                String request = httpreader.readLine();
                out.println(request);

                // zerlege Request-Zeile in Worte
                StringTokenizer tokens = new StringTokenizer(request);
                String method = tokens.nextToken();

                if (method.equals("GET") || method.equals("POST")) {
                    // hole Request-URI
                    String URI = tokens.nextToken();    // "/rechne"

                    String rechnen = "";
                    int x = 0;
                    int y = 0;
                    int op;
                    int ergebnis = 0;
                    if (URI.equals("Not Allowed")) {
                        httpwriter.println("HTTP/1.0 403 Not Allowed");
                        httpwriter.flush();
                    } else if (URI.contains("rechne")) {
                        String[] array = URI.split("/");
                        //entferne "/" aus Anfrage
                        for (int i = 0; i <= array.length - 1; i++) {
                            if (!array[i].equals("")) {
                                rechnen += array[i];
                            }
                        }
                        //zerlege in x, y und op
                        String[] a = rechnen.split("&");
                        for (int i = 0; i <= a.length - 1; i++) {
                            if (a[i].contains("x")) {
                                String[] expression = a[i].split("=");
                                for (int j = 0; j < expression.length; j++) {
                                    if (!expression[j].contains("x")) {
                                        x = Integer.parseInt(expression[j]);
                                    }
                                }
                            } else if (a[i].contains("y")) {
                                String[] expression = a[i].split("=");
                                for (int j = 0; j < expression.length; j++) {
                                    if (!expression[j].contains("y")) {
                                        y = Integer.parseInt(expression[j]);
                                    }
                                }
                            } else if (a[i].contains("op")) {
                                if (a[i].contains("+")) {
                                    ergebnis = x + y;
                                } else if (a[i].contains("-")) {
                                    ergebnis = x - y;
                                } else if (a[i].contains("*")) {
                                    ergebnis = x * y;
                                } else if (a[i].contains("/")) {
                                    ergebnis = x / y;
                                }
                            }
                        }
                        String s = "Ergebnis: " + Integer.toString(ergebnis);

                        httpwriter.println(s);
                        httpwriter.flush();
                    } else {
                        httpwriter.println("HTTP/1.0 404");
                        httpwriter.flush();
                    }
                } else {
                    httpwriter.println("HTTP/1.0 405 Method Not Allowed");
                    out.println("HTTP Method abgelehnt: " + method);
                    httpwriter.flush();
                }
            } catch (NoSuchElementException e) {
                // keine Tokens (z.B. URI) im Request
                httpwriter.println("HTTP/1.0 400 Bad Request");
                out.println("Bad Request");
                httpwriter.flush();
            } catch (IOException e) {
                httpwriter.println("HTTP/1.0 500 Internal Server Error");
                out.println("I/O error " + e);
                httpwriter.flush();
            }
            httpwriter.close();
            sock.close();
        } catch (IOException e) {
            // falls hier schon eine Exception auftrat, besteht keine
            // Möglichkeit, eine Antwort zu übergeben
            out.println("I/O error am Anfang " + e);
        }
    }
}
