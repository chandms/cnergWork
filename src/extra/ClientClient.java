package extra;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientClient {

    String downloadFolder;
    String copyFolder;
    ArrayList<String> fileList;


    public ClientClient(String x,String y,ArrayList<String>myList)
    {
        downloadFolder=x;
        copyFolder=y;
        fileList=myList;
    }


    public void startrecieve() throws IOException {
        int filesize=2022386;
        int bytesRead;
        int currentTot = 0;


        System.out.println(fileList);
        int ii=9000;
            for(int j=0;j<fileList.size();j++) {
                Socket socket = new Socket("127.0.0.1",ii);
                ii++;
                System.out.println("New file name sent");
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String fileName = fileList.get(j);
                out.writeUTF(fileName);
                System.out.println("Sent file name");
                byte[] bytearray = new byte[filesize];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream("C:/Users/Pupul/Desktop/" + copyFolder + "/" + fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bytesRead = is.read(bytearray, 0, bytearray.length);
                currentTot = bytesRead;

                do {
                    bytesRead =
                            is.read(bytearray, currentTot, (bytearray.length - currentTot));
                    if (bytesRead >= 0) currentTot += bytesRead;
                } while (bytesRead > -1);
                bos.write(bytearray, 0, currentTot);
                socket.close();
                System.out.println("I got one file......................");
                bos.flush();
                bos.close();
            }

    }
}
