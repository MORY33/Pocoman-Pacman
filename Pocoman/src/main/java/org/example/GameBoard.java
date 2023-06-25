package org.example;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;


public class GameBoard extends JPanel {
    private static final int SIZE = 19; // Updated size
    private int yellowSquareCounter;
    private Position pacmanPos;
    private Ghost ghost1;
    private FollowGhost ghost2;
    private Ghost ghost3;


    //    private static final int MAX_RESETS = 3; // Maximum number of game resets
    private static final int MAX_RESETS = 2; // Maximum number of game resets


    private short[][] board;
    private int moveDirection; // 0: no movement, 1: up, 2: down, 3: left, 4: right
    private int resetCount; // Counter for game resets


    public GameBoard() {
        setPreferredSize(new Dimension(800, 800));
        board = new short[SIZE][SIZE];
        yellowSquareCounter = 0;
        initBoard();
        pacmanPos = new Position(SIZE / 2 + 6, SIZE / 2);
        moveDirection = 4; // Start Pacman moving to the right
        resetCount = 0;

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                        keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    handleArrowKeys(keyCode);
                }
            }
        });

        setFocusTraversalKeysEnabled(false); // To capture arrow keys

        // Start Pacman's constant movement
        Timer movementTimer = new Timer(200, (e) -> movePacman());
        movementTimer.setRepeats(true);
        movementTimer.start();

        ghost1 = new Ghost(new Position(SIZE / 2, SIZE / 2 + 1)); // Create a new Ghost object
        ghost2 = new FollowGhost(new Position(SIZE / 2, SIZE / 2), this); // Create a new Ghost object
        ghost3 = new Ghost(new Position(SIZE / 2, SIZE / 2 - 1)); // Create a new Ghost object
        Thread ghostThread1 = new Thread(ghost1); // Create a new thread for the ghost
        Thread ghostThread2 = new Thread(ghost2); // Create a new thread for the ghost
        Thread ghostThread3 = new Thread(ghost3); // Create a new thread for the ghost
        ghostThread1.start(); // Start the ghost thread
        ghostThread2.start(); // Start the ghost thread
        ghostThread3.start(); // Start the ghost thread
    }

    public boolean isWalkable(int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            // The cell is out of bounds
            return false;
        }

        short cell = board[row][col];

        // Check if the cell is a wall or any other obstacle
        // Assuming that a wall is represented by the character '#'
        return cell != 0;
    }

    public Position getPacmanPosition() {
        return pacmanPos;
    }

    public void updateGhostPosition(Position oldPosition, Position newPosition) {
        int oldRow = oldPosition.getRow();
        int oldCol = oldPosition.getCol();
        int newRow = newPosition.getRow();
        int newCol = newPosition.getCol();

        // Update the game board to reflect the new position
        board[oldRow][oldCol] -= 50; // Assuming EMPTY_SPACE is a constant representing an empty space on the board
        board[newRow][newCol] += 50; // Assuming GHOST_SYMBOL is a constant representing the ghost symbol on the board
    }

    public void resetGhostPosition() {
        board[ghost1.getPosition().getRow()][ghost1.getPosition().getCol()] -= 50; // Clear current ghost position
        ghost1.setPosition(new Position(SIZE / 2, SIZE / 2 + 1)); // Reset ghost position
        board[ghost1.getPosition().getRow()][ghost1.getPosition().getCol()] += 50; // Set new ghost position

        board[ghost2.getPosition().getRow()][ghost2.getPosition().getCol()] -= 50; // Clear current ghost position
        ghost2.setPosition(new Position(SIZE / 2, SIZE / 2)); // Reset ghost position
        board[ghost2.getPosition().getRow()][ghost2.getPosition().getCol()] += 50; // Set new ghost position

        board[ghost3.getPosition().getRow()][ghost3.getPosition().getCol()] -= 50; // Clear current ghost position
        ghost3.setPosition(new Position(SIZE / 2, SIZE / 2 - 1)); // Reset ghost position
        board[ghost3.getPosition().getRow()][ghost3.getPosition().getCol()] += 50; // Set new ghost position
        repaint();
    }

    private class Ghost extends GameCharacter {
        private Random random;

        public Ghost(Position position) {
            super(position);
            random = new Random();
        }

        @Override
        public void move() {
            int direction = generateValidDirection();
            int dRow = 0;
            int dCol = 0;
            switch (direction) {
                case 1:
                    dRow = -1; // Move up
                    break;
                case 2:
                    dRow = 1; // Move down
                    break;
                case 3:
                    dCol = -1; // Move left
                    break;
                case 4:
                    dCol = 1; // Move right
                    break;
            }

            int newRow = getPosition().getRow() + dRow;
            int newCol = getPosition().getCol() + dCol;


            if (isValidGhostMove(newRow, newCol)) {
//                System.out.println(board[getPosition().getRow()][getPosition().getCol()]);

                board[getPosition().getRow()][getPosition().getCol()] -= 50; // Clear previous ghost position
//                System.out.println(board[getPosition().getRow()][getPosition().getCol()]);

                setPosition(new Position(newRow, newCol)); // Update ghost position
                board[getPosition().getRow()][getPosition().getCol()] += 50; // Set new ghost position

                if (board[getPosition().getRow()][getPosition().getCol()]==76 || board[getPosition().getRow()][getPosition().getCol()]==66) {
                    board[getPosition().getRow()][getPosition().getCol()]-=15;
                    handlePacmanGhostCollision();
                }

                repaint();
            }

        }

        private int generateValidDirection() {
            List<Integer> validDirections = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                int dRow = 0;
                int dCol = 0;
                switch (i) {
                    case 1:
                        dRow = -1; // Move up
                        break;
                    case 2:
                        dRow = 1; // Move down
                        break;
                    case 3:
                        dCol = -1; // Move left
                        break;
                    case 4:
                        dCol = 1; // Move right
                        break;
                }

                int newRow = getPosition().getRow() + dRow;
                int newCol = getPosition().getCol() + dCol;
                if (isValidGhostMove(newRow, newCol)) {
                    validDirections.add(i);
                }
            }

            if (validDirections.isEmpty()) {
                return 0; // No valid directions available
            } else {
                int randomIndex = random.nextInt(validDirections.size());
                return validDirections.get(randomIndex);
            }
        }

        @Override
        public void run() {
            while (true) {
                move();
                try {
                    Thread.sleep(500); // Wait for 500 milliseconds between each movement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FollowGhost extends GameCharacter {
        private Position pacmanPos;
        private GameBoard gameBoard;

        public FollowGhost(Position position, GameBoard gameBoard) {
            super(position);
            this.gameBoard = gameBoard;
            this.pacmanPos = gameBoard.getPacmanPosition();
        }

        private int calculateManhattanDistance(Position pos1, Position pos2) {
            int rowDiff = Math.abs(pos1.getRow() - pos2.getRow());
            int colDiff = Math.abs(pos1.getCol() - pos2.getCol());
            return rowDiff + colDiff;
        }

        private Position getNextMoveTowardsPlayer() {
            int ghostRow = getPosition().getRow();
            int ghostCol = getPosition().getCol();

            int playerRow = pacmanPos.getRow();
            int playerCol = pacmanPos.getCol();

            int rowDiff = playerRow - ghostRow;
            int colDiff = playerCol - ghostCol;

            // Check if the ghost can move directly towards the player
            if (Math.abs(rowDiff) + Math.abs(colDiff) <= 1 && gameBoard.isWalkable(playerRow, playerCol)) {
                return new Position(playerRow, playerCol);
            }

            // If the ghost is blocked by a wall, follow the right-hand rule to go around it
            if (colDiff > 0) {
                // Try moving right while keeping the right hand on the wall
                if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1); // Move right
                } else if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol); // Move up
                } else if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol); // Move down
                } else {
                    return new Position(ghostRow, ghostCol - 1); // Move left
                }
            } else if (colDiff < 0) {
                // Try moving left while keeping the right hand on the wall
                if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1); // Move left
                } else if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol); // Move down
                } else if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol); // Move up
                } else {
                    return new Position(ghostRow, ghostCol + 1); // Move right
                }
            } else if (rowDiff > 0) {
                // Try moving down while keeping the right hand on the wall
                if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol); // Move down
                } else if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1); // Move right
                } else if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1); // Move left
                } else {
                    return new Position(ghostRow - 1, ghostCol); // Move up
                }
            } else if (rowDiff < 0) {
                // Try moving up while keeping the right hand on the wall
                if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol); // Move up
                } else if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1); // Move left
                } else if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1); // Move right
                } else {
                    return new Position(ghostRow + 1, ghostCol); // Move down
                }
            }

            // If no valid move is found, stay in the current position
            return getPosition();
        }






        @Override
        public void move() {
            pacmanPos = gameBoard.getPacmanPosition(); // Update pacmanPos

            Position nextMove = getNextMoveTowardsPlayer();

            gameBoard.updateGhostPosition(getPosition(), nextMove); // Update ghost's position on the game board
            setPosition(nextMove); // Update ghost's position

            if (board[getPosition().getRow()][getPosition().getCol()] == 76 || board[getPosition().getRow()][getPosition().getCol()] == 66) {
                board[getPosition().getRow()][getPosition().getCol()] -= 15;
                handlePacmanGhostCollision();
            }
        }



        @Override
        public void run() {
            while (true) {
                move();
                try {
                    Thread.sleep(500); // Wait for 500 milliseconds between each movement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void initBoard() {
        //0 - sciana
        //1 - puste pole
        //+10 - coin
        //+15 - pacman
        //+50 - ghost
        short[][] predefinedBoard = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 11, 11, 11, 11, 11, 11, 11, 11, 0, 11, 11, 11, 11, 11, 11, 11, 11, 0},
                {0, 11, 0, 0, 11, 0, 0, 0, 11, 0, 11, 0, 0, 0, 11, 0, 0, 11, 0},
                {0, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0},
                {0, 11, 0, 0, 11, 0, 11, 0, 0, 0, 0, 0, 11, 0, 11, 0, 0, 11, 0},
                {0, 11, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 11, 0},
                {0, 0, 0, 0, 11, 0, 0, 0, 11, 0, 11, 0, 0, 0, 11, 0, 0, 0, 0},
                {1, 1, 1, 0, 11, 0, 11, 11, 11, 11, 11, 11, 11, 0, 11, 0, 1, 1, 1},
                {0, 0, 0, 0, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 0, 0, 0, 0},
                {11, 11, 11, 11, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 11, 11, 11, 11},
                {0, 0, 0, 0, 11, 11, 11, 0, 0, 0, 0, 0, 11, 11, 11, 0, 0, 0, 0},
                {1, 1, 1, 0, 11, 0, 11, 11, 11, 11, 11, 11, 11, 0, 11, 0, 1, 1, 1},
                {0, 0, 0, 0, 11, 0, 0, 0, 11, 0, 11, 0, 0, 0, 11, 0, 0, 0, 0},
                {0, 11, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 0, 11, 11, 11, 11, 0},
                {0, 11, 0, 0, 11, 0, 11, 0, 0, 0, 0, 0, 11, 0, 11, 0, 0, 11, 0},
                {0, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0},
                {0, 11, 0, 0, 11, 0, 0, 0, 11, 0, 11, 0, 0, 0, 11, 0, 0, 11, 0},
                {0, 11, 11, 11, 11, 11, 11, 11, 11, 0, 11, 11, 11, 11, 11, 11, 11, 11, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

        };

        // Copy the predefined board into the actual game board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = predefinedBoard[i][j];
            }
        }

        // Place Pacman ('p') at the center of the board
//        board[SIZE / 2][SIZE / 2] = 'p';
        board[SIZE / 2 + 6][SIZE / 2] += 15;
        // Place ghosts ('g') at specific positions
        board[SIZE / 2][SIZE / 2 + 1] += 50;  // First ghost
        board[SIZE / 2][SIZE / 2] += 50;  // Second ghost
        board[SIZE / 2][SIZE / 2 - 1] += 50;  // third ghost



    }


    public void movePacman() {
        int dRow = 0;
        int dCol = 0;
        switch (moveDirection) {
            case 1:
                dRow = -1; // Move up
                break;
            case 2:
                dRow = 1; // Move down
                break;
            case 3:
                dCol = -1; // Move left
                break;
            case 4:
                dCol = 1; // Move right
                break;
        }

        int newRow = pacmanPos.getRow() + dRow;
        int newCol = pacmanPos.getCol() + dCol;
        if (isValidMove(newRow, newCol)) {
            if(board[pacmanPos.getRow()][pacmanPos.getCol()] == 26){
                yellowSquareCounter++;
                if(yellowSquareCounter==100) {
                    JDialog dialog = new JDialog((Frame) null, "Game Over", true);
                    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                    JOptionPane.showMessageDialog(dialog, "YOU WIN", "Game Over", JOptionPane.INFORMATION_MESSAGE);

                    // Create the "Try Again" button
                    JButton tryAgainButton = new JButton("Try Again");
                    tryAgainButton.addActionListener(e -> {
                        dialog.dispose(); // Close the custom JDialog
                        resetCount = 1;
                        yellowSquareCounter = 0;
                        resetGhostPosition();
                        initBoard();
                        repaint();
                        resetGame(); // Restart the game
                        repaint();
                    });

                    // Create the "Quit Game" button
                    JButton quitGameButton = new JButton("Quit Game");
                    quitGameButton.addActionListener(e -> {
                        System.exit(0); // Exit the application
                    });

                    // Add the buttons to the custom JDialog
                    JPanel buttonPanel = new JPanel();
                    buttonPanel.add(tryAgainButton);
                    buttonPanel.add(quitGameButton);
                    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                    dialog.pack();
                    dialog.setLocationRelativeTo(this); // Center the dialog on the frame
                    dialog.setVisible(true);
                }
                board[pacmanPos.getRow()][pacmanPos.getCol()] -= 25;
            }
            else if(board[pacmanPos.getRow()][pacmanPos.getCol()] == 16){
                board[pacmanPos.getRow()][pacmanPos.getCol()] -= 15;
            }
            pacmanPos = new Position(newRow, newCol);


            if (board[pacmanPos.getRow()][pacmanPos.getCol()] > 50) {
                System.out.println(board[pacmanPos.getRow()][pacmanPos.getCol()]);
                handlePacmanGhostCollision(); // Handle Pacman touching a ghost
                return;
            }
            board[pacmanPos.getRow()][pacmanPos.getCol()] += 15;
            repaint();
        }
    }
    private void handlePacmanGhostCollision() {
        resetCount++;
        if (resetCount > MAX_RESETS) {
            finishGame(); // Finish the game if Pacman resets more than MAX_RESETS times
        } else {
            resetGame(); // Reset the game
        }
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] != 0;
    }

    private boolean isValidGhostMove(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] != 0 && board[row][col] <50;
    }

    private void handleArrowKeys(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                moveDirection = 1; // Move up
                break;
            case KeyEvent.VK_DOWN:
                moveDirection = 2; // Move down
                break;
            case KeyEvent.VK_LEFT:
                moveDirection = 3; // Move left
                break;
            case KeyEvent.VK_RIGHT:
                moveDirection = 4; // Move right
                break;
        }
    }
    private void resetGame() {
        // Reset the game state here
        // For example, clear the board, reposition Pacman and ghosts, update the score, etc.
        pacmanPos = new Position(SIZE / 2 + 6,SIZE / 2);
        moveDirection = 4;
//            repaint();
    }


    private void finishGame() {
        // Finish the game here
        // For example, display "YOU LOSE" message or perform any other end-game actions

        // Create a custom JDialog to display the message, "Try Again," and "Quit Game" buttons
        JDialog dialog = new JDialog((Frame) null, "Game Over", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JOptionPane.showMessageDialog(dialog, "YOU LOSE", "Game Over", JOptionPane.INFORMATION_MESSAGE);

        // Create the "Try Again" button
        JButton tryAgainButton = new JButton("Try Again");
        tryAgainButton.addActionListener(e -> {
            dialog.dispose(); // Close the custom JDialog
            resetCount = 1;
            yellowSquareCounter = 0;
            resetGhostPosition();
            initBoard();
            repaint();
            resetGame(); // Restart the game
        });

        // Create the "Quit Game" button
        JButton quitGameButton = new JButton("Quit Game");
        quitGameButton.addActionListener(e -> {
            System.exit(0); // Exit the application
        });

        // Add the buttons to the custom JDialog
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tryAgainButton);
        buttonPanel.add(quitGameButton);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this); // Center the dialog on the frame
        dialog.setVisible(true);
    }
    private void endGame() {
        // Perform end game logic here
        // For example, display a message or reset the game
        System.out.println("Game Over");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Get the smallest size and make it even for proper grid division
        int cellSize = Math.min(getWidth(), getHeight());
        cellSize = cellSize - (cellSize % SIZE);

        int cellWidth = cellSize / SIZE;
        int cellHeight = cellSize / SIZE;

        // Set the background color to white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Paint the cells and ghosts
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                switch (board[i][j]) {
                    case 0:
                        g.setColor(Color.BLACK);
                        break;
                    case 11:
                        g.setColor(Color.WHITE);
                        // Draw yellow dot
                        int dotSize = cellWidth / 4;
                        int dotOffsetX = j * cellWidth + (cellWidth / 2) - (dotSize / 2);
                        int dotOffsetY = i * cellHeight + (cellHeight / 2) - (dotSize / 2);
                        g.setColor(Color.YELLOW);
                        g.fillOval(dotOffsetX, dotOffsetY, dotSize, dotSize);
                        break;
                    case 26, 16:
                        g.setColor(Color.RED);
                        break;
                    case 61, 51:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.WHITE);
                        break;

                    default:
                        System.out.println(board[i][j]);
                        break;
                }
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Yellow Squares: " + yellowSquareCounter, 10, 30);
    }


}