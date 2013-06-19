import java.io.*;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: sk-saad
 * Date: 6/17/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientManager {
    private Socket clientSocket;

    ClientManager ( int port) {
        try {
            clientSocket = new Socket("localhost", port);
        }
        catch ( IOException ioEx ) {
            System.out.println("Client:Socket Creation Error !!");
        }
    }

    public String readSocket() {
        String message = "" ;
        try {
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            message = inFromServer.readLine();
        }
        catch ( IOException ioEx ) {
            System.out.println("Client:Socket Read Error !!");
        }
        return message ;
    }

    public void writeSocket( String message ) {
        try {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(message+'\n');
            outToServer.flush();
        }
        catch ( IOException ioEx ) {
            System.out.println ("Client:Socket Write Error !!");
        }
    }

    public void getFile( int size) {
        try {
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            int readSize = 0 , t = 0 ;
            String htmlFile = "" ;
            while ( readSize < size && t++ < 10 ) {
                Thread.sleep(500);
                int bufferSize = clientSocket.getInputStream().available() ;
                char[] buffer = new char[bufferSize] ;
                inFromServer.read(buffer,0,bufferSize) ;
                if ( bufferSize > 0 ) {
                    htmlFile += new String (buffer) ;
                }
                readSize += bufferSize ;
            }
            System.out.println(htmlFile);
        } catch (IOException ioEx) {
            System.out.println("Client:File Read Error(IO) !!");
        } catch (InterruptedException iEx) {
            System.out.println("Client:File Read Error(Interrupt) !!");
        }
    }

    public void sendFile ( File file ) {

        try {
            BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            FileReader fileReader = new FileReader(file) ;

            char[] buffer = new char[(int) file.length()] ;
            fileReader.read(buffer,0, (int) file.length()) ;

            outToServer.flush();
            outToServer.write(buffer, 0, (int) file.length());
            Thread.sleep(2000);
            outToServer.flush();
            fileReader.close();

        } catch (IOException ioEx) {
            System.out.println("Client:File Write Error(IO) !!");
        } catch (InterruptedException iEx) {
            System.out.println("Client:File Read Error(Interrupt) !!");
        }
    }

    public void closeSocket() {
        try {
            clientSocket.close();
        }
        catch ( IOException ioEx ) {
            System.out.println("Client:Socket Closing Error !!");
        }
    }
}
