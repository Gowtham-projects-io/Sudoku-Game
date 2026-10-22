package com.sudoku;

/**
 * Abstract base class — demonstrates INHERITANCE.
 *
 * OOP Concept: Inheritance
 * Every concrete game type (SudokuGame) extends this class, inheriting
 * common state (gameId, difficulty, status) and forcing subclasses to
 * implement startGame().
 */
public abstract class Game {

    // ── Encapsulated fields ──────────────────────────────────────────────────
    private final String gameId;
    private String difficulty;
    private String status;          // "IN_PROGRESS" | "COMPLETED" | "FAILED"
    private long startTime;
    private long endTime;

    // ── Constructor ──────────────────────────────────────────────────────────
    protected Game(String gameId, String difficulty) {
        this.gameId     = gameId;
        this.difficulty = difficulty;
        this.status     = "IN_PROGRESS";
        this.startTime  = System.currentTimeMillis();
        this.endTime    = 0L;
    }

    // ── Abstract method: subclasses MUST override (Polymorphism) ────────────
    /**
     * Starts the game. Each game type implements its own logic.
     * Overriding this in SudokuGame demonstrates runtime polymorphism.
     */
    public abstract void startGame();

    // ── Getters / Setters (Encapsulation) ───────────────────────────────────
    public String getGameId()    { return gameId; }

    public String getDifficulty() { return difficulty; }
    public void   setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStatus()    { return status; }
    public void   setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            this.endTime = System.currentTimeMillis();
        }
    }

    public long getStartTime()   { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime()     { return endTime; }
    public void setEndTime(long endTime)     { this.endTime = endTime; }

    /** Resets the start time to now and clears the end time. */
    public void resetTimer() {
        this.startTime = System.currentTimeMillis();
        this.endTime   = 0L;
    }

    /** Returns elapsed seconds since the game started. */
    public long getElapsedSeconds() {
        long end = (endTime > 0) ? endTime : System.currentTimeMillis();
        return (end - startTime) / 1000;
    }

    public boolean isCompleted() { return "COMPLETED".equals(status); }

    @Override
    public String toString() {
        return String.format("Game[id=%s, difficulty=%s, status=%s]",
                             gameId, difficulty, status);
    }
}
