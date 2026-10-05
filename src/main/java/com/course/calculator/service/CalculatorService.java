package com.course.calculator.service;

import com.course.calculator.core.AngleMode;
import com.course.calculator.core.CalculationException;
import com.course.calculator.core.ExpressionEvaluator;
import com.course.calculator.db.HistoryRepository;
import com.course.calculator.model.CalculationRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 计算并入库。 */
public final class CalculatorService {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final HistoryRepository historyRepository;
    private double lastAnswer;

    public CalculatorService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public CalculationRecord calculate(String expression, AngleMode mode)
            throws CalculationException {
        double value = evaluator.evaluate(expression, mode, lastAnswer);
        String result = evaluator.format(value);
        lastAnswer = value;
        String createdAt = LocalDateTime.now().format(TIME_FORMAT);
        try {
            long id = historyRepository.insert(expression.trim(), result, createdAt);
            return new CalculationRecord(id, expression.trim(), result, createdAt);
        } catch (SQLException ex) {
            throw new CalculationException("历史记录保存失败，请稍后重试");
        }
    }

    public List<CalculationRecord> listHistory() throws SQLException {
        return historyRepository.findAll();
    }

    public boolean deleteHistory(long id) throws SQLException {
        return historyRepository.deleteById(id);
    }

    public void clearHistory() throws SQLException {
        historyRepository.deleteAll();
    }
}
