package com.course.calculator.model;

/** 一条历史记录，对应 calculation_history 表一行。 */
public final class CalculationRecord {
    private final long id;
    private final String expression;
    private final String result;
    private final String createdAt;

    public CalculationRecord(long id, String expression, String result, String createdAt) {
        this.id = id;
        this.expression = expression;
        this.result = result;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
