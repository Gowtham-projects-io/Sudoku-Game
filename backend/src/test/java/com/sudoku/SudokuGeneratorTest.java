package com.sudoku;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SudokuGenerator Tests")
class SudokuGeneratorTest {

    private final SudokuGenerator generator = new SudokuGenerator();
    private final SudokuSolver    solver    = new SudokuSolver();

    @Test
    @DisplayName("Easy puzzle has a unique solution")
    void easyPuzzleUnique() {
        int[][][] pair = generator.generate("easy");
        assertEquals(1, solver.countSolutions(pair[0]), "Easy puzzle must be uniquely solvable");
    }

    @Test
    @DisplayName("Medium puzzle has a unique solution")
    void mediumPuzzleUnique() {
        int[][][] pair = generator.generate("medium");
        assertEquals(1, solver.countSolutions(pair[0]));
    }

    @Test
    @DisplayName("Hard puzzle has a unique solution")
    void hardPuzzleUnique() {
        int[][][] pair = generator.generate("hard");
        assertEquals(1, solver.countSolutions(pair[0]));
    }

    @Test
    @DisplayName("Generated solution is a valid complete grid")
    void generatedSolutionIsValid() {
        int[][][] pair = generator.generate("easy");
        assertTrue(SudokuValidator.isValidCompleteGrid(pair[1]));
    }

    @Test
    @DisplayName("Easy puzzle has more clues than hard")
    void easyHasMoreCluesThanHard() {
        int easy = countClues(generator.generate("easy")[0]);
        int hard = countClues(generator.generate("hard")[0]);
        assertTrue(easy > hard, "Easy must have more given digits than hard");
    }

    private int countClues(int[][] puzzle) {
        int count = 0;
        for (int[] row : puzzle)
            for (int v : row)
                if (v != 0) count++;
        return count;
    }
}
