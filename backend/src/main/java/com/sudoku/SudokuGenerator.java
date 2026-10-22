package com.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates valid, uniquely-solvable Sudoku puzzles.
 *
 * OOP Concept: Composition + Encapsulation
 * Uses a SudokuSolver internally (Composition) and keeps all intermediate
 * state private (Encapsulation).
 */
public class SudokuGenerator {

    // ── Composed helper (Composition) ────────────────────────────────────────
    private final SudokuSolver solver;
    private final Random       rng;

    // ── Difficulty → clue count ──────────────────────────────────────────────
    private static final int EASY_CLUES   = 40;
    private static final int MEDIUM_CLUES = 32;
    private static final int HARD_CLUES   = 25;

    // ── Constructor ──────────────────────────────────────────────────────────
    public SudokuGenerator() {
        this.solver = new SudokuSolver();
        this.rng    = new Random();
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Returns a pair: [puzzle][solution] for the requested difficulty.
     */
    public int[][][] generate(String difficulty) {
        int clues = switch (difficulty.toLowerCase()) {
            case "easy"   -> EASY_CLUES;
            case "hard"   -> HARD_CLUES;
            default       -> MEDIUM_CLUES;
        };

        int[][] solution = generateFullSolution();
        int[][] puzzle   = createPuzzle(solution, clues);
        return new int[][][]{puzzle, solution};
    }

    // ── Private generation logic ─────────────────────────────────────────────

    /** Builds a fully filled, valid Sudoku grid using backtracking + shuffling. */
    private int[][] generateFullSolution() {
        int[][] grid = new int[SudokuBoard.SIZE][SudokuBoard.SIZE];
        fillGrid(grid);
        return grid;
    }

    private boolean fillGrid(int[][] grid) {
        int[] empty = findEmpty(grid);
        if (empty == null) return true;

        int row = empty[0];
        int col = empty[1];

        List<Integer> digits = shuffledDigits();
        for (int v : digits) {
            if (SudokuValidator.isValidPlacement(grid, row, col, v)) {
                grid[row][col] = v;
                if (fillGrid(grid)) return true;
                grid[row][col] = 0;
            }
        }
        return false;
    }

    /**
     * Removes digits from the full solution until the target clue count
     * is reached, verifying unique solvability after each removal.
     */
    private int[][] createPuzzle(int[][] solution, int clues) {
        int[][] puzzle = deepCopy(solution);
        int cellsToRemove = SudokuBoard.SIZE * SudokuBoard.SIZE - clues;

        List<int[]> positions = allPositions();
        Collections.shuffle(positions, rng);

        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) break;

            int row = pos[0];
            int col = pos[1];
            int saved = puzzle[row][col];

            puzzle[row][col] = 0;

            // Only keep removal if puzzle still has a unique solution
            if (solver.countSolutions(puzzle) == 1) {
                removed++;
            } else {
                puzzle[row][col] = saved;   // restore
            }
        }
        return puzzle;
    }

    private List<Integer> shuffledDigits() {
        List<Integer> digits = new ArrayList<>();
        for (int i = 1; i <= SudokuBoard.SIZE; i++) digits.add(i);
        Collections.shuffle(digits, rng);
        return digits;
    }

    private List<int[]> allPositions() {
        List<int[]> list = new ArrayList<>();
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                list.add(new int[]{r, c});
            }
        }
        return list;
    }

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
