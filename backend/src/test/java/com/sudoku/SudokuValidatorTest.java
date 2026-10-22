package com.sudoku;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SudokuValidator Tests")
class SudokuValidatorTest {

    private int[][] grid;

    @BeforeEach
    public void setUp() {
        // A partially filled valid grid
        grid = new int[][]{
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
    }

    @Test
    @DisplayName("Valid placement returns true")
    void validPlacementReturnsTrue() {
        // Row 0, col 2 is empty; placing 4 should be valid
        assertTrue(SudokuValidator.isValidPlacement(grid, 0, 2, 4));
    }

    @Test
    @DisplayName("Row conflict detected")
    void rowConflictDetected() {
        // Row 0 already contains 5; placing 5 at (0,2) is invalid
        assertTrue(SudokuValidator.conflictsInRow(grid, 0, 2, 5));
    }

    @Test
    @DisplayName("Column conflict detected")
    void columnConflictDetected() {
        // Col 0 already contains 5 (row 0); placing 5 at (2,0) is invalid
        assertTrue(SudokuValidator.conflictsInCol(grid, 2, 0, 5));
    }

    @Test
    @DisplayName("Box conflict detected")
    void boxConflictDetected() {
        // Top-left box has 5, 3, 6, 9, 8; placing 9 at (0,2) is invalid
        assertTrue(SudokuValidator.conflictsInBox(grid, 0, 2, 9));
    }

    @Test
    @DisplayName("No conflict for unique valid digit")
    void noConflictForUniqueDigit() {
        assertFalse(SudokuValidator.hasConflict(grid, 0, 2, 4));
    }

    @Test
    @DisplayName("Erasing (value=0) never conflicts")
    void erasingNeverConflicts() {
        assertFalse(SudokuValidator.hasConflict(grid, 0, 0, 0));
    }

    @Test
    @DisplayName("Valid complete grid passes validation")
    void validCompleteGridPasses() {
        int[][] complete = {
            {5, 3, 4, 6, 7, 8, 9, 1, 2},
            {6, 7, 2, 1, 9, 5, 3, 4, 8},
            {1, 9, 8, 3, 4, 2, 5, 6, 7},
            {8, 5, 9, 7, 6, 1, 4, 2, 3},
            {4, 2, 6, 8, 5, 3, 7, 9, 1},
            {7, 1, 3, 9, 2, 4, 8, 5, 6},
            {9, 6, 1, 5, 3, 7, 2, 8, 4},
            {2, 8, 7, 4, 1, 9, 6, 3, 5},
            {3, 4, 5, 2, 8, 6, 1, 7, 9}
        };
        assertTrue(SudokuValidator.isValidCompleteGrid(complete));
    }

    @ParameterizedTest
    @CsvSource({"0,2,4", "0,5,2", "0,3,2"})
    @DisplayName("Multiple valid placements pass")
    void multipleValidPlacements(int row, int col, int value) {
        // Verified manually against the setUp grid:
        // (0,2,4): row 0 ok, col 2 ok, top-left box ok
        // (0,5,2): row 0 ok, col 5 ok, top-middle box ok
        // (0,3,2): row 0 ok, col 3 ok, top-middle box ok
        assertTrue(SudokuValidator.isValidPlacement(grid, row, col, value));
    }
}
