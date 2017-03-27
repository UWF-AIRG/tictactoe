import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedInputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
/**
 * This class represents a client for the Tic Tac Toe network game. It prints the user interface,
 * and directly interacts with the user, and translates the input into valid N3TP protocol commands.
 * @author Stephen Majors
 * @assignment 5
 * @filename TicTacToeClient.java
 *
 */
public class TicTacToeClient {

	private Socket clientSocket;			// The main game socket
	private Scanner consoleScanner;			// The scanner for console input
	private PrintWriter out;				// Sends the commands over TCP/IP
	private InputStream in1;				// Receives the raw commands over TCP/IP
	private BufferedInputStream in2;		// Wraps up the InputStream into something more manageable
	
	/**
	 * The main constructor for a Tic Tac Toe client. It creates all the necessary
	 * IO components and Sockets for the game to be ran. 
	 */
	public TicTacToeClient()
	{
		String address = null;
		try
		{
			consoleScanner = new Scanner(System.in);
/*			System.out.print("Enter the address that is hosting the TTServer: ");    Uncomment this to have user select host 
			address = consoleScanner.nextLine();*/
			
			address = "localhost";
			
			clientSocket = new Socket(address, TicTacToeServer.N3TP_PORT);	// Connect the client to the server
			out = new PrintWriter(clientSocket.getOutputStream());			// Create the output stream

			in1 = clientSocket.getInputStream();							// Create the input stream
			in2 = new BufferedInputStream(in1);								// Wrap the input stream up
		}
		catch (ConnectException e)
		{
			System.out.println("Connection timed out.");
			System.exit(-128);
		}
		
		catch (IOException i)
		{
			if (i instanceof UnknownHostException)
			{
				System.out.println("Could not find the host: " + address);
				System.exit(-64);
			}
			i.printStackTrace();
		}
		
	}
	public static void main(String[] args) 
	{
		TicTacToeClient tttClient = new TicTacToeClient();		// Create a new Client object
		tttClient.getCommands();								// Enter the main game loop
	}
	
	/**
	 * This is the main game loop. It sits in a loop, sending commands over the
	 * TCP/IP stream, and receives responses in relation to the game itself.
	 */
	public void getCommands()
	{
		String response;								// Responses received

		System.out.print("Enter your name: ");			// Ask for the user's name
		String name = consoleScanner.nextLine();
		
		// Attempt to join the game
		
		out.print("join " + name + "\n");				// Send the join command to the service
		out.flush();
		
		response = readResponseBuffer(in2);
		
		/* If the game is full, abort here */
		
		if (response.equals("Sorry, all player spaces are taken."))
		{
			System.out.println("Sorry, all available spots are taken. Try again later");
			return;
		}
		
		System.out.println(response);			// This response contains the welcome message
		
		/* The main game loop */
		while(!TicTacToeServer.gameOver)
		{

			final int START_PLAY_NUM = 0;
			final int CURR_PLAY_NUM = -1;
			
			int yourPlayerNumber = START_PLAY_NUM;
			int currentPlayerNumber = CURR_PLAY_NUM;
			do
			{
				// This block loops while it waits for this player's turn to come up
				try
				{
					out.println("playerturn");					// Send for the current player's turn
					out.flush();
					response = readResponseBuffer(in2);
					String responseSplit[] = response.split(",");	// Split the response into two and convert it to ints
					yourPlayerNumber = Integer.parseInt(responseSplit[0]);
					currentPlayerNumber = Integer.parseInt(responseSplit[1].substring(0,1));
					Thread.currentThread();
					Thread.sleep(1000);					// Sleep for one second so we do not overload the server with requests
				}
				
				/* Most likely will be caught if the server unexpectedly dies while the client is blocked */
				catch (Exception e)
				{

					System.out.println("Fatal Error. Connection abruptly reset.");
					System.exit(-128);

				}

			} while(yourPlayerNumber != currentPlayerNumber);
			
			out.println("boardstate");							// Send off for the board state
			out.flush();
			response = readResponseBuffer(in2);					// Read in the board state and print it
			System.out.println(response);
			
			System.out.print("Please enter the x and y coordinates to place your mark.\n" +
					"All indexes are 0 based. Example: 1 1 for the center point.\nDo not add any extraneous punctuation, only spaces: ");
			String[] cResponse = consoleScanner.nextLine().split(" ");	// Grab the move from the keyboard input
			if (cResponse[0].toUpperCase().equals("QUIT"))				// If the 'move' is QUIT, then quit the game.
			{
				out.println("quit");
				out.flush();
				break;
			}
			try
			{
				out.println("choose " + cResponse[0] + " " + cResponse[1]);	// Send the move off to the Service
				out.flush();
			}
			catch (ArrayIndexOutOfBoundsException a)						// If there was illegal input of some sort.
			{																// Note that extra inputs are ignored
				System.out.println("Illegal input.");						// This is only when there is not enough input
				continue;
			}

			response = readResponseBuffer(in2);								// The response back from the service. This will contain the validity of the move
			System.out.println(response);									// and if the game is over or not.
			
			// If the board is full, or of there is a winner, quit the game
			if (response.indexOf("full,") != -1 || (response.indexOf("winner!") != -1))
			{
				System.out.println("Terminating connection.");
				TicTacToeServer.gameOver = true;
			}
		}
		try
		{
			clientSocket.close();				// Close all of the IO components and sockets
			in1.close();
			out.close();
			consoleScanner.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method takes a <code>BufferedInputStream</code> from a Socket, and returns the characters
	 * in the stream. This method does not block waiting for more input, unlike the <code>hasNext()</code>
	 * method in <code>Scanner</code>
	 * @param input The <code>BufferedInputStream</code> containing the contents from the Socket.
	 * @return The contents of the stream.
	 */
	private String readResponseBuffer(BufferedInputStream input)
	{
		int i = 0;
		String returnResp = "";
		try
		{
			do
			{
				i = input.read();				// Read in a character from the stream
				returnResp += (char)i;			// Cast it to char, and add it to the return String
			} while (input.available() > 0);	// This is set to greater than 0, so it does not block waiting for input
			
		}
		/* If recv failed, then the other client has disconnected because it has already won */
		/* Terminate this connection in that case, otherwise continue. */
		catch (Exception e)
		{
			if (e.getMessage().indexOf("recv") != 0)
			{
				System.out.println("Game Over");
				System.exit(-1);
			}
			
		}
		
		return returnResp;
	}

}
