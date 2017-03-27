import java.util.Scanner;


/**
 * This class represents a generic Tic Tac Toe board, that is 3 x 3 spaces.
 * @author Stephen Majors
 * @assignment 5
 * @filename TicTacToeBoard.java
 *
 */
public class TicTacToeBoard 
{
	private final int X = 1;				// Values to add for X
	private final int O = -1;				// Value to add for O
	private final int EMPTY = 0;			// Value to add for EMPTY
	private final int BOARD_X = 3;			// Board X width
	private final int BOARD_Y = 3;			// Board Y height
	private final int X_WINS = 3;			// Condition for X to win
	private final int O_WINS = -3;			// Condition for O to win
	private String moveErrorReason = "";
	
	private enum boardCell { O, EMPTY, X};	// Enumeration of all possible cell states
	
	private boardCell[][] ttBoard;			// Creating a board of these cells
	
	/**
	 * The main constructor for this class. It creates a Tic Tac Toe board of BOARD_X by
	 * BOARD_Y spaces, and fills all the spaces with EMPTY (0) cells.
	 */
	public TicTacToeBoard()
	{
		ttBoard = new boardCell[BOARD_X][BOARD_Y];			// Create the new board
		
		for (int i = 0; i < ttBoard.length; i++)			// And fill all the cells with EMPTY
			for (int j = 0; j < ttBoard[i].length; j++)
				ttBoard[i][j] = boardCell.EMPTY;
	}
	
	/**
	 * This method adds a move to this board. It checks first to see if
	 * the move is at a valid position, then it checks to see if there is a move already there, then
	 * places the move if there is not already a move at that position.
	 * @param row The X position to place the move
	 * @param column The Y position to place the move
	 * @param move The move itself
	 * @return <code>true</code> if the move was placed, <code>false</code> if the move was not placed.
	 */
	public boolean setMoveOnBoard(int row, int column, String move)
	{
		/* Check first for the validity of the move in regards to the board
		 * If the move is placed out of bounds, declare the move invalid and return.
		 */
		boolean invalidBoardPosition = (row >= BOARD_X) || (column >= BOARD_Y)
				|| (row < 0) || (column < 0);
		
		if (invalidBoardPosition)
		{
			moveErrorReason = "Invalid move at position.";
			return false;
		}
		
		// Check for the existence of any other entities at this position except EMPTY
		
		boardCell atThisPos = ttBoard[row][column];  // Get the current state of the selected row,column
		
		if (!atThisPos.equals(boardCell.EMPTY))		// If not EMPTY, declare the move invalid and return
		{	
			moveErrorReason = "There is already a move at this position";
			return false;
		}
		move = move.toUpperCase();				// To eliminate mismatches
		
		// Place the move if it is a valid X or Y. If not, just return false.
		if (move.equals("X"))
			ttBoard[row][column] = boardCell.X;
		else if (move.equals("O"))
			ttBoard[row][column] = boardCell.O;
		else
			return false;
	
		return true;
	}
	
	/**
	 * This method declares a winner, if there currently is a winner. It runs through
	 * all possible states of the board, inclding all vertical and horizontal and diagonal
	 * wins, as well as the FULL state of the board.
	 * @return A String stating the winner of the match, or the FULL state of the board.
	 */
	public String declareWinner()
	{		
		int counter = 0;
		
		// Check if board is full
		for (int i = 0; i < ttBoard.length; i++)
		{
			for (int j = 0; j < ttBoard[i].length; j++)
			{
				if (ttBoard[i][j].toString().equals("EMPTY"))
					counter++;
			}
		}
		
		if (counter == 0) { return "FULL"; } // This will be 0 if there are no valid spaces left
		
		counter = 0;
		
		// Check Horizontal
		for (int i = 0; i < ttBoard.length; i++)
		{
			for (int j = 0; j < ttBoard[i].length; j++)
			{
				if (ttBoard[i][j].toString().equals("X"))
					counter += X;
				else if (ttBoard[i][j].toString().equals("O"))
					counter += O;
				
			}
			
			if (counter == X_WINS)
				return "X";
			else if (counter == O_WINS)
				return "O";
			
			counter = 0;
				
		}
		 // Check Vertical
		
		for (int i = 0; i < ttBoard.length; i++)
		{
			for (int j = 0; j < ttBoard[i].length; j++)
			{
				if (ttBoard[j][i].toString().equals("X"))
					counter += X;
				else if (ttBoard[j][i].toString().equals("O"))
					counter += O;
				
			}
			
			if (counter == X_WINS)
				return "X";
			else if (counter == O_WINS)
				return "O";
			
			counter = 0;
				
		}
		
		// Check forward diagonals
		
		counter = 0;
		
		/* Forward diagonals only need one loop.
		 * The straight line diagonal points will all be the same numbers.
		 * E.G 0-0, 1-1, 2-2 etc
		 */
		for (int k = 0; k < BOARD_X; k++)
		{
			if (ttBoard[k][k].toString().equals("X"))
				counter += X;
			else if (ttBoard[k][k].toString().equals("O"))
				counter += O;
		}
		
		if (counter == X_WINS)
			return "X";
		else if (counter == O_WINS)
			return "O";
		
		counter = 0;
		
		int incrementer = 0;
		
		// Check reverse diagonals 
		
		/*
		 * Backwards diagonals are a little different. We need a counter to keep track of where we are. Backwards
		 * diagonal points work like this. 0-2, 1-1, 2-0
		 */
		for (int l = BOARD_X - 1; l >= 0; l--)
		{
			if (ttBoard[l][incrementer].toString().equals("X"))
				counter += X;
			else if (ttBoard[l][incrementer].toString().equals("O"))
				counter += O;
			incrementer++;
		}
		
		if (counter == X_WINS)
			return "X";
		else if (counter == O_WINS)
			return "O";
		
		
		return "NO";			// If no winner is determined, and the board is not full.
	}
	
	@Override
	public String toString()
	{
		String theBoard = "";
		
		for (int i = 0; i < ttBoard.length; i++)
		{
			for (int j = 0; j < ttBoard[i].length; j++)
			{
				if (ttBoard[i][j].toString().equals("EMPTY")) // If the board is empty at this cell, print a '_' character
					theBoard += "_ ";
				else
					theBoard += ttBoard[i][j].toString() + " "; // Else, print the character in this cell
			}
			
			if (i < ttBoard.length - 1)	// If we have reached the end of a row, print a new line character.
				theBoard += "\n";
		}
		
		return theBoard;
	}
	
	/**
	 * Returns the reason why the move was invalid
	 * @return A <code>String<code> containing the reason why the move was invalid, or
	 * No Error if there is no current error.
	 */
	public String getMoveErrorReason()
	{
		String temp = moveErrorReason;
		if (temp.equals("")) { return "No error."; }
		moveErrorReason = "";
		return temp;
	}
	

	
	
	public static void main(String[]args)
	{
		Scanner in = new Scanner(System.in);
		TicTacToeBoard tt = new TicTacToeBoard();
		System.out.println("Ctrl+C to exit this demo");
		while (true)
		{
			System.out.println(tt.toString());
			System.out.print("Enter a move. In form CoordX,CoordY,Move (0 index): ");
			String[] theMove = in.nextLine().split(",");
			
			int x = Integer.parseInt(theMove[0]);
			int y = Integer.parseInt(theMove[1]);
			boolean wasMoveValid = tt.setMoveOnBoard(x, y, theMove[2]);
			
			if (!wasMoveValid)
			{
				System.out.println("Move was invalid. Try again.");
				continue;
			}
			
			String temp = tt.declareWinner();
			
			if (temp.equals("X"))
			{
				System.out.println("X Wins!");
				System.out.println(tt.toString());
				break;
			}
			else if (temp.equals("O"))
			{
				System.out.println("O Wins!");
				System.out.println(tt.toString());
				break;
			}
		}
	}
}
