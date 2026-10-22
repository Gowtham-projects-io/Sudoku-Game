package com.sudoku;

import org.json.JSONArray;

/**
 * Manages the 9×9 grid of SudokuCell objects — demonstrates COMPOSITION.
 *
 * OOP Concept: Composition
 * SudokuBoard IS composed of 81 SudokuCell objects.  It delegates all
 * single-cell concerns to SudokuCell and owns the board-level logic.
 */
public class SudokuBoard {

    public static final int SIZE = 9;
    public static final int BOX_SIZE = 3;

    // ── Composed grid (Composition) ──────────────────────────────────────────
    private final SudokuCell[][] cells;

    // ── Constructor ──────────────────────────────────────────────────────────
    public SudokuBoard() {
        cells = new SudokuCell[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = new SudokuCell();
            }
        }
    }

    /** Deep-copy constructor (used to snapshot initial puzzle state). */
    public SudokuBoard(SudokuBoard other) {
        cells = new SudokuCell[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = new SudokuCell(other.cells[r][c]);
            }
        }
    }

    // ── Board initialisation ─────────────────────────────────────────────────
    /**
     * Loads a 9×9 int grid into the board cells.
     * Non-zero entries are marked fixed (given clues).
     */
    public void loadPuzzle(int[][] puzzle) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int v = puzzle[r][c];
                cells[r][c].forceSetValue(v);
                cells[r][c].setFixed(v != 0);
            }
        }
    }

    // ── Cell access ──────────────────────────────────────────────────────────
    public SudokuCell getCell(int row, int col) {
        validateIndices(row, col);
        return cells[row][col];
    }

    public int getValue(int row, int col) {
        validateIndices(row, col);
        return cells[row][col].getValue();
    }

    /**
     * Sets a value on a non-fixed cell.
     * @throws IllegalStateException if the cell is a fixed clue.
     */
    public void setValue(int row, int col, int value) {
        validateIndices(row, col);
        cells[row][col].setValue(value);   // SudokuCell enforces fixed check
    }

    public boolean isCellFixed(int row, int col) {
        validateIndices(row, col);
        return cells[row][col].isFixed();
    }

    public boolean isCellEmpty(int row, int col) {
        validateIndices(row, col);
        return cells[row][col].isEmpty();
    }

    // ── Board-level queries ──────────────────────────────────────────────────
    public boolean isFilled() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c].isEmpty()) return false;
            }
        }
        return true;
    }

    /** Returns a 2-D int array snapshot of the current board values. */
    public int[][] toIntGrid() {
        int[][] grid = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = cells[r][c].getValue();
            }
        }
        return grid;
    }

    /** Serialises the board into a 9-element JSONArray of 9-element int arrays. */
    public JSONArray toJson() {
        JSONArray board = new JSONArray();
        for (int r = 0; r < SIZE; r++) {
            JSONArray row = new JSONArray();
            for (int c = 0; c < SIZE; c++) {
                row.put(cells[r][c].getValue());
            }
            board.put(row);
        }
        return board;
    }

    /** Serialises which cells are fixed (given clues). */
    public JSONArray fixedToJson() {
        JSONArray board = new JSONArray();
        for (int r = 0; r < SIZE; r++) {
            JSONArray row = new JSONArray();
            for (int c = 0; c < SIZE; c++) {
                row.put(cells[r][c].isFixed());
            }
            board.put(row);
        }
        return board;
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private void validateIndices(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException(
                String.format("Invalid cell indices: (%d, %d)", row, col));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SIZE; r++) {
            if (r % BOX_SIZE == 0 && r != 0) sb.append("------+-------+------\n");
            for (int c = 0; c < SIZE; c++) {
                if (c % BOX_SIZE == 0 && c != 0) sb.append("| ");
                sb.append(cells[r][c]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
