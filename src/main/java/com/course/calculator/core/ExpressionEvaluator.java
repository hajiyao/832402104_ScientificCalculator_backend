package com.course.calculator.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * 递归下降解析器，不用 eval。
 * 加减 -> 乘除取模 -> 正负号 -> 乘方 -> 阶乘 -> 数字/常量/函数/括号
 */
public final class ExpressionEvaluator {
    private String input;
    private int pos;
    private AngleMode angleMode;
    private double lastAnswer;

    public double evaluate(String expression, AngleMode mode, double previousAnswer)
            throws CalculationException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new CalculationException("请输入表达式");
        }
        input = normalize(expression);
        pos = 0;
        angleMode = mode == null ? AngleMode.DEG : mode;
        lastAnswer = previousAnswer;

        double value = parseExpression();
        skipSpaces();
        if (pos != input.length()) {
            throw error("无法识别的字符");
        }
        ensureFinite(value);
        return value;
    }

    /** double 转显示字符串。 */
    public String format(double value) {
        if (Math.abs(value) < 1e-14) {
            value = 0.0;
        }
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        double magnitude = Math.abs(value);
        if (magnitude != 0.0 && (magnitude >= 1e12 || magnitude < 1e-9)) {
            return String.format(Locale.US, "%.10E", value)
                    .replaceAll("0+E", "E")
                    .replace("E+", "E");
        }
        BigDecimal number = BigDecimal.valueOf(value)
                .setScale(12, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return number.toPlainString();
    }

    private String normalize(String value) {
        return value.replace('×', '*')
                .replace('÷', '/')
                .replace('−', '-')
                .replace('π', 'p');
    }

    private double parseExpression() throws CalculationException {
        double value = parseTerm();
        while (true) {
            if (match('+')) {
                value += parseTerm();
            } else if (match('-')) {
                value -= parseTerm();
            } else {
                return checked(value);
            }
        }
    }

    private double parseTerm() throws CalculationException {
        double value = parseUnary();
        while (true) {
            if (match('*')) {
                value *= parseUnary();
            } else if (match('/')) {
                double divisor = parseUnary();
                if (divisor == 0.0) {
                    throw error("除数不能为 0");
                }
                value /= divisor;
            } else if (match('%')) {
                double divisor = parseUnary();
                if (divisor == 0.0) {
                    throw error("取模时除数不能为 0");
                }
                value %= divisor;
            } else if (matchWord("mod")) {
                double divisor = parseUnary();
                if (divisor == 0.0) {
                    throw error("取模时除数不能为 0");
                }
                value %= divisor;
            } else if (isImplicitMultiplication()) {
                value *= parseUnary();
            } else {
                return checked(value);
            }
        }
    }

    /** 一元正负号。 */
    private double parseUnary() throws CalculationException {
        if (match('+')) {
            return parseUnary();
        }
        if (match('-')) {
            return -parseUnary();
        }
        return parsePower();
    }

    /** 乘方右结合。 */
    private double parsePower() throws CalculationException {
        double base = parsePostfix();
        if (match('^')) {
            base = Math.pow(base, parseUnary());
        }
        return checked(base);
    }

    private double parsePostfix() throws CalculationException {
        double value = parsePrimary();
        while (match('!')) {
            value = factorial(value);
        }
        return value;
    }

    private double parsePrimary() throws CalculationException {
        skipSpaces();
        if (match('(')) {
            double value = parseExpression();
            expect(')');
            return value;
        }
        if (pos < input.length()
                && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            return parseNumber();
        }
        if (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            String name = parseIdentifier().toLowerCase(Locale.US);
            if ("pi".equals(name) || "p".equals(name)) {
                return Math.PI;
            }
            if ("e".equals(name)) {
                return Math.E;
            }
            if ("ans".equals(name)) {
                return lastAnswer;
            }
            expect('(');
            double argument = parseExpression();
            expect(')');
            return applyFunction(name, argument);
        }
        if (pos >= input.length()) {
            throw error("表达式不完整");
        }
        throw error("此处应为数字、常量或函数");
    }

    private double parseNumber() throws CalculationException {
        int start = pos;
        boolean hasDigits = false;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
            hasDigits = true;
        }
        if (pos < input.length() && input.charAt(pos) == '.') {
            pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                pos++;
                hasDigits = true;
            }
        }
        if (!hasDigits) {
            throw error("小数格式错误");
        }
        // 科学计数法
        if (pos < input.length() && (input.charAt(pos) == 'E' || input.charAt(pos) == 'e')) {
            int mark = pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
                pos++;
            }
            int expStart = pos;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                pos++;
            }
            if (expStart == pos) {
                pos = mark;
            }
        }
        try {
            return Double.parseDouble(input.substring(start, pos));
        } catch (NumberFormatException ex) {
            throw error("数字格式错误");
        }
    }

    private String parseIdentifier() {
        int start = pos;
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            pos++;
        }
        return input.substring(start, pos);
    }

    private double applyFunction(String name, double value) throws CalculationException {
        double result;
        switch (name) {
            case "sin":
                result = Math.sin(toRadians(value));
                break;
            case "cos":
                result = Math.cos(toRadians(value));
                break;
            case "tan":
                double radians = toRadians(value);
                if (Math.abs(Math.cos(radians)) < 1e-14) {
                    throw error("tan 在该角度没有定义");
                }
                result = Math.tan(radians);
                break;
            case "asin":
                requireRange(value, -1.0, 1.0, "asin 的参数范围为 [-1, 1]");
                result = fromRadians(Math.asin(value));
                break;
            case "acos":
                requireRange(value, -1.0, 1.0, "acos 的参数范围为 [-1, 1]");
                result = fromRadians(Math.acos(value));
                break;
            case "atan":
                result = fromRadians(Math.atan(value));
                break;
            case "sqrt":
                if (value < 0.0) {
                    throw error("负数不能开平方根");
                }
                result = Math.sqrt(value);
                break;
            case "ln":
                if (value <= 0.0) {
                    throw error("ln 的参数必须大于 0");
                }
                result = Math.log(value);
                break;
            case "log":
                if (value <= 0.0) {
                    throw error("log 的参数必须大于 0");
                }
                result = Math.log10(value);
                break;
            case "exp":
                result = Math.exp(value);
                break;
            case "abs":
                result = Math.abs(value);
                break;
            default:
                throw error("未知函数：" + name);
        }
        return checked(result);
    }

    private double factorial(double value) throws CalculationException {
        if (value < 0 || value != Math.rint(value)) {
            throw error("阶乘只适用于非负整数");
        }
        if (value > 170) {
            throw error("阶乘结果过大（最大支持 170!）");
        }
        double result = 1.0;
        for (int i = 2; i <= (int) value; i++) {
            result *= i;
        }
        return result;
    }

    private double toRadians(double value) {
        return angleMode == AngleMode.DEG ? Math.toRadians(value) : value;
    }

    private double fromRadians(double value) {
        return angleMode == AngleMode.DEG ? Math.toDegrees(value) : value;
    }

    private void requireRange(double value, double min, double max, String message)
            throws CalculationException {
        if (value < min || value > max) {
            throw error(message);
        }
    }

    /** 2(3+4)、2pi 这种省略乘号。 */
    private boolean isImplicitMultiplication() {
        skipSpaces();
        if (pos >= input.length()) {
            return false;
        }
        char ch = input.charAt(pos);
        return ch == '(' || Character.isLetter(ch);
    }

    private boolean match(char expected) {
        skipSpaces();
        if (pos < input.length() && input.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    /** 匹配字母操作符（mod），后面不能还是字母。 */
    private boolean matchWord(String word) {
        skipSpaces();
        int end = pos + word.length();
        if (end > input.length()) {
            return false;
        }
        String segment = input.substring(pos, end).toLowerCase(Locale.US);
        if (!segment.equals(word)) {
            return false;
        }
        if (end < input.length() && Character.isLetter(input.charAt(end))) {
            return false;
        }
        pos = end;
        return true;
    }

    private void expect(char expected) throws CalculationException {
        if (!match(expected)) {
            throw error("缺少 " + expected);
        }
    }

    private void skipSpaces() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private double checked(double value) throws CalculationException {
        ensureFinite(value);
        return value;
    }

    private void ensureFinite(double value) throws CalculationException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw error("计算结果超出有效范围");
        }
    }

    /** 出错位置从 1 开始数。 */
    private CalculationException error(String message) {
        return new CalculationException(message + "（位置 " + (pos + 1) + "）");
    }
}
