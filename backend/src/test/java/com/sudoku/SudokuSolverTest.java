package com.sudoku;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SudokuSolver Tests")
class SudokuSolverTest {

    private SudokuSolver solver;

    @BeforeEach
    public void setUp() {
        solver = new SudokuSolver();
    }

    @Test
    @DisplayName("Solver solves a known puzzle correctly")
    void solverSolvesKnownPuzzle() {
        int[][] puzzle = {
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

        int[][] solution = solver.solve(puzzle);

        assertNotNull(solution, "Solver must return a non-null solution");
        assertTrue(SudokuValidator.isValidCompleteGrid(solution),
                   "Solved grid must pass full validation");
    }

    @Test
    @DisplayName("Solver returns null for an unsolvable puzzle")
    void solverReturnsNullForUnsolvable() {
        // (0,0) = 0, needs 1. But col 0 already contains 1 in row 1 -> contradiction.
        // Every other digit 2-9 already appears in row 0.
        int[][] unsolvable = {
            {0, 2, 3, 4, 5, 6, 7, 8, 9},   // empty at (0,0), must be 1
            {1, 5, 6, 7, 8, 9, 2, 3, 4},   // col 0 has 1 here -> conflict
            {4, 8, 9, 1, 2, 3, 5, 6, 7},
            {2, 6, 1, 3, 7, 5, 8, 9, 4},   // various to keep cols valid for this test
            {3, 7, 5, 8, 9, 4, 6, 1, 2},
            {8, 9, 4, 2, 6, 7, 3, 5, 1},
            {5, 3, 7, 6, 4, 2, 9, 8, 1},
            {6, 4, 2, 9, 3, 8, 4, 7, 5},
            {9, 1, 8, 5, 1, 2, 6, 4, 3}
        };
        assertNull(solver.solve(unsolvable));
    }

    @Test
    @DisplayName("Count solutions: unique puzzle returns 1")
    void uniquePuzzleHasOneSolution() {
        int[][] puzzle = {
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
        assertEquals(1, solver.countSolutions(puzzle));
    }

    @Test
    @DisplayName("Already solved grid counts as 1 solution")
    void alreadySolvedGridCountsOne() {
        int[][] solved = {
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
        assertEquals(1, solver.countSolutions(solved));
    }
}
