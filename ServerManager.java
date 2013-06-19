/**
 * Created with IntelliJ IDEA.
 * User: sk-saad
 * Date: 6/17/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.net.*;

public class ServerManager implements Runnable {
    private ServerSocket serverSocket ;
    private Socket connectionSocket ;
    private boolean running;


    ServerManager( Socket connectionSocket ) {

        running = true ;
        this.connectionSocket = connectionSocket ;
    }

    public String readSocket() {
        String message = new String() ;
        try {
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            message = inFromClient.readLine() ;
        }
        catch ( IOException ioEx ) {
            System.out.println("Server:Socket Read Error !!") ;
        }
        return message ;
    }

    public void writeSocket( String message ) {
        try {
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            outToClient.flush();
            outToClient.writeBytes(message);
        }
        catch ( IOException ioEx ) {
            System.out.println("Server:Socket Write Error !!");
        }
    }
    public void closeSocket() {
        try {
            connectionSocket.close();
            serverSocket.close();
        } catch (IOException ioEx) {
            System.out.println("Server:Socket Closing Error !!");
        }
    }
    public Socket getSocket() {
        return connectionSocket ;
    }
    public void run() {
        while(running) {
            String message = this.readSocket() ;
            if ( message.equals("exit") ) {
                running = false ;
                continue;
            }
            else if ( message.startsWith("get") ) {
                System.out.print(Thread.currentThread().toString());
                System.out.println( "Client: Requested " + message.substring(4) ) ;

                File file = new File(message.substring(4)) ;
                if ( file.exists() ) {
                    this.writeSocket( "Sending " + file.length() + '\n');
                    this.sendFile(file);
                }
                else {
                    final String dir = System.getProperty("user.dir");
                    this.writeSocket("You wanted :"+message.substring(4) +"File Not Found in "+dir+'\n') ;
                }
            }
            else if ( message.startsWith("post") ) {
                this.writeSocket( "Ok, send file " + '\n');
                String reply = this.readSocket() ;
                if ( reply.startsWith("posting") )   {
                    System.out.println(Integer.parseInt(reply.substring(8))) ;
                    this.getFile( Integer.parseInt(reply.substring(8)) ) ;
                }
                else
                    System.out.println("Client Says:[" + reply + "]");
            }
            else
                System.out.println("Client Says:[" + message + "]");
        }
    }
    public void shutDown() {
        running = false ;
    }
    public void sendFile ( File file ) {

        try {
            BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
            FileReader fileReader = new FileReader(file) ;

            char[] buffer = new char[(int) file.length()] ;
            fileReader.read(buffer,0, (int) file.length()) ;

            outToClient.flush();
            outToClient.write(buffer, 0, (int) file.length());
            Thread.sleep(2000);
            outToClient.flush();
            fileReader.close();

        } catch (IOException ioEx) {
            System.out.println("Server:File Write Error(IO) !!");
        } catch (InterruptedException iEx) {
            System.out.println("Server:File Write Error(Interrupt) !!");
        }
    }

    public void getFile ( int size ) {
        try {
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            int readSize = 0 ;
            int t = 0 ;
            String htmlFile = "" ;
            while ( readSize < size && t++ < 10) {
                Thread.sleep(500);
                int bufferSize = connectionSocket.getInputStream().available() ;
                char[] buffer = new char[bufferSize] ;
                inFromServer.read(buffer,0,bufferSize) ;
                if ( bufferSize > 0 ) {
                    htmlFile += new String (buffer) ;
                }
                readSize += bufferSize ;
            }
          System.out.println(htmlFile);
        } catch (IOException ioEx) {
            System.out.println("Server:File Write Error(IO) !!");
        } catch (InterruptedException iEx) {
            System.out.println("Server:File Write Error(Interrupt) !!");
        }
    }
}
