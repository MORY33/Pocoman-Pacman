package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maze {
    private int[][] grid;
    private int width;
    private int height;
    private Random random;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new int[height][width];
        random = new Random();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isWall(int row, int col) {
        return grid[row][col] == 1;
    }

    public void generate() {
        // Initialize the maze with walls
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = 1;
            }
        }

        // Start recursive backtracking to create paths
        backtrack(1, 1);

        // Set entrance and exit
        grid[0][1] = 0;
        grid[height - 1][width - 2] = 0;
    }

    private void backtrack(int row, int col) {
        grid[row][col] = 0;

        List<Direction> directions = new ArrayList<>(List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));
        java.util.Collections.shuffle(directions);

        for (Direction direction : directions) {
            int newRow = row + direction.getDy() * 2;
            int newCol = col + direction.getDx() * 2;

            if (isValidCell(newRow, newCol)) {
                grid[row + direction.getDy()][col + direction.getDx()] = 0;
                backtrack(newRow, newCol);
            }
        }
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width && grid[row][col] == 1;
    }
}

enum Direction {
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}