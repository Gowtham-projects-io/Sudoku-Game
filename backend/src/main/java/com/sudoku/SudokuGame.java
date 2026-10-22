package com.sudoku;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Concrete game entity — demonstrates INHERITANCE and POLYMORPHISM.
 *
 * OOP Concept: Inheritance
 *   Extends the abstract Game class, inheriting gameId / difficulty / status.
 *
 * OOP Concept: Polymorphism
 *   Overrides startGame() from Game; a Game reference can invoke this
 *   method and the SudokuGame implementation is dispatched at runtime.
 *
 * OOP Concept: Composition
 *   Contains a SudokuBoard (the live board), a snapshot of the initial
 *   puzzle, and a snapshot of the solution.
 */
public class SudokuGame extends Game {

    // ── Composed objects (Composition) ───────────────────────────────────────
    private final SudokuBoard board;           // live mutable board
    private final SudokuBoard initialBoard;    // immutable puzzle snapshot
    private final int[][]     solution;        // correct answer (never sent to client)

    // ── Game-specific state (Encapsulation) ──────────────────────────────────
    private int            mistakeCount;
    private int            hintsUsed;
    private final Deque<int[]> undoStack;      // each entry: [row, col, previousValue]

    // ── Constructor ──────────────────────────────────────────────────────────
    public SudokuGame(String gameId, String difficulty,
                      int[][] puzzle, int[][] solution) {
        super(gameId, difficulty);            // parent constructor (Inheritance)

        this.solution     = deepCopy(solution);
        this.board        = new SudokuBoard();
        this.board.loadPuzzle(puzzle);
        this.initialBoard = new SudokuBoard(this.board);  // deep copy
        this.mistakeCount = 0;
        this.hintsUsed    = 0;
        this.undoStack    = new ArrayDeque<>();
    }

    // ── Polymorphism: overrides abstract Game#startGame() ───────────────────
    @Override
    public void startGame() {
        setStatus("IN_PROGRESS");
        resetTimer();
        System.out.println("SudokuGame started: " + getGameId()
                           + " | difficulty=" + getDifficulty());
    }

    // ── Move handling ────────────────────────────────────────────────────────
    /**
     * Attempts to place a digit on the board.
     * @return true if the value matches the solution.
     */
    public boolean placeValue(int row, int col, int value) {
        int previous = board.getValue(row, col);
        board.setValue(row, col, value);          // throws if cell is fixed

        // Push undo record
        undoStack.push(new int[]{row, col, previous});

        boolean correct = (solution[row][col] == value);
        if (!correct && value != 0) {
            mistakeCount++;
        }
        return correct;
    }

    /** Removes the last placed digit (undo), restoring the previous value. */
    public int[] undo() {
        if (undoStack.isEmpty()) return null;
        int[] last = undoStack.pop();   // [row, col, previousValue]
        int row = last[0], col = last[1], prevVal = last[2];
        try {
            board.setValue(row, col, prevVal);
        } catch (IllegalStateException ignored) {
            // cell was marked fixed somehow — should never happen for non-clue cells
        }
        return last;
    }

    /** Resets the board to the original puzzle, clears undo and mistakes. */
    public void reset() {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                SudokuCell init = initialBoard.getCell(r, c);
                board.getCell(r, c).setFixed(false);        // allow write
                board.getCell(r, c).forceSetValue(init.getValue());
                board.getCell(r, c).setFixed(init.isFixed());
            }
        }
        undoStack.clear();
        mistakeCount = 0;
        resetTimer();
    }

    /** Checks whether the live board is fully and correctly filled. */
    public boolean isComplete() {
        if (!board.isFilled()) return false;
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                if (board.getValue(r, c) != solution[r][c]) return false;
            }
        }
        return true;
    }

    /**
     * Finds one empty cell whose correct value comes from the solution.
     * @return int[] {row, col, value} or null if no empty cell exists.
     */
    public int[] getHintCell() {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                if (board.isCellEmpty(r, c)) {
                    hintsUsed++;
                    return new int[]{r, c, solution[r][c]};
                }
            }
        }
        return null;
    }

    // ── Serialisation helpers ────────────────────────────────────────────────
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("gameId",       getGameId());
        obj.put("difficulty",   getDifficulty());
        obj.put("status",       getStatus());
        obj.put("board",        board.toJson());
        obj.put("fixed",        board.fixedToJson());
        obj.put("mistakes",     mistakeCount);
        obj.put("hintsUsed",    hintsUsed);
        obj.put("elapsedSecs",  getElapsedSeconds());
        return obj;
    }

    public JSONArray getBoardJson()   { return board.toJson(); }
    public JSONArray getFixedJson()   { return board.fixedToJson(); }

    // ── Getters ──────────────────────────────────────────────────────────────
    public SudokuBoard getBoard()     { return board; }
    public int getMistakeCount()      { return mistakeCount; }
    public int getHintsUsed()         { return hintsUsed; }
    public int getSolutionValue(int r, int c) { return solution[r][c]; }

    // ── Utility ──────────────────────────────────────────────────────────────
    private static int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}
