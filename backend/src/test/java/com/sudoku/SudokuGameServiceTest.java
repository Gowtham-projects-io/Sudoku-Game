package com.sudoku;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SudokuGameService API Tests")
class SudokuGameServiceTest {

    private SudokuGameService service;

    @BeforeEach
    public void setUp() {
        service = new SudokuGameService();
    }

    // ── newGame ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("newGame returns a gameId and board")
    void newGameReturnsGameIdAndBoard() {
        JSONObject result = service.newGame("easy");
        assertTrue(result.has("gameId"), "Response must include gameId");
        assertTrue(result.has("board"),  "Response must include board");
        assertNotNull(result.getString("gameId"));
        assertEquals("easy", result.getString("difficulty"));
    }

    @Test
    @DisplayName("newGame difficulty medium works")
    void newGameMedium() {
        JSONObject result = service.newGame("medium");
        assertEquals("medium", result.getString("difficulty"));
    }

    @Test
    @DisplayName("newGame difficulty hard works")
    void newGameHard() {
        JSONObject result = service.newGame("hard");
        assertEquals("hard", result.getString("difficulty"));
    }

    // ── validateMove ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid move on non-fixed cell is accepted")
    void validMoveAccepted() {
        JSONObject game   = service.newGame("easy");
        String gameId     = game.getString("gameId");

        // Find first empty cell from the board
        var fixed = game.getJSONArray("fixed");
        int targetRow = -1, targetCol = -1;
        outer:
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!fixed.getJSONArray(r).getBoolean(c)) {
                    targetRow = r;
                    targetCol = c;
                    break outer;
                }
            }
        }
        assertTrue(targetRow >= 0, "Must find at least one non-fixed cell");

        // Try placing 5 (may or may not be correct but must not throw)
        JSONObject result = service.validateMove(gameId, targetRow, targetCol, 5);
        assertTrue(result.has("valid"));
        assertTrue(result.has("conflict"));
    }

    @Test
    @DisplayName("Placing on a fixed cell throws IllegalStateException")
    void placingOnFixedCellThrows() {
        JSONObject game = service.newGame("easy");
        String gameId   = game.getString("gameId");

        var fixed = game.getJSONArray("fixed");

        int fixedRow = -1, fixedCol = -1;
        outer:
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (fixed.getJSONArray(r).getBoolean(c)) {
                    fixedRow = r;
                    fixedCol = c;
                    break outer;
                }
            }
        }
        assertTrue(fixedRow >= 0, "Easy puzzle must have at least one fixed cell");

        final int fr = fixedRow, fc = fixedCol;
        assertNotNull(assertThrows(IllegalStateException.class,
            () -> service.validateMove(gameId, fr, fc, 1)));
    }

    // ── getHint ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getHint returns a valid cell and value")
    void getHintReturnsValidCell() {
        JSONObject game = service.newGame("medium");
        String gameId   = game.getString("gameId");

        JSONObject hint = service.getHint(gameId);
        assertTrue(hint.getBoolean("available"), "Hint must be available");
        int row   = hint.getInt("row");
        int col   = hint.getInt("col");
        int value = hint.getInt("value");

        assertTrue(row >= 0 && row < 9);
        assertTrue(col >= 0 && col < 9);
        assertTrue(value >= 1 && value <= 9);
    }

    // ── resetGame ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resetGame restores original board")
    void resetRestoresOriginalBoard() {
        JSONObject game   = service.newGame("easy");
        String gameId     = game.getString("gameId");
        var    fixedArr   = game.getJSONArray("fixed");

        // Find and fill an empty cell
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (!fixedArr.getJSONArray(r).getBoolean(c)) {
                    service.validateMove(gameId, r, c, 5);
                    break;
                }
            }
        }

        JSONObject reset = service.resetGame(gameId);
        assertTrue(reset.has("board"), "Reset must return a board");
        assertEquals("Game reset to original puzzle.", reset.getString("message"));
    }

    // ── checkCompletion ──────────────────────────────────────────────────────

    @Test
    @DisplayName("checkCompletion returns false for new game")
    void checkCompletionFalseForNewGame() {
        JSONObject game = service.newGame("easy");
        String gameId   = game.getString("gameId");

        JSONObject check = service.checkCompletion(gameId);
        assertFalse(check.getBoolean("completed"));
    }

    // ── Game not found ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Operations on unknown gameId throw exception")
    void unknownGameIdThrows() {
        assertNotNull(assertThrows(IllegalArgumentException.class,
            () -> service.checkCompletion("nonexistent-id")));
    }
}
