package com.course.calculator.core;

/** 表达式或运算非法时抛出，message 可以直接显示给用户。 */
public class CalculationException extends Exception {
    public CalculationException(String message) {
        super(message);
    }
}
