/**
 * Created with IntelliJ IDEA.
 * User: sk-saad
 * Date: 6/17/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerManager implements Runnable {
    private Socket connectionSocket ;
    String currentDirectory ;
    private String serverName, connectionType, date, lastModified, contentType, contentLength ;


    ServerManager( Socket connectionSocket ) {
        currentDirectory = System.getProperty("user.dir");
        this.connectionSocket = connectionSocket ;
        date =  "Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + '\n';
        serverName =  "Server: " + "Custom Server" + " (Unix) (Red-Hat/Linux)" + '\n';
        lastModified =  "Last-Modified: " ;
        contentType = "Content-Type: text/html; charset=UTF-8" + '\n';
        contentLength = "Content-Length: " ;
        connectionType = "Connection: " ;
    }

    public void run() {
        try {
             BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
             int bufferSize = connectionSocket.getInputStream().available() ;
             char[] buffer = new char[bufferSize] ;
             inFromServer.read(buffer,0,bufferSize) ;
             String htmlHeader = new String (buffer) ;
             if ( htmlHeader.startsWith("GET") )
                 serveGET(htmlHeader) ;
            else if ( htmlHeader.startsWith("POST") )
                 servePOST(htmlHeader) ;
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    public void serveGET ( String htmlHeader ) {

        System.out.println (htmlHeader) ;

        int startPosition, endPosition;
        String fileName = new String() ;
        startPosition = htmlHeader.indexOf('/') ;
        endPosition = htmlHeader.indexOf("HTTP")-1;
        fileName = htmlHeader.substring(startPosition,endPosition);
        File file = new File( currentDirectory+"/files"+fileName ) ;
        if ( file.exists() ) {
            try {
                String lastModified = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format( new Date( file.lastModified() ) ) ;
                long fileLength = file.length();
                BufferedReader bufferedReader = new BufferedReader( new FileReader(file));
                PrintWriter printWriter = new PrintWriter(connectionSocket.getOutputStream(),true);
                String buffer = "" ;

                printWriter.print(get200Header(fileLength,lastModified,"close"));

                while ( ( buffer = bufferedReader.readLine() ) != null )
                    printWriter.println(buffer);

                bufferedReader.close();
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                PrintWriter printWriter = new PrintWriter(connectionSocket.getOutputStream(),true);
                System.out.print(get404Header("close"));
                printWriter.print(get404Header("close"));
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void servePOST (  String htmlHeader ) {
        try {
            System.out.println(htmlHeader);
            PrintWriter printWriter = new PrintWriter(connectionSocket.getOutputStream(),true);
            String lastModified = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) ;

            String response ="<html>" +
            "<head>\n"   +
            "<title>Your POST Response</title>\n"  +
            "</head>\n"  +
            "<body>\n"  ;
            int startPosition = 0, endPosition ;
            boolean gotFormattedData = false;
            while ( ( startPosition = htmlHeader.indexOf("name=",startPosition) ) != -1 ) {
                gotFormattedData = true ;
                startPosition += 6;
                endPosition = htmlHeader.indexOf('\"',startPosition) ;
                String name = htmlHeader.substring(startPosition,endPosition);
                startPosition = endPosition+5;
                endPosition = htmlHeader.indexOf('\n',startPosition+1);
                String value = htmlHeader.substring(startPosition,endPosition-1) ;
                response += "<h1>Field Name:["+name+"] Value:[" + value +  "]</h1>" ;
            }
            if(!gotFormattedData) {
                startPosition = htmlHeader.length()-1;
                while( htmlHeader.charAt(startPosition-1) != '\n' ) startPosition-- ;
                String parameter = htmlHeader.substring(startPosition);
                startPosition = 0 ;
                while ( ( endPosition = parameter.indexOf('=',startPosition) ) != -1 ) {
                    String name = parameter.substring(startPosition,endPosition);
                    String value = "" ;
                    startPosition = endPosition+1 ;
                    if ( ( endPosition = parameter.indexOf('&',startPosition) ) == -1 ) {
                        value = parameter.substring(startPosition) ;
                        startPosition = parameter.length() ;
                    }
                    else {
                        value = parameter.substring(startPosition,endPosition) ;
                        startPosition = endPosition + 1;
                    }
                    response += "<h1>Field Name:["+name+"] Value:[" + value +  "]</h1>" ;
                }
            }
            response +=
                    "</body>\n" +
                    "</html>\n"  + '\n'   ;
            printWriter.print(get200Header(response.length(),lastModified,"close"));
            printWriter.print(response);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String get200Header( long contentLength ,String lastModified ,String connectionType ) {
         String reply =  "HTTP/1.1 200 OK" + '\n'
                 + this.date
                 + this.serverName
                 + this.lastModified + lastModified + '\n'
                 + this.contentType
                 + this.contentLength + contentLength + '\n'
                 + this.connectionType + connectionType + '\n'
                 + '\n' ;
        System.out.print(reply);
        return reply ;
    }
    private String get404Header( String connectionType ) {
        String reply =  "HTTP/1.1 404 Not Found" + '\n'
                + this.date
                + this.serverName
                + this.contentType
                + this.connectionType + connectionType + '\n'
                + '\n'
                + "<html>\n" +
                "<head>\n" +
                "  <title>404 Not Found</title>\n" +
                "</head>\n" +
                "<body><h1>\n" +
                "  Error 404: Content Not Found!!\n" +
                "</h1></body>\n" +
                "</html>\n";
        return reply ;
    }
}
