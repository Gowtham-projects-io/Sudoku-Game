package com.sudoku;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer — implements GameOperations (Abstraction) and
 * composes SudokuGenerator + SudokuSolver + SudokuValidator (Composition).
 *
 * OOP Concept: Abstraction
 *   Implements the GameOperations interface; callers (SudokuController)
 *   only know the interface, not this concrete class.
 *
 * OOP Concept: Composition
 *   Uses SudokuGenerator and SudokuSolver as collaborators.
 *
 * OOP Concept: Polymorphism
 *   Invokes game.startGame() through a Game reference, triggering
 *   SudokuGame's overridden implementation at runtime.
 */
public class SudokuGameService implements GameOperations {

    // ── Composed collaborators (Composition) ─────────────────────────────────
    private final SudokuGenerator            generator;
    private final SudokuSolver               solver;
    private final Map<String, SudokuGame>    games;

    // ── Constructor ──────────────────────────────────────────────────────────
    public SudokuGameService() {
        this.generator = new SudokuGenerator();
        this.solver    = new SudokuSolver();
        this.games     = new ConcurrentHashMap<>();
    }

    // ── GameOperations implementation ────────────────────────────────────────

    @Override
    public JSONObject newGame(String difficulty) {
        String gameId = UUID.randomUUID().toString();

        // Generate puzzle + solution pair
        int[][][] pair    = generator.generate(difficulty);
        int[][]   puzzle  = pair[0];
        int[][]   solution = pair[1];

        // Polymorphism: SudokuGame extends Game; startGame() dispatches to
        // SudokuGame#startGame() via a Game reference.
        SudokuGame game = new SudokuGame(gameId, difficulty, puzzle, solution);
        Game gameRef = game;          // Game reference for polymorphic call
        gameRef.startGame();          // runtime dispatch → SudokuGame#startGame()

        games.put(gameId, game);

        JSONObject response = game.toJson();
        response.put("message", "New game created!");
        return response;
    }

    @Override
    public JSONObject validateMove(String gameId, int row, int col, int value) {
        SudokuGame game = getGame(gameId);

        // Check for board-level conflict before placing
        int[][] currentGrid = game.getBoard().toIntGrid();
        boolean conflict = SudokuValidator.hasConflict(currentGrid, row, col, value);

        boolean correct;
        if (value == 0) {
            // Erase: always allowed on non-fixed cells
            game.placeValue(row, col, 0);
            correct = true;
        } else {
            correct = game.placeValue(row, col, value);
        }

        boolean completed = game.isComplete();
        if (completed) game.setStatus("COMPLETED");

        JSONObject resp = new JSONObject();
        resp.put("valid",     correct);
        resp.put("conflict",  conflict && value != 0);
        resp.put("completed", completed);
        resp.put("mistakes",  game.getMistakeCount());
        resp.put("board",     game.getBoardJson());
        return resp;
    }

    @Override
    public JSONObject getHint(String gameId) {
        SudokuGame game = getGame(gameId);
        int[] hint = game.getHintCell();

        JSONObject resp = new JSONObject();
        if (hint == null) {
            resp.put("available", false);
        } else {
            // Auto-place the hint on the board
            game.placeValue(hint[0], hint[1], hint[2]);
            resp.put("available",  true);
            resp.put("row",        hint[0]);
            resp.put("col",        hint[1]);
            resp.put("value",      hint[2]);
            resp.put("board",      game.getBoardJson());
            resp.put("hintsUsed",  game.getHintsUsed());
        }
        return resp;
    }

    @Override
    public JSONObject checkCompletion(String gameId) {
        SudokuGame game = getGame(gameId);
        boolean completed = game.isComplete();
        if (completed) game.setStatus("COMPLETED");

        JSONObject resp = new JSONObject();
        resp.put("completed",   completed);
        resp.put("elapsedSecs", game.getElapsedSeconds());
        resp.put("mistakes",    game.getMistakeCount());
        resp.put("hintsUsed",   game.getHintsUsed());
        return resp;
    }

    @Override
    public JSONObject resetGame(String gameId) {
        SudokuGame game = getGame(gameId);
        game.reset();
        game.setStatus("IN_PROGRESS");

        JSONObject resp = new JSONObject();
        resp.put("board",   game.getBoardJson());
        resp.put("fixed",   game.getFixedJson());
        resp.put("message", "Game reset to original puzzle.");
        return resp;
    }

    // ── Undo support (extra, not in interface) ───────────────────────────────
    public JSONObject undoMove(String gameId) {
        SudokuGame game = getGame(gameId);
        int[] undone = game.undo();

        JSONObject resp = new JSONObject();
        if (undone == null) {
            resp.put("available", false);
        } else {
            resp.put("available", true);
            resp.put("row",   undone[0]);
            resp.put("col",   undone[1]);
            resp.put("value", undone[2]);
            resp.put("board", game.getBoardJson());
        }
        return resp;
    }

    public SudokuSolver getSolver() {
        return solver;
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private SudokuGame getGame(String gameId) {
        SudokuGame game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }
        return game;
    }
}
