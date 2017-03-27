import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * This class represents a server for a Tic Tac Toe game. It creates the connections to the game,
 * and spawns them off into new threads.
 * @author Stephen Majors
 * @assignment 5
 * @filename TicTacToeServer.java
 *
 */
public class TicTacToeServer 
{

	final static int N3TP_PORT = 4999;		// The game port
	static boolean gameOver = false;		// Static variable so the clients can find out when the game is over
	
	public static void main(String[] args) throws IOException 
	{
		Game game = new Game();				// Create a new Tic Tac Toe game
		
		ServerSocket gameServer = null;
		try 
		{
			gameServer = new ServerSocket(N3TP_PORT);	// Create a new ServerSocket to listen for incoming connections
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("Waiting for TicTacToe clients...");
		
		while(true)
		{
			Socket clientSocket = null;
			try 
			{
				clientSocket = gameServer.accept();		// Wait for new connections to arrive
				MonitorServerSocket mss = new MonitorServerSocket(gameServer, clientSocket);
				Thread msst = new Thread(mss);
				msst.setName("MonitorServerSocket");
				msst.start();
				
			} 
			catch (IOException e) 
			{
				if (e instanceof SocketException)
				{
					gameServer.close();
					System.out.println("Server terminating due to game over.");
					System.exit(-1);
				}
				e.printStackTrace();
			}

			System.out.println("A TicTacToe client has connected...");
			TicTacToeService t = new TicTacToeService(clientSocket, game);	// Spawn a new service for this incoming client
			Thread thr = new Thread(t);										// Spawn a new thread for the client
			thr.setName("PlayerThread");
			thr.start();													// And run the thread
		}

	}
	
	/**
	 * This class is bound to each client that is spawned. It checks the state of the socket. As soon as a client closes for any
	 * reason, the server will disconnect. These disconnects are usually due to game over however
	 * @author Stephen Majors
	 * @assignment 5
	 * @filename TicTacToeServer.java
	 *
	 */
	static class MonitorServerSocket implements Runnable
	{

		private ServerSocket refServerSocket;					// Reference to the ServerSocket
		private Socket refClientSocket;							// Reference to the ClientSocket
		private final int PAUSE = 5000;							// Class will pause this amount of time
		
		public MonitorServerSocket(ServerSocket s, Socket cs)
		{
			refServerSocket = s;		// Reference to the ServerSocket
			refClientSocket = cs;		// Reference to the Client sockets
			
			System.out.println("Spawned MonitorServerSocket");
		}
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					if (refClientSocket.isClosed())			// If the client socket is closed
					{
						refServerSocket.close();			// Close the server socket
					}
					
					Thread.currentThread();					// Sleep here for 5 seconds
					Thread.sleep(PAUSE);
				
				}
				catch (IOException i)
				{
					break;						// Thrown immediately after killing the ServerSocket.
				}
				
				catch (InterruptedException ie)
				{
					break;
				}
				
			
			}
		
		}

	}
}
