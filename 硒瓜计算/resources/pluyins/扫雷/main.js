(function() {
    'use strict';

    const DIFFICULTIES = {
        easy:   { rows: 9,  cols: 9,  mines: 10 },
        medium: { rows: 16, cols: 16, mines: 40 },
        hard:   { rows: 16, cols: 30, mines: 99 }
    };

    let current = 'easy';
    let board = [];
    let revealed = [];
    let flagged = [];
    let mines = [];
    let gameOver = false;
    let firstClick = true;
    let flagMode = false;
    let timer = 0;
    let timerHandle = null;
    let openedCount = 0;

    const $ = id => document.getElementById(id);
    const boardEl = $('msBoard');
    const minesEl = $('msMines');
    const timeEl = $('msTime');
    const faceEl = $('msFace');
    const messageEl = $('msMessage');
    const diffSelect = document.querySelector('.ms-difficulty');
    const modeBtn = document.querySelector('.ms-mode-btn');
    const resetBtn = document.querySelector('.ms-reset');

    function init() {
        const cfg = DIFFICULTIES[current];
        boardEl.innerHTML = '';
        boardEl.style.gridTemplateColumns = `repeat(${cfg.cols}, 28px)`;
        board = Array(cfg.rows).fill(0).map(() => Array(cfg.cols).fill(0));
        revealed = Array(cfg.rows).fill(0).map(() => Array(cfg.cols).fill(false));
        flagged = Array(cfg.rows).fill(0).map(() => Array(cfg.cols).fill(false));
        mines = [];
        gameOver = false;
        firstClick = true;
        timer = 0;
        openedCount = 0;
        timeEl.textContent = '0';
        minesEl.textContent = cfg.mines;
        faceEl.textContent = '😊';
        messageEl.textContent = '';
        messageEl.className = 'ms-message';

        if (timerHandle) { clearInterval(timerHandle); timerHandle = null; }

        for (let r = 0; r < cfg.rows; r++) {
            for (let c = 0; c < cfg.cols; c++) {
                const cell = document.createElement('div');
                cell.className = 'ms-cell';
                cell.dataset.r = r;
                cell.dataset.c = c;

                // 左键点击
                cell.addEventListener('click', function(e) {
                    e.preventDefault();
                    onCellClick(r, c);
                });

                // 右键插旗
                cell.addEventListener('contextmenu', function(e) {
                    e.preventDefault();
                    onCellRightClick(r, c);
                });

                // 移动端长按插旗
                let longPressTimer = null;
                cell.addEventListener('touchstart', function() {
                    longPressTimer = setTimeout(() => {
                        longPressTimer = null;
                        onCellRightClick(r, c);
                    }, 500);
                }, { passive: true });
                cell.addEventListener('touchend', function() {
                    if (longPressTimer) { clearTimeout(longPressTimer); longPressTimer = null; }
                });
                cell.addEventListener('touchmove', function() {
                    if (longPressTimer) { clearTimeout(longPressTimer); longPressTimer = null; }
                });

                boardEl.appendChild(cell);
            }
        }
    }

    function placeMines(safeR, safeC) {
        const cfg = DIFFICULTIES[current];
        let placed = 0;
        while (placed < cfg.mines) {
            const r = Math.floor(Math.random() * cfg.rows);
            const c = Math.floor(Math.random() * cfg.cols);
            if (Math.abs(r - safeR) <= 1 && Math.abs(c - safeC) <= 1) continue;
            if (mines.some(m => m.r === r && m.c === c)) continue;
            mines.push({ r, c });
            board[r][c] = -1;
            placed++;
        }
        // 计算每个格子的数字
        for (let r = 0; r < cfg.rows; r++) {
            for (let c = 0; c < cfg.cols; c++) {
                if (board[r][c] === -1) continue;
                let count = 0;
                for (let dr = -1; dr <= 1; dr++) {
                    for (let dc = -1; dc <= 1; dc++) {
                        if (dr === 0 && dc === 0) continue;
                        const nr = r + dr, nc = c + dc;
                        if (nr < 0 || nr >= cfg.rows || nc < 0 || nc >= cfg.cols) continue;
                        if (board[nr][nc] === -1) count++;
                    }
                }
                board[r][c] = count;
            }
        }
    }

    function startTimer() {
        if (timerHandle) return;
        timerHandle = setInterval(() => {
            timer++;
            if (timer > 999) { timer = 999; }
            timeEl.textContent = timer;
        }, 1000);
    }

    function onCellClick(r, c) {
        if (gameOver) return;
        if (revealed[r][c]) return;
        if (flagMode) { onCellRightClick(r, c); return; }
        if (flagged[r][c]) return;

        if (firstClick) {
            placeMines(r, c);
            firstClick = false;
            startTimer();
        }
        reveal(r, c);
        checkWin();
    }

    function onCellRightClick(r, c) {
        if (gameOver) return;
        if (revealed[r][c]) return;
        const cfg = DIFFICULTIES[current];
        flagged[r][c] = !flagged[r][c];
        updateCell(r, c);
        const flagCount = flagged.flat().filter(Boolean).length;
        minesEl.textContent = cfg.mines - flagCount;
    }

    function reveal(r, c) {
        const cfg = DIFFICULTIES[current];
        if (r < 0 || r >= cfg.rows || c < 0 || c >= cfg.cols) return;
        if (revealed[r][c] || flagged[r][c]) return;

        revealed[r][c] = true;
        openedCount++;
        updateCell(r, c);

        if (board[r][c] === -1) {
            gameOver = true;
            faceEl.textContent = '😵';
            messageEl.textContent = '💥 踩到雷了！点击"重开"再来一局';
            messageEl.className = 'ms-message lose';
            for (const m of mines) {
                revealed[m.r][m.c] = true;
                updateCell(m.r, m.c);
            }
            if (timerHandle) { clearInterval(timerHandle); timerHandle = null; }
            return;
        }

        if (board[r][c] === 0) {
            for (let dr = -1; dr <= 1; dr++) {
                for (let dc = -1; dc <= 1; dc++) {
                    if (dr === 0 && dc === 0) continue;
                    const nr = r + dr, nc = c + dc;
                    if (nr < 0 || nr >= cfg.rows || nc < 0 || nc >= cfg.cols) continue;
                    if (!revealed[nr][nc]) reveal(nr, nc);
                }
            }
        }
    }

    function updateCell(r, c) {
        const cell = boardEl.querySelector(`.ms-cell[data-r="${r}"][data-c="${c}"]`);
        if (!cell) return;
        cell.className = 'ms-cell';
        if (revealed[r][c]) {
            cell.classList.add('revealed');
            if (board[r][c] === -1) {
                cell.textContent = '💣';
                cell.classList.add('mine');
            } else if (board[r][c] === 0) {
                cell.textContent = '';
            } else {
                cell.textContent = board[r][c];
                cell.classList.add('n' + board[r][c]);
            }
        } else if (flagged[r][c]) {
            cell.textContent = '🚩';
            cell.classList.add('flagged');
        } else {
            cell.textContent = '';
        }
    }

    function checkWin() {
        const cfg = DIFFICULTIES[current];
        const total = cfg.rows * cfg.cols - cfg.mines;
        if (openedCount >= total) {
            gameOver = true;
            faceEl.textContent = '😎';
            messageEl.textContent = `🎉 恭喜获胜！用时 ${timer} 秒`;
            messageEl.className = 'ms-message win';
            if (timerHandle) { clearInterval(timerHandle); timerHandle = null; }
        }
    }

    diffSelect.addEventListener('change', function() {
        current = this.value;
        init();
    });

    resetBtn.addEventListener('click', function() {
        init();
    });

    faceEl.addEventListener('click', function() {
        init();
    });

    modeBtn.addEventListener('click', function() {
        flagMode = !flagMode;
        modeBtn.textContent = `🚩 标记模式：${flagMode ? '开' : '关'}`;
        modeBtn.classList.toggle('active', flagMode);
    });

    init();
})();