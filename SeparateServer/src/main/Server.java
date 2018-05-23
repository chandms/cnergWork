package main;

import sun.util.calendar.BaseCalendar;
import sun.util.calendar.LocalGregorianCalendar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class Server {

public static void main(String args[]) throws IOException {

    ServerSocket serverSocket = new ServerSocket(8081);
    System.out.println("Waiting for clients to connect");
   /* while(true)
    {
        Socket client = serverSocket.accept();
        /*InputStreamReader isr = new InputStreamReader(client.getInputStream());
        BufferedReader bufr = new BufferedReader(isr);
        String line=bufr.readLine();
        while(!line.isEmpty())
        {
            System.out.println(line);
            line=bufr.readLine();
        }
        Date today = new Date();
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n"+today;
        client.getOutputStream().write(httpResponse.getBytes("UTF-8"));
    }*/
    while (true) {
        try (Socket socket = serverSocket.accept()) {
            Date today = new Date();
            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
            socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        }
    }


}
}
