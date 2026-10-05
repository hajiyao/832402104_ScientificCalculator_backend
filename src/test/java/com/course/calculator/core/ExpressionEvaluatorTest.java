package com.course.calculator.core;

/** 解析器测试，直接运行 main。 */
public final class ExpressionEvaluatorTest {
    private static final double EPSILON = 1e-10;
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private int passed;

    public static void main(String[] args) throws Exception {
        new ExpressionEvaluatorTest().run();
    }

    private void run() throws Exception {
        assertValue("2+3*4", AngleMode.DEG, 14.0);
        assertValue("(2+3)*4", AngleMode.DEG, 20.0);
        assertValue("2^3^2", AngleMode.DEG, 512.0);
        assertValue("-2^2", AngleMode.DEG, -4.0);
        assertValue("3!+sqrt(16)", AngleMode.DEG, 10.0);
        assertValue("sin(30)", AngleMode.DEG, 0.5);
        assertValue("cos(pi)", AngleMode.RAD, -1.0);
        assertValue("asin(1)", AngleMode.DEG, 90.0);
        assertValue("log(1000)", AngleMode.DEG, 3.0);
        assertValue("ln(e)", AngleMode.DEG, 1.0);
        assertValue("2pi", AngleMode.DEG, 2.0 * Math.PI);
        assertValue("2(3+4)", AngleMode.DEG, 14.0);
        assertValue("8 mod 3", AngleMode.DEG, 2.0);
        assertValue("3*-2", AngleMode.DEG, -6.0);
        assertValue("ans*2", AngleMode.DEG, 10.0, 5.0);

        assertFailure("1/0", AngleMode.DEG);
        assertFailure("sqrt(-1)", AngleMode.DEG);
        assertFailure("3.2!", AngleMode.DEG);
        assertFailure("sin(", AngleMode.DEG);
        assertFailure("2+*3", AngleMode.DEG);

        System.out.println("All tests passed: " + passed);
    }

    private void assertValue(String expression, AngleMode mode, double expected)
            throws CalculationException {
        assertValue(expression, mode, expected, 0.0);
    }

    private void assertValue(String expression, AngleMode mode, double expected, double answer)
            throws CalculationException {
        double actual = evaluator.evaluate(expression, mode, answer);
        if (Math.abs(actual - expected) > EPSILON) {
            throw new AssertionError(expression + ": expected " + expected + ", got " + actual);
        }
        passed++;
    }

    private void assertFailure(String expression, AngleMode mode) throws Exception {
        try {
            evaluator.evaluate(expression, mode, 0.0);
            throw new AssertionError(expression + ": expected CalculationException");
        } catch (CalculationException expected) {
            passed++;
        }
    }
}
