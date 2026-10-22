package com.sudoku;

/**
 * Pure validation logic for Sudoku rules.
 *
 * OOP Concept: Encapsulation + Single Responsibility
 * All rule-checking is isolated here so other classes stay clean.
 */
public class SudokuValidator {

    private SudokuValidator() { /* utility class — no instantiation */ }

    // ── Core validators ──────────────────────────────────────────────────────

    /** Returns true if placing `value` at (row,col) in `grid` violates no rules. */
    public static boolean isValidPlacement(int[][] grid, int row, int col, int value) {
        if (value == 0) return true;   // erasing is always valid placement-wise
        return !conflictsInRow(grid, row, col, value)
            && !conflictsInCol(grid, row, col, value)
            && !conflictsInBox(grid, row, col, value);
    }

    public static boolean conflictsInRow(int[][] grid, int row, int col, int value) {
        for (int c = 0; c < SudokuBoard.SIZE; c++) {
            if (c != col && grid[row][c] == value) return true;
        }
        return false;
    }

    public static boolean conflictsInCol(int[][] grid, int row, int col, int value) {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            if (r != row && grid[r][col] == value) return true;
        }
        return false;
    }

    public static boolean conflictsInBox(int[][] grid, int row, int col, int value) {
        int boxRow = (row / SudokuBoard.BOX_SIZE) * SudokuBoard.BOX_SIZE;
        int boxCol = (col / SudokuBoard.BOX_SIZE) * SudokuBoard.BOX_SIZE;
        for (int r = boxRow; r < boxRow + SudokuBoard.BOX_SIZE; r++) {
            for (int c = boxCol; c < boxCol + SudokuBoard.BOX_SIZE; c++) {
                if ((r != row || c != col) && grid[r][c] == value) return true;
            }
        }
        return false;
    }

    /**
     * Validates that the entire completed grid satisfies all Sudoku rules.
     * Used after the puzzle is declared complete.
     */
    public static boolean isValidCompleteGrid(int[][] grid) {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                int v = grid[r][c];
                if (v < 1 || v > 9) return false;
                // Temporarily clear to avoid self-conflict
                grid[r][c] = 0;
                if (!isValidPlacement(grid, r, c, v)) {
                    grid[r][c] = v;
                    return false;
                }
                grid[r][c] = v;
            }
        }
        return true;
    }

    /** Validates that an in-progress move does not conflict with existing cells. */
    public static boolean hasConflict(int[][] currentBoard, int row, int col, int value) {
        if (value == 0) return false;
        int saved = currentBoard[row][col];
        currentBoard[row][col] = 0;
        boolean conflict = !isValidPlacement(currentBoard, row, col, value);
        currentBoard[row][col] = saved;
        return conflict;
    }
}
