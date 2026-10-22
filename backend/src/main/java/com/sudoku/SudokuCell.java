package com.sudoku;

/**
 * Represents a single cell on the Sudoku board — demonstrates ENCAPSULATION.
 *
 * OOP Concept: Encapsulation
 * All fields are private.  The `fixed` flag prevents modification of
 * pre-filled puzzle cells through the public setter.
 */
public class SudokuCell {

    // ── Private fields ───────────────────────────────────────────────────────
    private int     value;      // 0 = empty, 1-9 = placed digit
    private boolean fixed;      // true = given clue, cannot be changed
    private boolean hasConflict;

    // ── Constructors ─────────────────────────────────────────────────────────
    public SudokuCell() {
        this.value       = 0;
        this.fixed       = false;
        this.hasConflict = false;
    }

    public SudokuCell(int value, boolean fixed) {
        this.value       = value;
        this.fixed       = fixed;
        this.hasConflict = false;
    }

    /** Copy constructor — used when resetting or cloning the board. */
    public SudokuCell(SudokuCell other) {
        this.value       = other.value;
        this.fixed       = other.fixed;
        this.hasConflict = other.hasConflict;
    }

    // ── Controlled mutator (Encapsulation: invalid change blocked) ───────────
    /**
     * Sets the cell value.  Throws if the cell is a fixed clue.
     */
    public void setValue(int value) {
        if (fixed) {
            throw new IllegalStateException(
                "Cannot modify a fixed (given) cell.");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException(
                "Cell value must be between 0 and 9, got: " + value);
        }
        this.value = value;
    }

    /** Force-sets a value regardless of the fixed flag (used during board init). */
    public void forceSetValue(int value) {
        this.value = value;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int     getValue()       { return value; }
    public boolean isFixed()        { return fixed; }
    public boolean isEmpty()        { return value == 0; }
    public boolean hasConflict()    { return hasConflict; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setFixed(boolean fixed)           { this.fixed = fixed; }
    public void setHasConflict(boolean conflict)  { this.hasConflict = conflict; }

    @Override
    public String toString() {
        return value == 0 ? "." : String.valueOf(value);
    }
}
