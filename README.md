# 🔢 Sudoku Game — Java OOP + HTML5 / CSS3 / JavaScript

A full-stack, playable Sudoku application built with a **Java 17+ REST API backend** following Object-Oriented Programming (OOP) design principles, paired with a modern **dark glassmorphism frontend**.

🌐 **Live Demo (GitHub Pages):** [https://gowtham-projects-io.github.io/Sudoku-Game/](https://gowtham-projects-io.github.io/Sudoku-Game/)

📦 **GitHub Repository:** [https://github.com/Gowtham-projects-io/Sudoku-Game](https://github.com/Gowtham-projects-io/Sudoku-Game)

---

## 🚀 Key Features

- **Java OOP Architecture**: Strict encapsulation, abstraction, inheritance, polymorphism, and composition.
- **RESTful API**: Standardized JSON communication with endpoints for `/new`, `/move`, `/hint`, `/undo`, `/reset`, and `/check`.
- **Intelligent Puzzles**: Algorithmic backtracking solver and unique puzzle generator across Easy, Medium, and Hard.
- **Dark Glassmorphism Interface**: Smooth glow effects, responsive 9x9 board, numpad, timer, mistake tracker, and victory celebrations.
- **Hybrid Deployment**: Runs locally connected to the high-performance Java REST API server, and features seamless standalone capability for static hosting on GitHub Pages.

---

## 🏛️ Java OOP Concepts Demonstrated

| OOP Concept | Class / Interface | Description |
|---|---|---|
| **Encapsulation** | `SudokuCell`, `SudokuBoard`, `SudokuGame` | Private fields with guarded mutators. Fixed clue cells reject tampering. |
| **Abstraction** | `GameOperations` | Interface declaring public game operations (`newGame`, `validateMove`, `getHint`, etc.) implemented by `SudokuGameService`. |
| **Inheritance** | `Game` (abstract base) & `SudokuGame` | `SudokuGame` extends `Game`, inheriting identifier, difficulty, timers, and lifecycle state. |
| **Polymorphism** | `Game#startGame()` | Subclass overrides `startGame()`, dispatched dynamically at runtime via a `Game` reference. |
| **Composition** | `SudokuBoard` & `SudokuController` | `SudokuGame` *HAS-A* `SudokuBoard` composed of 81 `SudokuCell` objects; `SudokuController` *HAS-A* `SudokuGameService`. |

---

## 📁 Project Directory Structure

```
SudokuGame/
├── .github/workflows/
│   └── deploy.yml              # Automated GitHub Pages CI/CD workflow
├── frontend/
│   ├── index.html              # Modern semantic HTML5 interface
│   ├── style.css               # Dark glassmorphism CSS styling
│   └── script.js               # Frontend controller & hybrid API bridge
├── backend/
│   ├── pom.xml                 # Maven build configuration (Java 17+)
│   └── src/
│       ├── main/java/com/sudoku/
│       │   ├── Game.java               # Abstract base (Inheritance)
│       │   ├── GameOperations.java     # Interface (Abstraction)
│       │   ├── SudokuGame.java         # Extends Game (Polymorphism)
│       │   ├── SudokuCell.java         # Encapsulated cell model
│       │   ├── SudokuBoard.java        # 9x9 board model (Composition)
│       │   ├── SudokuGenerator.java    # Puzzle generator
│       │   ├── SudokuSolver.java       # Backtracking solver
│       │   ├── SudokuValidator.java    # Grid rules & conflict checker
│       │   ├── SudokuGameService.java  # Implements GameOperations
│       │   ├── SudokuController.java   # HTTP REST handlers
│       │   └── Main.java               # Server entry point & static server
│       └── test/java/com/sudoku/
│           ├── SudokuValidatorTest.java
│           ├── SudokuSolverTest.java
│           ├── SudokuGeneratorTest.java
│           └── SudokuGameServiceTest.java
├── run.bat                     # Windows one-click launcher
├── run.ps1                     # PowerShell one-click launcher
├── index.html                  # Root redirect for GitHub Pages
└── README.md
```

---

## 💻 Local Setup & Execution

### Prerequisites
- **Java 17+** (`java -version`)
- **Maven 3.8+** (`mvn -version`)

### Quick Start (Windows)
Double-click `run.bat` or run:
```powershell
.\run.ps1
```
This automatically compiles the backend, launches the server on port `8080`, and opens your default browser at `http://localhost:8080`.

### Manual Build & Run
```powershell
# 1. Build and run tests
cd backend
mvn clean package

# 2. Start the Java backend server
java -jar target/sudoku-backend-1.0.0.jar

# 3. Open http://localhost:8080 in your browser
```

### Running Unit Tests
```powershell
cd backend
mvn test
```

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/game/new` | Generates a new puzzle with chosen difficulty |
| `POST` | `/api/game/move` | Validates and places a digit on the board |
| `POST` | `/api/game/hint` | Retrieves an auto-placed hint for an empty cell |
| `POST` | `/api/game/undo` | Reverts the last move made |
| `POST` | `/api/game/reset` | Resets the puzzle back to initial clues |
| `POST` | `/api/game/check` | Checks current board completion status |

---

## 🌐 GitHub Pages Deployment

The repository includes a ready-to-run GitHub Actions CI/CD workflow (`.github/workflows/deploy.yml`):

1. Go to repository **Settings** on GitHub:
   - Click **Pages** on the left sidebar.
   - Under **Build and deployment** -> **Source**, select **GitHub Actions**.
2. The GitHub Action will automatically build and deploy the frontend to:
   **`https://gowtham-projects-io.github.io/Sudoku-Game/`**
