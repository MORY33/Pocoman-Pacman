package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameBoard extends JPanel {
    private static final int SIZE = 19;
    private int yellowSquareCounter;
    private Position pacmanPos;
    private Ghost ghost1;
    private FollowGhost ghost2;
    private Ghost ghost3;


    private static final int MAX_RESETS = 2;


    private short[][] board;
    private int moveDirection; // 0: brak ruchu, 1: gora, 2: dol, 3: lewo, 4: prawo
    private int resetCount;


    public GameBoard() {
        setPreferredSize(new Dimension(800, 800));
        board = new short[SIZE][SIZE];
        yellowSquareCounter = 0;
        initBoard();
        pacmanPos = new Position(SIZE / 2 + 6, SIZE / 2);
        moveDirection = 4;
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

        setFocusTraversalKeysEnabled(false);

        Timer movementTimer = new Timer(200, (e) -> movePacman());
        movementTimer.setRepeats(true);
        movementTimer.start();

        ghost1 = new Ghost(new Position(SIZE / 2, SIZE / 2 + 1));
        ghost2 = new FollowGhost(new Position(SIZE / 2, SIZE / 2), this);
        ghost3 = new Ghost(new Position(SIZE / 2, SIZE / 2 - 1));
        Thread ghostThread1 = new Thread(ghost1);
        Thread ghostThread2 = new Thread(ghost2);
        Thread ghostThread3 = new Thread(ghost3);
        ghostThread1.start();
        ghostThread2.start();
        ghostThread3.start();
    }

    public boolean isWalkable(int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return false;
        }
        short cell = board[row][col];
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

        board[oldRow][oldCol] -= 50;
        board[newRow][newCol] += 50;
    }

    public void resetGhostPosition() {
        board[ghost1.getPosition().getRow()][ghost1.getPosition().getCol()] -= 50;
        ghost1.setPosition(new Position(SIZE / 2, SIZE / 2 + 1));
        board[ghost1.getPosition().getRow()][ghost1.getPosition().getCol()] += 50;

        board[ghost2.getPosition().getRow()][ghost2.getPosition().getCol()] -= 50;
        ghost2.setPosition(new Position(SIZE / 2, SIZE / 2));
        board[ghost2.getPosition().getRow()][ghost2.getPosition().getCol()] += 50;

        board[ghost3.getPosition().getRow()][ghost3.getPosition().getCol()] -= 50;
        ghost3.setPosition(new Position(SIZE / 2, SIZE / 2 - 1));
        board[ghost3.getPosition().getRow()][ghost3.getPosition().getCol()] += 50;
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
                    dRow = -1;
                    break;
                case 2:
                    dRow = 1;
                    break;
                case 3:
                    dCol = -1;
                    break;
                case 4:
                    dCol = 1;
                    break;
            }

            int newRow = getPosition().getRow() + dRow;
            int newCol = getPosition().getCol() + dCol;


            if (isValidGhostMove(newRow, newCol)) {

                board[getPosition().getRow()][getPosition().getCol()] -= 50;

                setPosition(new Position(newRow, newCol));
                board[getPosition().getRow()][getPosition().getCol()] += 50;

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
                        dRow = -1;
                        break;
                    case 2:
                        dRow = 1;
                        break;
                    case 3:
                        dCol = -1;
                        break;
                    case 4:
                        dCol = 1;
                        break;
                }

                int newRow = getPosition().getRow() + dRow;
                int newCol = getPosition().getCol() + dCol;
                if (isValidGhostMove(newRow, newCol)) {
                    validDirections.add(i);
                }
            }

            if (validDirections.isEmpty()) {
                return 0;
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
                    Thread.sleep(500);
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

            if (Math.abs(rowDiff) + Math.abs(colDiff) <= 1 && gameBoard.isWalkable(playerRow, playerCol)) {
                return new Position(playerRow, playerCol);
            }

            if (colDiff > 0) {
                if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1);
                } else if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol);
                } else if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol);
                } else {
                    return new Position(ghostRow, ghostCol - 1);
                }
            } else if (colDiff < 0) {
                if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1);
                } else if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol);
                } else if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol);
                } else {
                    return new Position(ghostRow, ghostCol + 1);
                }
            } else if (rowDiff > 0) {
                if (gameBoard.isWalkable(ghostRow + 1, ghostCol)) {
                    return new Position(ghostRow + 1, ghostCol);
                } else if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1);
                } else if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1);
                } else {
                    return new Position(ghostRow - 1, ghostCol);
                }
            } else if (rowDiff < 0) {
                if (gameBoard.isWalkable(ghostRow - 1, ghostCol)) {
                    return new Position(ghostRow - 1, ghostCol);
                } else if (gameBoard.isWalkable(ghostRow, ghostCol - 1)) {
                    return new Position(ghostRow, ghostCol - 1);
                } else if (gameBoard.isWalkable(ghostRow, ghostCol + 1)) {
                    return new Position(ghostRow, ghostCol + 1);
                } else {
                    return new Position(ghostRow + 1, ghostCol);
                }
            }

            return getPosition();
        }






        @Override
        public void move() {
            pacmanPos = gameBoard.getPacmanPosition();

            Position nextMove = getNextMoveTowardsPlayer();

            gameBoard.updateGhostPosition(getPosition(), nextMove);
            setPosition(nextMove);

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
                    Thread.sleep(500);
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

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = predefinedBoard[i][j];
            }
        }

        board[SIZE / 2 + 6][SIZE / 2] += 15;
        board[SIZE / 2][SIZE / 2 + 1] += 50;  //pierwszy duch
        board[SIZE / 2][SIZE / 2] += 50; //drugi duch
        board[SIZE / 2][SIZE / 2 - 1] += 50;  //trzeci duch



    }


    public void movePacman() {
        int dRow = 0;
        int dCol = 0;
        switch (moveDirection) {
            case 1:
                dRow = -1;
                break;
            case 2:
                dRow = 1;
                break;
            case 3:
                dCol = -1;
                break;
            case 4:
                dCol = 1;
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

                    JButton tryAgainButton = new JButton("Try Again");
                    tryAgainButton.addActionListener(e -> {
                        dialog.dispose();
                        resetCount = 1;
                        yellowSquareCounter = 0;
                        resetGhostPosition();
                        initBoard();
                        repaint();
                        resetGame();
                        repaint();
                    });

                    JButton quitGameButton = new JButton("Quit Game");
                    quitGameButton.addActionListener(e -> {
                        System.exit(0);
                    });

                    JPanel buttonPanel = new JPanel();
                    buttonPanel.add(tryAgainButton);
                    buttonPanel.add(quitGameButton);
                    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                    dialog.pack();
                    dialog.setLocationRelativeTo(this);
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
                handlePacmanGhostCollision();
                return;
            }
            board[pacmanPos.getRow()][pacmanPos.getCol()] += 15;
            repaint();
        }
    }
    private void handlePacmanGhostCollision() {
        resetCount++;
        if (resetCount > MAX_RESETS) {
            finishGame();
        } else {
            resetGame();
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
                moveDirection = 1;
                break;
            case KeyEvent.VK_DOWN:
                moveDirection = 2;
                break;
            case KeyEvent.VK_LEFT:
                moveDirection = 3;
                break;
            case KeyEvent.VK_RIGHT:
                moveDirection = 4;
                break;
        }
    }
    private void resetGame() {
        pacmanPos = new Position(SIZE / 2 + 6,SIZE / 2);
        moveDirection = 4;
    }


    private void finishGame() {
        JDialog dialog = new JDialog((Frame) null, "Game Over", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JOptionPane.showMessageDialog(dialog, "YOU LOSE", "Game Over", JOptionPane.INFORMATION_MESSAGE);

        JButton tryAgainButton = new JButton("Try Again");
        tryAgainButton.addActionListener(e -> {
            dialog.dispose();
            resetCount = 1;
            yellowSquareCounter = 0;
            resetGhostPosition();
            initBoard();
            repaint();
            resetGame();
        });

        JButton quitGameButton = new JButton("Quit Game");
        quitGameButton.addActionListener(e -> {
            System.exit(0);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tryAgainButton);
        buttonPanel.add(quitGameButton);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void endGame() {
        System.out.println("Game Over");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellSize = Math.min(getWidth(), getHeight());
        cellSize = cellSize - (cellSize % SIZE);

        int cellWidth = cellSize / SIZE;
        int cellHeight = cellSize / SIZE;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                switch (board[i][j]) {
                    case 0:
                        g.setColor(Color.BLACK);
                        break;
                    case 11:
                        g.setColor(Color.WHITE);
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