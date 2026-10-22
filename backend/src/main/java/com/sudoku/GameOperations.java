package com.sudoku;

import org.json.JSONObject;

/**
 * Interface — demonstrates ABSTRACTION.
 *
 * OOP Concept: Abstraction
 * Defines the contract for all game operations without exposing any
 * implementation details.  SudokuGameService implements this interface.
 */
public interface GameOperations {

    /**
     * Creates a brand-new puzzle for the requested difficulty.
     * @param difficulty "easy" | "medium" | "hard"
     * @return JSON object containing gameId, board, and metadata.
     */
    JSONObject newGame(String difficulty);

    /**
     * Validates and records a player's move.
     * @param gameId  The active game identifier.
     * @param row     0-based row index.
     * @param col     0-based column index.
     * @param value   The digit (1-9) the player wants to place.
     * @return JSON with fields: valid (boolean), conflict (boolean).
     */
    JSONObject validateMove(String gameId, int row, int col, int value);

    /**
     * Returns a safe hint: one correct digit for an empty cell.
     * @param gameId The active game identifier.
     * @return JSON with fields: row, col, value.
     */
    JSONObject getHint(String gameId);

    /**
     * Checks whether the current board state is a complete, correct solution.
     * @param gameId The active game identifier.
     * @return JSON with field: completed (boolean).
     */
    JSONObject checkCompletion(String gameId);

    /**
     * Resets the board to its original puzzle state.
     * @param gameId The active game identifier.
     * @return JSON with the original board.
     */
    JSONObject resetGame(String gameId);
}
