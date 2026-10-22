package com.sudoku;

/**
 * Solves a Sudoku puzzle using recursive backtracking.
 *
 * OOP Concept: Encapsulation + Single Responsibility
 * The solver keeps its internal working grid private and exposes only
 * the solve() method.
 */
public class SudokuSolver {

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Solves the given puzzle in-place (modifies a copy, returns it).
     * @param puzzle a 9×9 grid (0 = empty).
     * @return solved grid, or null if unsolvable.
     */
    public int[][] solve(int[][] puzzle) {
        int[][] grid = deepCopy(puzzle);
        return backtrack(grid) ? grid : null;
    }

    /**
     * Checks whether the puzzle has exactly one solution.
     * Useful during puzzle generation to guarantee uniqueness.
     */
    public int countSolutions(int[][] puzzle) {
        int[][] grid = deepCopy(puzzle);
        int[] count = {0};
        countBacktrack(grid, count);
        return count[0];
    }

    // ── Private recursive solver ─────────────────────────────────────────────

    private boolean backtrack(int[][] grid) {
        int[] empty = findEmpty(grid);
        if (empty == null) return true;   // no empty cell → solved!

        int row = empty[0];
        int col = empty[1];

        for (int v = 1; v <= SudokuBoard.SIZE; v++) {
            if (SudokuValidator.isValidPlacement(grid, row, col, v)) {
                grid[row][col] = v;
                if (backtrack(grid)) return true;
                grid[row][col] = 0;
            }
        }
        return false;
    }

    private void countBacktrack(int[][] grid, int[] count) {
        if (count[0] > 1) return;   // early exit — more than one solution

        int[] empty = findEmpty(grid);
        if (empty == null) {
            count[0]++;
            return;
        }

        int row = empty[0];
        int col = empty[1];

        for (int v = 1; v <= SudokuBoard.SIZE; v++) {
            if (SudokuValidator.isValidPlacement(grid, row, col, v)) {
                grid[row][col] = v;
                countBacktrack(grid, count);
                grid[row][col] = 0;
            }
        }
    }

    /** Finds the first empty cell (value == 0) scanning left-to-right, top-down. */
    private int[] findEmpty(int[][] grid) {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                if (grid[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) copy[i] = src[i].clone();
        return copy;
    }
}
