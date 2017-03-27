import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class is the intermediate class between the Tic Tac Toe clients, and the Tic Tac Toe board
 * itself. It receives commands over the N3TP protocol and communicates with the game board to process
 * these commands.
 * @author Stephen Majors
 * @assignment 5
 * @filename TicTacToeService.java
 *
 */
public class TicTacToeService implements Runnable
{

	private Socket gameSocket;					// The socket to communicate with the clients over
	private Scanner in;							// To receive commands
	private PrintWriter out;					// To send responses back
	private Game game;							// The reference to the Game class which controls the board
	private int playerNumber = 0;				// The player number of this instance of the service
	private String playerName;					// The player name of this instance of the service
	static String gameWinner = "";				// A string that contains human readable strings about the state of the game
	
	private final int PLAYER_1 = 1;				// Player 1	
	private final int PLAYER_2 = 2;				// Player 2

	private final String X_WINS = "X";			// X wins
	private final String O_WINS = "O";			// O wins
	private final String FULL_BOARD = "FULL";	// The board is full
	private final String NO_WINNER_YET = "NO";	// The game continues
	
	private final String CMD_BSTATE = "BOARDSTATE";		// N3TP get board state
	private final String CMD_JOIN = "JOIN";				// N3TP join game
	private final String CMD_CHOOSE = "CHOOSE";			// N3TP choose move
	private final String CMD_PTURN = "PLAYERTURN";		// N3TP get player turn
	private final String CMD_QUIT = "QUIT";				// N3TP quit game
	

	/**
	 * Main constructor. Creates a socket to the client, and sets up a reference to the game the client
	 * will be playing.
	 * @param theSocket Socket to communicate with the client over.
	 * @param theGame A reference to the game from the Server
	 */
	public TicTacToeService(Socket theSocket, Game theGame)
	{
		game = theGame;
		gameSocket = theSocket;
	}
	
	public void run() 
	{
		
		try
		{
			try
			{
				in = new Scanner(gameSocket.getInputStream());				// Create input stream
				out = new PrintWriter(gameSocket.getOutputStream());		// Create output stream
				processLoop();												// Enter main service loop
			}
			
			finally
			{
				gameSocket.close();											// Close socket after we are done playing
			}
		}
		catch (IOException i)
		{
			i.printStackTrace();
		}
		
	}
	
	/**
	 * This is the main game service loop. It takes in the commands and processes them,
	 * sending output back to the clients as needed.
	 */
	public void processLoop()
	{
		while (true)
		{
			
			/* Block to check if there is a winner. We check here to prevent the game from infinitely running. */
			if (!game.getWinnerStatus().equals(NO_WINNER_YET))
			{
				sendWinnerStatus();
				break;
			}
			/* Wait at this position while waiting for client input */
			if (!in.hasNext())
				continue;
			String inCommand = in.nextLine().toUpperCase();						// Convert the input to uppercase
			
			/* If quit was sent, quit the game here. */
			if (inCommand.equals(CMD_QUIT))
			{
				out.println("Aborting game");
				out.flush();
				return;
			}
			else
				exec(inCommand);				// Send the command off to a separate execution method otherwise
			
			/* Here, we check if player number is anything other than 0. It will be 1 or 2 if the client
			 * was assigned a valid player number. If it is 0, there are already two players in the game.
			 * So go ahead and abort here.
			 */
			if (playerNumber == 0)
			{
				out.println("Aborting game due to too many players.");
				out.flush();
				return;
			}
			
				
		}
	}

	/**
	 * This method checks the Game itself to see if there is a winner. If there is, then we send the winner
	 * status back to the clients.
	 */
	private void sendWinnerStatus() 
	{
		String winnerStatus = game.getWinnerStatus();
		
		if (winnerStatus.equals(X_WINS))				// X player wins
		{
			out.println("X is the winner!");

		}
		else if (winnerStatus.equals(O_WINS))			// O player wins
		{
			out.println("O is the winner!");

		}
		else if (winnerStatus.equals(FULL_BOARD))		// The board is full of marks, and the game is over. (Draw)
		{
			out.println("The board is full, it is a draw.");

		}
		out.flush();
	}
	
	/**
	 * This method executes any remaining N3TP protocol commands other than QUIT
	 * @param theCommand The command string to execute.
	 */
	public void exec(String theCommand)
	{
		String[] commandSplit = theCommand.split(" ");				// Split the incoming command string up
		
		/* Get Player Turn Block */
		if (commandSplit[0].equals(CMD_PTURN))
		{
			int playNum = game.getActivePlayerNumber();		// This is the active player number
			out.println(playNum + "," + playerNumber);		// Print out player number of this instance of the Service, and the
			out.flush();									// currently active player number in comma delimited form.
			return;
		}
		
		/* This block grabs the board state from the Game itself */
		if (commandSplit[0].equals(CMD_BSTATE))
		{
			out.println(game.getBoardState());				// Returns the board state and prints it out.
			out.flush();
			return;
		}
		
		/* This block attempts to join the player to a game */
		if (commandSplit[0].equals(CMD_JOIN))
		{
			boolean canPlay;
			String name = commandSplit[1].toLowerCase();						// Grab the name from the command string, and lowercase it
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);	// Make the first character of the name uppercase
			canPlay = game.addPlayer(name);										// Determine if the user can play
			
			/* If the user can play, assign them a player number */
			if (canPlay)
			{
				if (game.getPlayerTwo().equals(""))			// If player 2 is not assigned yet, then this is player 1
				{
					/* Assign player 1, and print a welcome message */
					playerNumber = PLAYER_1;
					playerName = game.getPlayerOne();
					out.print("Hello " + game.getPlayerOne() + "! You are player number " + playerNumber);
				}
				else										// If player 2 is filled in, then this is player 2
				{
					/* Assign player 2, and print a welcome message */
					playerNumber = PLAYER_2;
					playerName = game.getPlayerTwo();
					out.print("Hello " + game.getPlayerTwo() + "! You are player number " + playerNumber);
				}
				
				out.flush();
				return;
			}
			else
			{
				// If all player positions are taken, abort this service
				out.println("Sorry, all player spaces are taken.");
				out.flush();
				return;
			}
		}
		/* Choosing a move block */
		else if (commandSplit[0].equals(CMD_CHOOSE))
		{
			
			// If the game has not started yet, back up to the previous method, and let the player know
			if (!game.hasGameStarted())
			{
				out.println("There are not two players yet. Please wait and try again.\n");
				out.flush();
				return;
			}
			
			// If it is not your turn right now, let the player know and back out of this method.
			if (game.getActivePlayerNumber() != playerNumber)
			{
				out.println("Sorry " + playerName + ". It is not your turn.\n");
				out.flush();
				return;
			}
			
			int r, c;		// To hold the row, and column of the move
			
			try
			{
				r = Integer.parseInt(commandSplit[1]);	// Get rows and columns from the input
				c = Integer.parseInt(commandSplit[2]);
			}
			
			catch (NumberFormatException nfe)			// If the input is not a number, let the user know
			{
				out.println("Non-numerical input for the positions. Try again\n");
				out.flush();
				return;
			}
			boolean legalMove = game.sendMove(playerNumber, r, c);		// Send move to game, and get back
																		// whether or not the move is legal
			if (!legalMove)
			{
				String temp = game.getMoveErrorReason();				// Get why it was illegal
				out.println(temp + "\n");								// And print it out.	
				out.flush();
				return;
			}
			
			
			out.println(game.getBoardState());							// Send the board state back, with the new move in place
			out.flush();
		}
	}

}
