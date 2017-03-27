
/**
 * This class interacts between a TicTacToe game service, and the tic tac toe board itself.
 * @author Stephen Majors
 * @assignment 5
 * @filename Game.java
 *
 */
public class Game 
{
	
	private String player1 = "";				// Player 1's name
	private String player2 = "";				// Player 2's name
	private int numOfPlayers = 0;				// Number of players currently registered
	private TicTacToeBoard ttBoard;				// The Tic Tac Toe board
	private boolean gameStart = false;			// Has the game started
	private int activePlayerNumber;				// The active player number
	
	/**
	 * The default constructor. This creates the Tic Tac Toe board.
	 */
	public Game()
	{
		ttBoard = new TicTacToeBoard();
	}
	
	/**
	 * Returns the name of player 1
	 * @return Player 1's name
	 */
	public String getPlayerOne()
	{
		return player1;
	}
	
	/**
	 * Returns the name of Player 2
	 * @return PLayer 2's name
	 */
	public String getPlayerTwo()
	{
		return player2;
	}
	
	/**
	 * Adds a player name to the game
	 * @param playerName The name of player to add
	 * @return <code>true<code> if the add was successful, <code>false</code> if the add was not successful.
	 */
	public boolean addPlayer(String playerName)
	{
		if (numOfPlayers == 2)						// If there are two players already, abort here.
			return false;
		else if (numOfPlayers == 0)					// If there are no players, add player 1 to the game.
		{
			player1 = playerName;
			numOfPlayers++;
			activePlayerNumber = 1;					// Also set the active player to 1 so player 1 starts first
		}
		else if (numOfPlayers == 1)					// If there is one player, add player 2
		{
			player2 = playerName;
			numOfPlayers++;
		}
		
		if (numOfPlayers == 2) { gameStart = true; }	// Start the game if we have two players
		return true;
			
	}
	
	/**
	 * Place a move on the board. Player number will determine if an X or an O is placed.
	 * @param playerNum The player number. This will determine if an X or an O is placed
	 * @param row The row of the board to place the mark on
	 * @param column The column of the board to place the mark on
	 * @return <code>true</code> if the move placement was successful, <code>false</code> if the move placement was not successful.<br/>
	 * If it is false, the caller should also call <code><b>getMoveErrorReason()</b></code> to determine why the move was invalid.
	 */
	public boolean sendMove(int playerNum, int row, int column)
	{
		boolean retVal = false;
		
		if (playerNum == 1)											// Place an X for Player 1
			retVal = ttBoard.setMoveOnBoard(row, column, "X");
		else if (playerNum == 2)									// Place an O for Player 2
			retVal = ttBoard.setMoveOnBoard(row, column, "O");
		
		// Switch player numbers around if move was valid. It is the end of player x's turn.
		if (retVal)
		{
			if (activePlayerNumber == 1)
				activePlayerNumber = 2;
			else
				activePlayerNumber = 1;
		}
		
		return retVal;
	}
	
	/**
	 * Get the reason why the move was invalid.
	 * @return A <code>String</code> containing the reason why the move was invalid.
	 */
	public String getMoveErrorReason()
	{
		return ttBoard.getMoveErrorReason();
	}
	
	/**
	 * Determines if the game has started or not
	 * @return <code>true</code> if the game has started, <code>false</code> if the game has not started.
	 */
	public boolean hasGameStarted()
	{
		return gameStart;
	}
	
	/**
	 * Gets the currently active player number in the game.
	 * @return The current player number.
	 */
	public int getActivePlayerNumber()
	{
		return activePlayerNumber;
	}
	
	/**
	 * Gets the current board state. It returns the board in the format<br><br>
	 * _ _ _<br>
	 * _ _ _<br>
	 * _ _ _<br><br>
	 * 
	 * With blanks filled in by currently occupied spaces.
	 * @return The state of the game board.
	 */
	public String getBoardState()
	{
		return ttBoard.toString();
	}
	
	/**
	 * Returns a String with the status of the current game, and the winner of the game if there
	 * is a winner.
	 * @return The status of the game.
	 */
	public String getWinnerStatus()
	{
		return ttBoard.declareWinner();
	}
}
