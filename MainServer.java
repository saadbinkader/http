import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: sk.saad
 * Date: 6/18/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class MainServer {
    public static void main ( String args[] ) {

        ServerSocket serverSocket = null;
        Socket connectionSocket = null;
        ThreadPoolExecutor threadPool = null;
        ServerManager serverManager ;
        final int maxThreadPoolSize = 2;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(10);

        try {
            serverSocket = new ServerSocket(6789) ;
            threadPool = new ThreadPoolExecutor(0,maxThreadPoolSize,1,TimeUnit.SECONDS,queue);
        } catch ( IOException ioEx) {

        }

        while ( true ) {
           try {
               connectionSocket = serverSocket.accept() ;
               BufferedReader inFromClient =
                       new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
               serverManager = new ServerManager( connectionSocket ) ;
               Thread thread = new Thread( serverManager );
               if ( threadPool.getPoolSize() < maxThreadPoolSize ) {
                  System.out.println("Served ["+inFromClient.readLine()+"]");
                  serverManager.writeSocket("Server:Server Connected!!"+'\n');
                  threadPool.execute(thread);

               }
               else {
                   serverManager.writeSocket("Server:Server Busy!!"+'\n');
                   thread.interrupt();
               }
               System.out.println("Thread Pool Size : " + threadPool.getPoolSize());
           }  catch (Exception ioEx) {}
        }
    }
}
