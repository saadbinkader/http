import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        final int maxThreadPoolSize = 9;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(10);

        threadPool = new ThreadPoolExecutor(0,maxThreadPoolSize,1,TimeUnit.SECONDS,queue);

        while ( true ) {
           try {
               serverSocket = new ServerSocket(6789) ;
               connectionSocket = serverSocket.accept() ;
               serverManager = new ServerManager( connectionSocket ) ;
               Thread thread = new Thread( serverManager );
               if ( threadPool.getPoolSize() < maxThreadPoolSize ) {
                  threadPool.execute(thread);
               }
               else {
                  System.out.println("Server:Server Busy!!"+'\n');
                   thread.interrupt();
               }
               System.out.println("Thread Pool Size : " + threadPool.getPoolSize());
           }  catch (IOException ioEx) {
               ioEx.printStackTrace();
           }
        }
    }
}
