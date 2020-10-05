package com.jetbrains;

import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

public class HTTPServer {
    public static void main(String args[])
            throws IOException {
        //if (argv.length > 0) {
        //ServerSocket ss = new ServerSocket (Integer.parseInt (argv [0]));
        ServerSocket ss = new ServerSocket(80);


        while (true) {
            // bearbeite die Verbindung asynchron
            new HTTPConnection(ss.accept());
        }
        //}
    }
}

class HTTPConnection
        extends Thread {
    Socket sock;

    HTTPConnection(Socket s) {
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
                out.println("HTTP: " + request);

                // zerlege Request-Zeile in Worte
                StringTokenizer tokens = new StringTokenizer(request);
                String method = tokens.nextToken();    // z.B. "GET", "/index.html", "HTTP/1.0"

                // bearbeite akzeptierte Request "Methods"
                if (method.equals("GET") || method.equals("POST")) {
                    // hole Request-URI
                    String URI = tokens.nextToken();    // "/index.html"
                    String URIparams = "";
                    //String HTTP_version = tokens.nextToken();    // "HTTP/1.0"

                   /*
                    //... URL verarbeiten
                    // "/index.html"
                    // "/bla/arg.html"
                    // "/img.jpg"
                    // "/uhrzeit"
                    // "/uhrzeit?format=am/pm"
                    // if(URI == "index.html"){

                    // }
                    // else if(URI == "/img.jpg"){

                    // }

                    // Header-Zeilen lesen
                    int tocolon;
                    do {
                        request = httpreader.readLine();
                        tocolon = request.indexOf(':');
                        if (tocolon > 0) {

                            //    ... Header-Zeile verarbeiten	// <- optional

                        }
                    } while (tocolon > 0);
                    // lies Body, falls vorhanden;	// <- optional
                    // Indizien sind Headerzeilen Content-Type oder Transfer-Encoding
                    // if (has_content || has_body) {
                    // lies Body (kann z.B. bei POST Parameter enthalten)
                    ///...
                    // }
                    */

                    // bearbeite den Request

                    // unterscheide nach einem Server-spezifischen Kriterium
                    // verschiedene Ressourcentypen, hier:
                    //	– Dateien (z.B. HTML-Seiten), wenn URI "." enthält
                    //	– dynamische Server-Funktionen
                    //Über Browser testen mit http://localhost/index.html
                    // bestimme Dateityp anhand des Suffix des Dateinamens
                    if (URI.contains("/")) {
                        String[] array = URI.split("/");
                        for (int i = 0; i <= array.length - 1; i++) {
                            if (array[i].equals("..")) {
                                URI = "Not Allowed";
                            } else if (array[i].equals("uhrzeit?format=12h") && array.length == 2) {
                                parameter = "12h";
                            }
                        }
                    }
                    if (URI.contains("action=uhrzeit")) {
                        String[] uhrzeit = URI.split("=");
                        String zeit = uhrzeit[uhrzeit.length - 1];
                        String hoursMinutesSekonds = "";
                        if (zeit.contains("%20")) {
                            String[] newTime = zeit.split("%20");
                            for (String s : newTime) {
                                hoursMinutesSekonds += s;
                                hoursMinutesSekonds += " ";
                            }
                        }
                        if (zeit.contains("%3A")) {
                            String[] newTime = zeit.split("%3A");
                            for (int i = 0; i < newTime.length; i++) {
                                if (i < newTime.length - 1) {
                                    hoursMinutesSekonds += newTime[i];
                                    hoursMinutesSekonds += ":";
                                } else {
                                    hoursMinutesSekonds += newTime[i];
                                }
                            }
                        }
                        parameter = hoursMinutesSekonds;
                    }

                    if (URI.equals("Not Allowed")) {
                        httpwriter.println("HTTP/1.0 403 Not Allowed");
                        httpwriter.flush();
                    } else if (URI.contains(".html") || URI.contains(".gif") || URI.contains(".jpg")) {        // Ressourcen-Typ wird angefragt

                        byte[] data = readFile(httpin, httpwriter, URI);

                        // liefere Response zurück
                        httpwriter.println("HTTP/1.0 200 OK");

                        // setze Content-Type entsprechend des Dateityps
                        if (URI.contains(".html")) {
                            httpwriter.println("Content-Type: text/html");
                        } else if (URI.contains(".gif")) {
                            httpwriter.println("Content-Type: image/gif");
                        } else if (URI.contains(".jpg")) {
                            httpwriter.println("Content-Type: image/jpg");
                        }

                        // Ende der Header-Zeilen
                        httpwriter.println("");

                        httpwriter.flush();

                        httpout.write(data);
                        httpout.flush();
                        httpout.close();
                    } else if (URI.contains("uhrzeit")) {
                        Date date = new Date();
                        String d = date.toString();

                        if (parameter == "12h") {
                            String pattern = "hh:mm:ss a";
                            DateFormat dateFormat = new SimpleDateFormat(pattern);
                            Date today = new Date();
                            d = dateFormat.format(today);
                        } else if (parameter != "" && parameter != "24h") {
                            d = parameter;
                        }

                        httpwriter.println("HTTP/1.0 200 OK");
                        httpwriter.println("Content-Type: text/plain");
                        httpwriter.println("");

                        httpwriter.println(d);
                        httpwriter.flush();
                    } else if (URI.contains("rechne")) {
                        String op = "";
                        String x = "";
                        String y = "";
                        MatheServer neuerServer = new MatheServer();
                        String response = "Berechnung nicht möglich";
                        if (!URI.contains("=")) {
                            String[] rechnen = URI.split("rechne?");
                            String r = rechnen[1];

                            for (int i = 1; i < r.length(); i++) {
                                if (r.charAt(i) == '+' || r.charAt(i) == '-' || r.charAt(i) == '*' || r.charAt(i) == '/') {
                                    op = String.valueOf(r.charAt(i));
                                } else if (x.equals("")) {
                                    x = String.valueOf(r.charAt(i));
                                } else {
                                    y = String.valueOf(r.charAt(i));
                                }
                            }
                            URI = "/rechne?x=" + x + "&y=" + y + "&op=" + op;
                        }

                        response = new Rechenanfrage("127.0.0.1", 1025).getData(URI);

                        httpwriter.println("<!DOCTYPE html>");
                        httpwriter.println("<html>");
                        httpwriter.println("<head><title></title></head>");
                        httpwriter.println("<body>");
                        httpwriter.println("<b>");
                        httpwriter.println(response);
                        httpwriter.println("</b>");
                        httpwriter.println("</body>");
                        httpwriter.println("</html>");
                        httpwriter.flush();
                    } else {
                        httpwriter.println("HTTP/1.0 404");
                        httpwriter.flush();
                    }
                }

                //else if (...
                // optional: eigene Methoden oder Request-Formate
                // (eigenes Anwendungsprotokoll statt HTTP)
                //       )
                // {  }

                else {
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

    public byte[] readFile(InputStream httpin, PrintWriter httpwriter, String URI){
        // schreibe Dateiinhalt in Response-Body
        // (binär, daher nicht über den Writer,
        // sondern den darunterliegenden OutputStream,
        // deshalb vorher flush erforderlich)
        String s = "/Users/CeSar/Desktop/Uni/2.Semester/Netzwerke/" + URI;
        File file = new File(s);
        byte[] data = null;
        try {
            httpin = new FileInputStream(file);
            data = new byte[(int) file.length()];
            httpin.read(data);
        } catch (FileNotFoundException e) {
            httpwriter.println("HTTP/1.0 404 Not Found");
            httpwriter.flush();
        } catch (IOException e) {
            httpwriter.println("HTTP/1.0 500 Internal Server Error");
            httpwriter.flush();
        } finally {
            try {
                if (httpin != null) httpin.close();
            } catch (IOException e) {
                httpwriter.println("HTTP/1.0 500 Internal Server Error");
                httpwriter.flush();
            }
        }
        return data;
    }
}