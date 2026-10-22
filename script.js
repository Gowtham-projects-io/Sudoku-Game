/**
 * script.js — Sudoku Frontend
 *
 * Communicates with the Java HttpServer backend via fetch().
 * All game logic (validation, hints, completion) runs on the Java side.
 * This file manages only UI state and user interactions.
 */

'use strict';

// ── Constants ─────────────────────────────────────────────────────────────────
const API_BASE    = 'http://localhost:8080/api/game';
const MAX_HINTS   = 3;
const MAX_MISTAKES = 5;

// ── Application State ─────────────────────────────────────────────────────────
let state = {
  gameId:      null,
  board:       [],       // 9×9 array of current values
  fixed:       [],       // 9×9 boolean array — which cells are given clues
  cellStates:  [],       // 9×9 — 'correct' | 'conflict' | 'hint' | ''
  selected:    null,     // {row, col} or null
  difficulty:  'medium',
  mistakes:    0,
  hints:       0,
  timerSecs:   0,
  timerHandle: null,
  gameActive:  false,
};

// ── Init ──────────────────────────────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  buildEmptyBoard();
  startNewGame();
  document.getElementById('sudokuBoard').addEventListener('keydown', handleKeyboard);
});

// ── Difficulty selector ───────────────────────────────────────────────────────
function selectDifficulty(diff) {
  state.difficulty = diff;
  document.querySelectorAll('.pill').forEach(p => {
    p.classList.toggle('active', p.dataset.diff === diff);
  });
}

// ── New game ──────────────────────────────────────────────────────────────────
async function startNewGame() {
  showLoading(true);
  stopTimer();

  try {
    const data = await apiPost('/new', { difficulty: state.difficulty });

    state.gameId     = data.gameId;
    state.board      = jsonArrayTo2D(data.board);
    state.fixed      = jsonArrayTo2D(data.fixed);
    state.cellStates = Array.from({length: 9}, () => Array(9).fill(''));
    state.selected   = null;
    state.mistakes   = 0;
    state.hints      = 0;
    state.gameActive = true;

    renderBoard();
    updateMistakeDisplay();
    updateHintBadge();
    setStatus('Select a cell and enter a number to begin.');
    startTimer();
  } catch (err) {
    showToast('⚠ Could not reach the backend. Is the Java server running?', 'error');
    setStatus('Backend unavailable — please start the Java server.');
  } finally {
    showLoading(false);
  }
}

// ── Board rendering ───────────────────────────────────────────────────────────
function buildEmptyBoard() {
  const boardEl = document.getElementById('sudokuBoard');
  boardEl.innerHTML = '';
  for (let r = 0; r < 9; r++) {
    for (let c = 0; c < 9; c++) {
      const cell = document.createElement('div');
      cell.className = 'cell';
      cell.id = `cell-${r}-${c}`;
      cell.dataset.row = r;
      cell.dataset.col = c;
      cell.setAttribute('role', 'gridcell');
      cell.setAttribute('tabindex', '-1');
      cell.addEventListener('click', () => selectCell(r, c));
      boardEl.appendChild(cell);
    }
  }
}

function renderBoard() {
  for (let r = 0; r < 9; r++) {
    for (let c = 0; c < 9; c++) {
      const cell   = getCellEl(r, c);
      const val    = state.board[r][c];
      const fixed  = state.fixed[r][c];
      const cState = state.cellStates[r][c];

      cell.textContent = val === 0 ? '' : val;
      cell.className   = 'cell';

      if (fixed)           cell.classList.add('fixed');
      if (cState)          cell.classList.add(cState);
      if (!fixed)          cell.setAttribute('aria-label', `Row ${r+1} Col ${c+1}${val ? ' value '+val : ' empty'}`);
    }
  }
  applyHighlights();
}

// ── Selection & highlights ────────────────────────────────────────────────────
function selectCell(row, col) {
  if (!state.gameActive) return;
  state.selected = { row, col };
  applyHighlights();
  getCellEl(row, col).focus();
}

function applyHighlights() {
  // Clear all
  document.querySelectorAll('.cell').forEach(el => {
    el.classList.remove('selected', 'highlight-area', 'highlight-num');
  });

  if (!state.selected) return;
  const { row, col } = state.selected;
  const selVal = state.board[row][col];
  const boxR   = Math.floor(row / 3) * 3;
  const boxC   = Math.floor(col / 3) * 3;

  for (let r = 0; r < 9; r++) {
    for (let c = 0; c < 9; c++) {
      const el = getCellEl(r, c);
      if (r === row && c === col) {
        el.classList.add('selected');
      } else if (
        r === row || c === col ||
        (r >= boxR && r < boxR + 3 && c >= boxC && c < boxC + 3)
      ) {
        el.classList.add('highlight-area');
      }
      // Highlight same number
      if (selVal !== 0 && state.board[r][c] === selVal && !(r === row && c === col)) {
        el.classList.add('highlight-num');
      }
    }
  }
}

// ── Number input ──────────────────────────────────────────────────────────────
async function inputNumber(num) {
  if (!state.gameActive || !state.selected) {
    showToast('Select a cell first!', 'warn');
    return;
  }
  const { row, col } = state.selected;
  if (state.fixed[row][col]) {
    showToast('This is a given clue — cannot be changed.', 'warn');
    return;
  }

  // Brief button press animation
  const numBtn = document.querySelector(`.num-btn[data-num="${num}"]`);
  if (numBtn) {
    numBtn.classList.add('pressed');
    setTimeout(() => numBtn.classList.remove('pressed'), 200);
  }

  try {
    const data = await apiPost('/move', {
      gameId: state.gameId,
      row, col, value: num
    });

    state.board[row][col]      = num;
    state.mistakes             = data.mistakes;
    state.cellStates[row][col] = data.conflict ? 'conflict' : (data.valid ? 'correct' : 'conflict');

    updateMistakeDisplay();
    renderBoard();

    // Trigger pop animation
    const cellEl = getCellEl(row, col);
    cellEl.classList.add('pop');
    cellEl.addEventListener('animationend', () => cellEl.classList.remove('pop'), { once: true });

    if (data.conflict) {
      setStatus(`Conflict detected at row ${row+1}, col ${col+1}. Try another number.`);
      if (state.mistakes >= MAX_MISTAKES) {
        setStatus('Too many mistakes! Starting a new game…');
        setTimeout(startNewGame, 2000);
      }
    } else if (data.valid) {
      setStatus('');
    }

    if (data.completed) {
      onGameComplete(data);
    }
  } catch (err) {
    showToast('Move failed — backend error.', 'error');
  }
}

// ── Erase ─────────────────────────────────────────────────────────────────────
async function eraseCell() {
  if (!state.selected) return;
  const { row, col } = state.selected;
  if (state.fixed[row][col]) return;
  if (state.board[row][col] === 0) return;

  try {
    await apiPost('/move', { gameId: state.gameId, row, col, value: 0 });
    state.board[row][col]      = 0;
    state.cellStates[row][col] = '';
    renderBoard();
  } catch (err) {
    showToast('Erase failed.', 'error');
  }
}

// ── Undo ──────────────────────────────────────────────────────────────────────
async function undoMove() {
  if (!state.gameActive) return;
  try {
    const data = await apiPost('/undo', { gameId: state.gameId });
    if (!data.available) { showToast('Nothing to undo.', 'warn'); return; }

    state.board[data.row][data.col]      = data.value;
    state.cellStates[data.row][data.col] = '';
    renderBoard();
    setStatus(`Undo: cleared cell (${data.row+1}, ${data.col+1}).`);
  } catch (err) {
    showToast('Undo failed.', 'error');
  }
}

// ── Hint ──────────────────────────────────────────────────────────────────────
async function getHint() {
  if (!state.gameActive) return;
  if (state.hints >= MAX_HINTS) {
    showToast('No hints remaining!', 'warn');
    return;
  }

  try {
    const data = await apiPost('/hint', { gameId: state.gameId });
    if (!data.available) { showToast('No hints available — board may be full!', 'warn'); return; }

    state.board[data.row][data.col]      = data.value;
    state.cellStates[data.row][data.col] = 'hint';
    state.hints                          = data.hintsUsed;
    state.selected                       = { row: data.row, col: data.col };

    updateHintBadge();
    renderBoard();
    setStatus(`Hint placed at row ${data.row+1}, col ${data.col+1}: ${data.value}`);
    showToast(`💡 Hint: ${data.value} placed at (${data.row+1},${data.col+1})`, 'success');
  } catch (err) {
    showToast('Hint failed.', 'error');
  }
}

// ── Reset ─────────────────────────────────────────────────────────────────────
async function resetGame() {
  if (!state.gameActive) return;
  try {
    const data = await apiPost('/reset', { gameId: state.gameId });
    state.board      = jsonArrayTo2D(data.board);
    state.fixed      = jsonArrayTo2D(data.fixed);
    state.cellStates = Array.from({length: 9}, () => Array(9).fill(''));
    state.selected   = null;
    state.mistakes   = 0;
    stopTimer();
    state.timerSecs  = 0;
    updateMistakeDisplay();
    renderBoard();
    updateTimerDisplay();
    startTimer();
    setStatus('Puzzle reset to initial state.');
    showToast('🔄 Puzzle restarted!', 'success');
  } catch (err) {
    showToast('Reset failed — backend error.', 'error');
  }
}

// ── Keyboard support ──────────────────────────────────────────────────────────
function handleKeyboard(e) {
  if (!state.selected) return;
  const { row, col } = state.selected;

  if (e.key >= '1' && e.key <= '9') {
    e.preventDefault();
    inputNumber(parseInt(e.key));
    return;
  }

  // Arrow navigation
  const moves = {
    ArrowUp:    [-1,  0],
    ArrowDown:  [ 1,  0],
    ArrowLeft:  [ 0, -1],
    ArrowRight: [ 0,  1],
  };
  if (moves[e.key]) {
    e.preventDefault();
    const [dr, dc] = moves[e.key];
    const nr = Math.max(0, Math.min(8, row + dr));
    const nc = Math.max(0, Math.min(8, col + dc));
    selectCell(nr, nc);
    return;
  }

  // Delete/Backspace = erase
  if (e.key === 'Delete' || e.key === 'Backspace') {
    e.preventDefault();
    eraseCell();
  }
}

// ── Victory ───────────────────────────────────────────────────────────────────
function onGameComplete(data) {
  state.gameActive = false;
  stopTimer();

  document.getElementById('vTime').textContent     = formatTime(state.timerSecs);
  document.getElementById('vMistakes').textContent = state.mistakes;
  document.getElementById('vHints').textContent    = state.hints;

  document.getElementById('victoryOverlay').hidden = false;
  setStatus('🎉 Congratulations! Puzzle solved!');
}

function closeOverlay() {
  document.getElementById('victoryOverlay').hidden = true;
}

// ── Timer ─────────────────────────────────────────────────────────────────────
function startTimer() {
  state.timerSecs  = 0;
  updateTimerDisplay();
  state.timerHandle = setInterval(() => {
    state.timerSecs++;
    updateTimerDisplay();
  }, 1000);
}

function stopTimer() {
  clearInterval(state.timerHandle);
  state.timerHandle = null;
}

function updateTimerDisplay() {
  document.getElementById('timer').textContent = formatTime(state.timerSecs);
}

function formatTime(secs) {
  const m = String(Math.floor(secs / 60)).padStart(2, '0');
  const s = String(secs % 60).padStart(2, '0');
  return `${m}:${s}`;
}

// ── UI helpers ────────────────────────────────────────────────────────────────
function updateMistakeDisplay() {
  document.getElementById('mistakeCount').textContent = state.mistakes;
  const chip = document.getElementById('mistakesChip');
  chip.style.borderColor = state.mistakes >= 3 ? 'rgba(239,68,68,0.5)' : '';
  chip.style.color       = state.mistakes >= 3 ? '#fca5a5' : '';
}

function updateHintBadge() {
  const left = MAX_HINTS - state.hints;
  document.getElementById('hintCount').textContent = `${left} left`;
}

function setStatus(msg) {
  document.getElementById('statusMsg').textContent = msg;
}

function showLoading(show) {
  document.getElementById('loadingOverlay').hidden = !show;
}

let toastTimer = null;
function showToast(msg, type = '') {
  const toast = document.getElementById('toast');
  toast.textContent = msg;
  toast.className   = `toast ${type}`;
  toast.hidden      = false;
  // Force reflow for animation
  void toast.offsetWidth;
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => { toast.hidden = true; }, 350);
  }, 2800);
}

// ── Standalone Client Engine (Fallback for GitHub Pages / Static Hosting) ──────
const clientEngine = {
  active: false,
  board: [],
  initial: [],
  solution: [],
  fixed: [],
  undoStack: [],
  mistakes: 0,
  hintsUsed: 0,

  init(difficulty) {
    this.undoStack = [];
    this.mistakes = 0;
    this.hintsUsed = 0;

    // 1. Generate full valid base solution
    const nums = [1, 2, 3, 4, 5, 6, 7, 8, 9].sort(() => Math.random() - 0.5);
    const pattern = (r, c) => (3 * (r % 3) + Math.floor(r / 3) + c) % 9;
    
    // Shuffle bands and stacks
    const shuffle3 = () => [0, 1, 2].sort(() => Math.random() - 0.5);
    const rowGroups = [shuffle3(), shuffle3(), shuffle3()];
    const colGroups = [shuffle3(), shuffle3(), shuffle3()];
    const bandShuffle = shuffle3();
    const stackShuffle = shuffle3();

    const rIdx = [];
    bandShuffle.forEach(b => rowGroups[b].forEach(r => rIdx.push(b * 3 + r)));
    const cIdx = [];
    stackShuffle.forEach(s => colGroups[s].forEach(c => cIdx.push(s * 3 + c)));

    this.solution = Array.from({ length: 9 }, (_, r) =>
      Array.from({ length: 9 }, (_, c) => nums[pattern(rIdx[r], cIdx[c])])
    );

    // 2. Remove clues based on difficulty
    const cluesCount = difficulty === 'easy' ? 42 : (difficulty === 'hard' ? 28 : 34);
    const cellsToRemove = 81 - cluesCount;

    this.board = this.solution.map(row => [...row]);
    this.fixed = Array.from({ length: 9 }, () => Array(9).fill(true));

    const positions = [];
    for (let r = 0; r < 9; r++) {
      for (let c = 0; c < 9; c++) positions.push([r, c]);
    }
    positions.sort(() => Math.random() - 0.5);

    for (let i = 0; i < cellsToRemove; i++) {
      const [r, c] = positions[i];
      this.board[r][c] = 0;
      this.fixed[r][c] = false;
    }

    this.initial = this.board.map(row => [...row]);
    return {
      gameId: 'client-offline-' + Date.now(),
      board: this.board,
      fixed: this.fixed,
      difficulty,
      message: 'Running in Standalone Client Mode (GitHub Pages)'
    };
  },

  hasConflict(grid, r, c, val) {
    if (val === 0) return false;
    for (let i = 0; i < 9; i++) {
      if (i !== c && grid[r][i] === val) return true;
      if (i !== r && grid[i][c] === val) return true;
    }
    const br = Math.floor(r / 3) * 3;
    const bc = Math.floor(c / 3) * 3;
    for (let row = br; row < br + 3; row++) {
      for (let col = bc; col < bc + 3; col++) {
        if ((row !== r || col !== c) && grid[row][col] === val) return true;
      }
    }
    return false;
  },

  handle(endpoint, body) {
    if (endpoint === '/new') {
      return this.init(body.difficulty || 'medium');
    }
    if (endpoint === '/move') {
      const { row, col, value } = body;
      const prev = this.board[row][col];
      this.board[row][col] = value;
      this.undoStack.push({ row, col, prev });

      const conflict = this.hasConflict(this.board, row, col, value);
      const valid = (value === 0 || value === this.solution[row][col]);
      if (!valid && value !== 0) {
        this.mistakes++;
      }

      let completed = true;
      for (let r = 0; r < 9; r++) {
        for (let c = 0; c < 9; c++) {
          if (this.board[r][c] === 0 || this.board[r][c] !== this.solution[r][c]) {
            completed = false;
            break;
          }
        }
      }

      return {
        valid,
        conflict: conflict && value !== 0,
        completed,
        mistakes: this.mistakes,
        board: this.board
      };
    }
    if (endpoint === '/hint') {
      for (let r = 0; r < 9; r++) {
        for (let c = 0; c < 9; c++) {
          if (this.board[r][c] === 0) {
            this.hintsUsed++;
            this.board[r][c] = this.solution[r][c];
            return {
              available: true,
              row: r,
              col: c,
              value: this.solution[r][c],
              hintsUsed: this.hintsUsed,
              board: this.board
            };
          }
        }
      }
      return { available: false };
    }
    if (endpoint === '/undo') {
      if (this.undoStack.length === 0) return { available: false };
      const last = this.undoStack.pop();
      this.board[last.row][last.col] = last.prev;
      return {
        available: true,
        row: last.row,
        col: last.col,
        value: last.prev,
        board: this.board
      };
    }
    if (endpoint === '/reset') {
      this.board = this.initial.map(row => [...row]);
      this.undoStack = [];
      this.mistakes = 0;
      return {
        board: this.board,
        fixed: this.fixed,
        message: 'Game reset to original puzzle.'
      };
    }
    if (endpoint === '/check') {
      return { completed: false, mistakes: this.mistakes, hintsUsed: this.hintsUsed };
    }
    return {};
  }
};

// ── API communication ─────────────────────────────────────────────────────────
async function apiPost(endpoint, body) {
  // If already running standalone client mode (e.g. on GitHub Pages)
  if (clientEngine.active) {
    return clientEngine.handle(endpoint, body);
  }

  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 1500);
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(body),
      signal:  controller.signal
    });
    clearTimeout(timeout);
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.error || `HTTP ${response.status}`);
    }
    return await response.json();
  } catch (err) {
    // When backend is unreachable (GitHub Pages deployment or offline), seamless fallback
    console.warn('Backend server not reachable — engaging standalone client engine:', err.message);
    clientEngine.active = true;
    return clientEngine.handle(endpoint, body);
  }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
function getCellEl(row, col) {
  return document.getElementById(`cell-${row}-${col}`);
}

/** Converts a JSONArray-of-JSONArrays (from org.json) to a plain 2D JS array. */
function jsonArrayTo2D(arr) {
  return Array.from(arr, row => Array.from(row));
}
