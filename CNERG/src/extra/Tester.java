package extra;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.net.*;

public class Tester {

        static String ipofServer;
        public Tester(String is)
        {
            ipofServer=is;
        }


        public static void test() throws Exception {

            /*if (args.length != 2) {
                System.err.println("Usage:  java Reverse "
                        + "http://<location of your servlet/script>"
                        + " string_to_reverse");
                System.exit(1);
            }*/
            InetAddress ina= InetAddress.getLocalHost();
            String sent=ina.getHostAddress().trim();
            String stringToReverse = URLEncoder.encode(sent, "UTF-8");

            URL url = new URL("http://"+ipofServer+":8081/ip");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
            out.write("ip =" + stringToReverse);

            out.close();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                System.out.println(decodedString);
            }
            in.close();
        }
    }
