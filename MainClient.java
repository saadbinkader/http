import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: sk.saad
 * Date: 6/18/13
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class MainClient {
    public static void main (String args[]) {
        ClientManager clientManager = null;
        String reply ;
        boolean connected = false ;
        BufferedReader br = new BufferedReader ( new InputStreamReader(System.in)) ;
        String command = new String() ;
        while ( true ) {
            try {
               command = br.readLine() ;
            } catch ( IOException ioEx) {
                System.out.println("Input Error!!");
                continue ;
            }

            if ( command.equals("connect") && !connected )  {
                clientManager = new ClientManager(6789) ;
                clientManager.writeSocket(Thread.currentThread().getName()+ '\n');
                reply = clientManager.readSocket() ;

                if ( reply.equals("Server:Server Connected!!") )
                    connected = true ;
                System.out.println( reply ) ;
            }

            else if ( command.startsWith("get") && connected ) {
                clientManager.writeSocket(command+ '\n');
                reply = clientManager.readSocket() ;
                if ( reply.startsWith("Sending") ) {
                    clientManager.getFile(Integer.parseInt(reply.substring(8))) ;
                }
                else
                    System.out.println(reply) ;
            }

            else if  ( command.startsWith("post") && connected) {
                clientManager.writeSocket(command+ '\n');
                reply = clientManager.readSocket() ;
                System.out.println("Server says:["+reply+"]");
                File file = new File(command.substring(5)) ;
                if ( file.exists() ) {
                    clientManager.writeSocket( "posting " +file.length()+'\n');
                    clientManager.sendFile (file) ;
                }
                else {
                    final String dir = System.getProperty("user.dir");
                    System.out.println( "No such file("+file.getName()+") in " + dir ) ;
                    clientManager.writeSocket( "Sorry, couldn't find file !!" + '\n' );
                }

            }

            else if ( command.equals("exit") && connected ) {
                clientManager.writeSocket(command+ '\n');
                break ;
            }
            else
                System.out.println("Invalid Command");
        }
    }
}
