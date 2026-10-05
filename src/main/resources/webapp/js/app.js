(function () {
    'use strict';

    var API_BASE = window.API_BASE || '';

    var exprEl = document.getElementById('expression');
    var resultEl = document.getElementById('result');
    var errorEl = document.getElementById('errorText');
    var modeBtn = document.getElementById('modeBtn');
    var modeBadge = document.getElementById('modeBadge');
    var historyList = document.getElementById('historyList');
    var historyEmpty = document.getElementById('historyEmpty');
    var clearAllBtn = document.getElementById('clearAllBtn');

    var expression = '';
    var mode = 'DEG';
    var lastResultText = null;
    var justCalculated = false;

    // 算完之后按这些键，就拿上一次结果接着算
    var OPERATOR_TOKENS = ['÷', '×', '-', '+', '^', '%', '!', ')', '.'];

    function appendToken(token) {
        clearError();
        if (justCalculated) {
            if (OPERATOR_TOKENS.indexOf(token) !== -1 && lastResultText !== null) {
                expression = lastResultText;
            } else {
                expression = '';
            }
            justCalculated = false;
        }
        expression += token;
        render();
    }

    function backspace() {
        clearError();
        justCalculated = false;
        expression = expression.slice(0, -1);
        render();
    }

    function clearAll() {
        expression = '';
        lastResultText = null;
        justCalculated = false;
        clearError();
        render();
    }

    function render() {
        exprEl.textContent = expression;
        if (justCalculated && lastResultText !== null) {
            resultEl.textContent = lastResultText;
            resultEl.classList.remove('error');
        } else if (!expression) {
            resultEl.textContent = '0';
            resultEl.classList.remove('error');
        }
    }

    function showError(message) {
        errorEl.textContent = message;
        resultEl.textContent = message;
        resultEl.classList.add('error');
    }

    function clearError() {
        errorEl.textContent = '';
        resultEl.classList.remove('error');
    }

    function toggleMode() {
        mode = mode === 'DEG' ? 'RAD' : 'DEG';
        modeBtn.textContent = mode;
        modeBadge.textContent = mode;
    }

    function calculate() {
        clearError();
        if (!expression.trim()) {
            showError('请输入表达式');
            return;
        }
        request('/api/calculate', {
            method: 'POST',
            body: JSON.stringify({ expression: expression, angleMode: mode })
        }).then(function (data) {
            lastResultText = data.result;
            justCalculated = true;
            render();
            loadHistory();
        }).catch(function (err) {
            showError(err.message);
        });
    }

    function loadHistory() {
        request('/api/history').then(function (data) {
            renderHistory(data.data || []);
        }).catch(function () {
            renderHistory([]);
        });
    }

    function deleteRecord(id) {
        request('/api/history/' + id, { method: 'DELETE' })
            .then(loadHistory)
            .catch(function (err) {
                showError(err.message);
            });
    }

    function clearHistoryAll() {
        request('/api/history', { method: 'DELETE' })
            .then(loadHistory)
            .catch(function (err) {
                showError(err.message);
            });
    }

    function request(path, options) {
        options = options || {};
        options.headers = { 'Content-Type': 'application/json' };
        return fetch(API_BASE + path, options).then(function (response) {
            return response.json().then(function (data) {
                if (!response.ok || data.success === false) {
                    throw new Error(data.message || '请求失败');
                }
                return data;
            });
        }).catch(function (err) {
            if (err instanceof TypeError) {
                throw new Error('无法连接后端服务，请确认后端已启动');
            }
            throw err;
        });
    }

    function renderHistory(records) {
        historyList.innerHTML = '';
        historyEmpty.style.display = records.length ? 'none' : 'block';

        records.forEach(function (record) {
            var li = document.createElement('li');
            li.className = 'history-item';

            var main = document.createElement('div');
            main.className = 'history-main';

            var expr = document.createElement('div');
            expr.className = 'history-expr';
            expr.textContent = record.expression + ' =';

            var result = document.createElement('div');
            result.className = 'history-result';
            result.textContent = record.result;

            main.appendChild(expr);
            main.appendChild(result);
            // 点一下把表达式填回去
            main.addEventListener('click', function () {
                expression = record.expression;
                justCalculated = false;
                clearError();
                render();
            });

            var time = document.createElement('span');
            time.className = 'history-time';
            time.textContent = record.createdAt;

            var del = document.createElement('button');
            del.className = 'btn-delete';
            del.textContent = '×';
            del.title = '删除该记录';
            del.addEventListener('click', function () {
                deleteRecord(record.id);
            });

            li.appendChild(main);
            li.appendChild(time);
            li.appendChild(del);
            historyList.appendChild(li);
        });
    }

    document.querySelector('.keypad').addEventListener('click', function (event) {
        var target = event.target;
        if (target.tagName !== 'BUTTON') {
            return;
        }
        var action = target.getAttribute('data-action');
        if (action === 'calculate') {
            calculate();
        } else if (action === 'clear') {
            clearAll();
        } else if (action === 'backspace') {
            backspace();
        } else if (action === 'toggleMode') {
            toggleMode();
        } else {
            var token = target.getAttribute('data-token');
            if (token !== null) {
                appendToken(token);
            }
        }
    });

    document.addEventListener('keydown', function (event) {
        var key = event.key;
        if (/^[0-9.()^%!]$/.test(key)) {
            appendToken(key);
        } else if (/^[a-zA-Z]$/.test(key)) {
            appendToken(key);
        } else if (key === '+') {
            appendToken('+');
        } else if (key === '-') {
            appendToken('-');
        } else if (key === '*') {
            appendToken('×');
        } else if (key === '/') {
            event.preventDefault();
            appendToken('÷');
        } else if (key === 'Enter' || key === '=') {
            event.preventDefault();
            calculate();
        } else if (key === 'Backspace') {
            backspace();
        } else if (key === 'Escape') {
            clearAll();
        }
    });

    clearAllBtn.addEventListener('click', clearHistoryAll);

    loadHistory();
})();
