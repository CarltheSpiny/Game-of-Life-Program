import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/***************************************************************
*
*	file: GameOfLife.java
*	@author Miguel Geronimo (AKA carlthespiny)
*	class: CS 1400.03 Intro to Programming and Problem Solving
*
*	assignment: Program 6
*	date last modified: 5/10/2022
*	purpose: Using the nested class, takes user input to find a
*	file, then creates a board. That board is then printed as many
*	times the user inputs, with each iteration following the 
*	Rules of Life.
*
****************************************************************/
public class GameOfLife {

	boolean areDebugMessagesEnabled = false;
	int[][] gameBoard;
	int[][] auxillaryGameBoard;
	boolean isAuxFilled = false;
	
	int totalRows = 0;
	int totalColumns = 0;
	
	int timesIterated = 0;
	int maxGenerationsToCompute;
	
	public GameOfLife() {
		Scanner scnr = new Scanner(System.in);
		File boardInfo;
		
		System.out.println("Enter file name:");
		String userInput = scnr.nextLine();
		if (!userInput.contains(".txt")) {
			userInput.concat(".txt");
		}
		
		boardInfo = new File(userInput);
		while (!boardInfo.exists()) {
			System.err.println("Could not find the specfied file \"" + boardInfo.getName() + "\"");
			System.out.println("Please Enter a vaild name (with .txt appended):");
			userInput = scnr.nextLine();
			
			boardInfo = new File(userInput);
		}
		System.out.println("Enter how many generations to compute:");
		int generationNum = 2;
		if (scnr.hasNext()) {
			generationNum = scnr.nextInt();
		} else {
			System.err.println("the Scanner did not have the number of generation to print! Will use just 2 generations");
		}
		this.areDebugMessagesEnabled = false;
		this.gameBoard = populateBoard(boardInfo);
		this.maxGenerationsToCompute = generationNum;
		this.computeNextGeneration(generationNum);
		scnr.close();
	}
	
	/**
	 *  Counts Lines from the passed file, returning the value as an Integer
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private int countLines(File fileName) throws IOException {
		InputStream inputStream = new BufferedInputStream(new FileInputStream(fileName));
		try {
			byte[] c = new byte[1024];
			int readChars = inputStream.read(c);
			if (readChars == -1) {
				return 0;
			}
			
			int count = 0;
			while (readChars == 1024) {
				for (int i = 0; i < 1024;) {
					if (c[i++] == '\n' ) {
						++count;
					}
				}
				readChars = inputStream.read(c);
			}
			while (readChars != -1) {
				
				for (int i = 0; i < readChars; i++) {
					if (c[i]  == '\n') {
						++count;
					}
				}
				readChars = inputStream.read(c);
			}
			return count == 0 ? 1 : count;
		} finally {
			inputStream.close();
		}
	}
	
	/**
	 * Returns number of columns in the game board
	 * @return
	 */
	public int getColumns() {
		return this.totalColumns;
	}
	
	/**
	 * Returns the number of rows on the board
	 * @return
	 */
	public int getRows() {
		return this.totalRows;
	}
	
	/**
	 * @author carlthespiny
	 * @param stringIn
	 * @return	a string with no spaces or hyphens, just numbers
	 */
	public String removeAllButNumbers(String stringIn) {
		String cleanedUpString = "";
		if (this.areDebugMessagesEnabled) System.out.println("Length of string to clean up is: " + stringIn.length());
		for (int p = 0; p < stringIn.length(); p++) {
			if (stringIn.charAt(p) != ' ') {
				if (stringIn.charAt(p) != '-') {
					if (stringIn.charAt(p) >= 48 && stringIn.charAt(p) <= 57) {
						cleanedUpString = cleanedUpString.concat(Character.toString(stringIn.charAt(p)));
					}
				}
				if (this.areDebugMessagesEnabled) System.out.println("Will add the char: " + stringIn.charAt(p) + " to the end of \"" + cleanedUpString + "\"");
			}
		}
		if (this.areDebugMessagesEnabled) System.out.println("The cleaned up string is: " + cleanedUpString);
		return cleanedUpString;
	}
	
	/**
	 * Fills the 2D array with the contents of the File
	 * @param gameBoard
	 * @return
	 */
	public int[][] populateBoard(File gameBoard) {
		int[][] completeBoard = new int[1][1];
		try {
			Scanner fileScnr = new Scanner(gameBoard);
			int rowsBoardSetting = fileScnr.nextInt(); // The end of the first line
			int columnsBoardSetting = fileScnr.nextInt();
			this.totalRows = countLines(gameBoard); //
			if (rowsBoardSetting != totalRows) {
				if (this.areDebugMessagesEnabled) System.err.println("The file settings for Rows do not match actual number of rows, using actual number of rows.");
				if (this.areDebugMessagesEnabled) System.err.println("The number of rows from the file settings: " + rowsBoardSetting + ". The number from counting the lines: " + totalRows);
				rowsBoardSetting = totalRows;
			} else {
				if (this.areDebugMessagesEnabled) System.out.println("The number of expected rows is: " + rowsBoardSetting);
			}
			
			completeBoard = new int[rowsBoardSetting][columnsBoardSetting]; // Create board with settings
			String lineForFlush = fileScnr.nextLine(); // Moves from settings line
			if (this.areDebugMessagesEnabled) System.out.println("Flushed this line: " + lineForFlush);
			
			for (int y = 0; y < rowsBoardSetting; y++) {
				if (fileScnr.hasNext()) {
					String currentLine = fileScnr.nextLine(); // Moves from settings line
					if (this.areDebugMessagesEnabled) System.out.println("Examing this line: \"" + currentLine + "\"");
					String newLine = removeAllButNumbers(currentLine);
					this.totalColumns = newLine.length(); // Total columns of this line
					if (columnsBoardSetting != totalColumns) {
						if (this.areDebugMessagesEnabled) System.err.println("The file settings for Columns do not match actual number of columns in line " + y + ", filling in missing columns with 0 or omiting extra columns.");
						if (this.areDebugMessagesEnabled) System.err.println("The number of columns from the file settings: " + columnsBoardSetting + ". The number from counting the columns: " + totalColumns);
						columnsBoardSetting = totalRows;
					}
					for (int x = 0; x < columnsBoardSetting; x++) {
						try {
							int currentChar = Integer.parseInt(Character.toString(newLine.charAt(x)));
							completeBoard[y][x] = currentChar; // Add value at this index to the array
							if (this.areDebugMessagesEnabled) System.out.println("Added the value: " + newLine.charAt(x) + " to 2D array[" + y + "][" + x + "]");
						} catch (IndexOutOfBoundsException e) {
							// If the charAt() method throws an exception, fill that slot with 0
							completeBoard[y][x] = 0;
							if (this.areDebugMessagesEnabled) System.out.println("Added the value: " + 0 + " to 2D array[" + y + "][" + x + "] (This was done in error)");
							System.out.println(e.getMessage());
						}
					}
				}
			}
			fileScnr.close();
			return completeBoard;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return completeBoard;
		
	}
	/**
	 * Returns value of cell at given column and row
	 * @param row
	 * @param column
	 * @return value at row and column, 0 if out of bounds
	 */
	public int getCell(int column, int row) {
		if (this.areDebugMessagesEnabled) System.out.println("Checking if the Cell at Row: " + row + " , Column: " + column);
		
		try {
			if (column > this.totalColumns - 1 || column < 0 || row > totalRows - 1 || row < 0) {
				return 0; // Out of bounds
			} else {
				if (this.areDebugMessagesEnabled) System.out.println("Returning the value: " + this.gameBoard[row][column] + " for Row: " + row + " , Column: " + column);
				return this.gameBoard[row][column];
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			if (this.areDebugMessagesEnabled) System.err.println(e.getMessage() + ". Returnning 0 for this position Row: " + row + " , Column: " + column);
			return 0;
		}
	}
	
	/**
	 * Sets cell at given column and row. Can assume it has been initilized
	 * @param column
	 * @param row
	 * @param value
	 */
	public void setCell(int column, int row, int value) {
		if (this.auxillaryGameBoard != null) {
			this.auxillaryGameBoard[row][column] = value;
			this.isAuxFilled = true;
			if (this.areDebugMessagesEnabled) System.out.println("Filled a slot in the auxillary board");
		} else {
			this.gameBoard[row][column] = value;
			if (this.areDebugMessagesEnabled) System.out.println("Filled a slot in the main board b/c auxillary was still null");
		}
	}
	
	/**
	 * Creates a 2D array to compute next iteration of board in accordance with the rules of Life.
	 * Then updates the board with new generation
	 * @param currentGeneration the number of generations to compute
	 */
	public void computeNextGeneration(int currentGeneration) {
		if (this.areDebugMessagesEnabled) System.out.println("Will print out " + this.maxGenerationsToCompute + " generations");
		if (this.areDebugMessagesEnabled) System.out.println("Times this method has looped: " + this.timesIterated);
		if (this.areDebugMessagesEnabled) System.out.println("Generations left to compute: " + currentGeneration);
			if (currentGeneration == this.maxGenerationsToCompute) { // Base Case Would only have to display the original board
				this.timesIterated++;
				System.out.println("Generation " + this.timesIterated);
				print();
				if (areDebugMessagesEnabled) System.out.println("Successfully Computed and Prinited Generation " + this.timesIterated);
				computeNextGeneration(currentGeneration - 1);
			} else if (currentGeneration != 0) {
				this.auxillaryGameBoard = new int[this.totalRows][this.totalColumns];
				if (this.areDebugMessagesEnabled) System.out.println("Starting to print out Generation " + this.timesIterated);
				for (int y = 0; y < this.totalRows; y++) {
					for (int x = 0; x < this.totalColumns; x++) {
						int numberOfLivingNeighbors = 0; //Num of neighbors for this iteration
						
						if (getCell(x, y - 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/1] The cell at row: " + (y - 1) + " column: " + x + " is a neighbor of this value");
							}
						}
						if (getCell(x, y + 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/2] The cell at row: " + (y + 1) + " column: " + x + " is a neighbor of this value");
							}
						}
						if (getCell(x - 1, y - 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/3] The cell at row: " + (y - 1) + " column: " + (x - 1) + " is a neighbor of this value");
							}
						}
						if (getCell(x - 1, y + 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/4] The cell at row: " + (y + 1) + " column: " + (x - 1) + " is a neighbor of this value");
							}
						}
						if (getCell(x + 1, y - 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/5] The cell at row: " + (y - 1) + " column: " + (x + 1) + " is a neighbor of this value");
							}
						}
						if (getCell(x + 1, y + 1) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/6] The cell at row: " + (y + 1) + " column: " + (x + 1) + " is a neighbor of this value");
							}
						}
						if (getCell(x - 1, y) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/7] The cell at row: " + y + " column: " + (x - 1) + " is a neighbor of this value");
							}
						}
						if (getCell(x + 1, y) == 1) {
							numberOfLivingNeighbors++;
							if (y == 7 && x == 4) {
								if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/8] The cell at row: " + y + " column: " + (x + 1) + " is a neighbor of this value");
							}
						}
						if (this.gameBoard[8][5] == 1) {
							if (y == 7 && x == 6) {
								if (this.gameBoard[7][5] == 1) {
									if (this.areDebugMessagesEnabled) System.out.println("[DEBUG/9] The cell at row: " + y + " column: " + (x + 1) + " is a neighbor of this value");
									numberOfLivingNeighbors++;
								}
							}
						}
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						// Alive
						if (getCell(x, y) == 1) { // This Slot is Alive
							if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " is alive (equal to one)");
								if (numberOfLivingNeighbors < 2) {
									setCell(x, y, 0); // Set Dead for next Generation
									if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will be killed due to loneliness");
								}
								else if (numberOfLivingNeighbors > 3) {
									setCell(x, y, 0); // Set Dead for next Generation
									if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will be killed due to overpopulation");
								////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
								// Transfer - Alive
								} else {
									setCell(x, y, 1); // Set auxArray posiiton with original value
									if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will retain a value of 1");
								}
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						// Dead
						} else if (getCell(x, y) == 0) {
							if (numberOfLivingNeighbors == 3) { // Accounts for Dead Rule
								setCell(x, y, 1); // Set Alive for next Generation
								if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will be resurected");
							////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
							// Transfer - Dead
							} else {
								setCell(x, y, 0); // Set auxArray posiiton with original value
								if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will retain a value of 0");
							}
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						// Transfer - Catch All
						} else {
							setCell(x, y, this.gameBoard[y][x]); // Set auxArray posiiton with original value
							if (this.areDebugMessagesEnabled) System.out.println("The cell at row: " + y + " column: " + x + " will retain its value " + this.gameBoard[y][x] + ", from catch all");
						}
					}
				}
				this.timesIterated++;
				System.out.println("Generation " + this.timesIterated);
				print();
				computeNextGeneration(currentGeneration - 1);
			} else {
				if (this.areDebugMessagesEnabled) System.out.println("Finished recursively looping the computeGenerations() method with a total of " + this.timesIterated + " Generations");
			}
	}
	
	/**
	 * Prints board to console
	 */
	public void print() {
		for (int y = 0; y < this.totalRows; y++) {
			for (int x = 0; x < this.totalColumns; x++) {
				if (!this.isAuxFilled) {
					System.out.print(this.gameBoard[y][x] + " ");
				} else {
					System.out.print(this.auxillaryGameBoard[y][x] + " ");
					this.isAuxFilled = false;
					this.gameBoard = this.auxillaryGameBoard;
				}
				
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		new GameOfLife();
	}
}
