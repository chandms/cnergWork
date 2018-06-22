package main;

import javax.xml.ws.spi.http.HttpHandler;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class CreditClient {

    public static String getResp(String file,String ip)
    {
        String fName=file;

        URL url = null;
        try {
            url = new URL("http://" + ip + ":8081/allow");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setDoOutput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(
                    connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.write(fName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String decodedString = null;
        String ff = "";
        try {
            while ((decodedString = in.readLine()) != null) {
                ff = ff + decodedString;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("I got ff "+ff);
        return ff;
    }
    public static ArrayList<String> downloadFile(String gotUrl,String saveDir,String ip,String fName) throws IOException
    {
        String timeStamp="";
        URL url = new URL(gotUrl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        int responseCode = httpCon.getResponseCode();
        ////////////////////////////////////////////////

        String resp=getResp(fName,ip);

        System.out.println("I am getting : "+resp);
        if(resp.equals("Yes")) {


            /////////////////////////////////////////////
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpCon.getHeaderField("Content-Disposition");
                String contentType = httpCon.getContentType();
                int contentLength = httpCon.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = gotUrl.substring(gotUrl.lastIndexOf("/") + 1,
                            gotUrl.length());
                }

                System.out.println("Content-Type = " + contentType);
                System.out.println("Content-Disposition = " + disposition);
                System.out.println("Content-Length = " + contentLength);
                System.out.println("fileName = " + fileName);

                // opens input stream from the HTTP connection
                InputStream inputStream = httpCon.getInputStream();
                //String saveFilePath = saveDir + File.separator + "CMCV.pdf";

                // opens an output stream to save into file
                FileOutputStream outputStream = new FileOutputStream(saveDir);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                timeStamp = new String(String.valueOf(System.currentTimeMillis()));
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                System.out.println("File downloaded");
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpCon.disconnect();

        }
        else
            System.out.println("Server denied...............");
        ArrayList<String> srr = new ArrayList<String>();
        srr.add(timeStamp);
        srr.add(resp);
        return srr;

    }




}

