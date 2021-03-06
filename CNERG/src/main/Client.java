package main;

import javax.xml.ws.spi.http.HttpHandler;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class Client {
    static String fName="output.seg0-L0.svc";

    public static void main(String args[]) throws IOException {
        String Dest=args[0];
        String ip = args[1];
        String filename="output.seg0-L0.svc";
        String fileURL = "http://" + ip + ":8081/get?name=" + filename;
        String saveDir = Dest + "/" + filename;
        downloadFile(fileURL,saveDir);
    }
    public static String downloadFile(String gotUrl,String saveDir) throws IOException
    {
        String timeStamp="";
        URL url = new URL(gotUrl);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        int responseCode = httpCon.getResponseCode();
        ////////////////////////////////////////////////

        //String resp=getResp(ip);
        String resp="Yes";
        System.out.println("I am getting : "+resp);
        if(resp=="Yes") {


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
        return timeStamp;

    }




}
