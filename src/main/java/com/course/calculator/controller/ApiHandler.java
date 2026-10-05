package com.course.calculator.controller;

import com.course.calculator.core.AngleMode;
import com.course.calculator.core.CalculationException;
import com.course.calculator.model.CalculationRecord;
import com.course.calculator.service.CalculatorService;
import com.course.calculator.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/** /api 路由。 */
public final class ApiHandler implements HttpHandler {
    private final CalculatorService calculatorService;

    public ApiHandler(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equals(method) && "/api/health".equals(path)) {
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"服务运行中\"}");
            } else if ("POST".equals(method) && "/api/calculate".equals(path)) {
                handleCalculate(exchange);
            } else if ("GET".equals(method) && "/api/history".equals(path)) {
                handleListHistory(exchange);
            } else if ("DELETE".equals(method) && "/api/history".equals(path)) {
                calculatorService.clearHistory();
                sendJson(exchange, 200, "{\"success\":true}");
            } else if ("DELETE".equals(method) && path.startsWith("/api/history/")) {
                handleDeleteHistory(exchange, path);
            } else {
                sendJson(exchange, 404, "{\"success\":false,\"message\":\"接口不存在\"}");
            }
        } catch (Exception ex) {
            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"服务器内部错误\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleCalculate(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String expression = JsonUtil.readStringField(body, "expression");
        if (expression == null || expression.trim().isEmpty()) {
            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"请输入表达式\"}");
            return;
        }
        String modeText = JsonUtil.readStringField(body, "angleMode");
        AngleMode mode = "RAD".equalsIgnoreCase(modeText) ? AngleMode.RAD : AngleMode.DEG;
        try {
            CalculationRecord record = calculatorService.calculate(expression, mode);
            String json = "{\"success\":true,"
                    + "\"expression\":\"" + JsonUtil.escape(record.getExpression()) + "\","
                    + "\"result\":\"" + JsonUtil.escape(record.getResult()) + "\","
                    + "\"id\":" + record.getId() + ","
                    + "\"createdAt\":\"" + JsonUtil.escape(record.getCreatedAt()) + "\"}";
            sendJson(exchange, 200, json);
        } catch (CalculationException ex) {
            String json = "{\"success\":false,\"message\":\""
                    + JsonUtil.escape(ex.getMessage()) + "\"}";
            sendJson(exchange, 400, json);
        }
    }

    private void handleListHistory(HttpExchange exchange) throws IOException, SQLException {
        List<CalculationRecord> records = calculatorService.listHistory();
        StringBuilder builder = new StringBuilder();
        builder.append("{\"success\":true,\"data\":[");
        for (int i = 0; i < records.size(); i++) {
            CalculationRecord record = records.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"id\":").append(record.getId())
                    .append(",\"expression\":\"").append(JsonUtil.escape(record.getExpression()))
                    .append("\",\"result\":\"").append(JsonUtil.escape(record.getResult()))
                    .append("\",\"createdAt\":\"").append(JsonUtil.escape(record.getCreatedAt()))
                    .append("\"}");
        }
        builder.append("]}");
        sendJson(exchange, 200, builder.toString());
    }

    private void handleDeleteHistory(HttpExchange exchange, String path) throws IOException {
        String idText = path.substring("/api/history/".length());
        long id;
        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException ex) {
            sendJson(exchange, 400,
                    "{\"success\":false,\"message\":\"记录 id 不合法\"}");
            return;
        }
        try {
            boolean deleted = calculatorService.deleteHistory(id);
            if (deleted) {
                sendJson(exchange, 200, "{\"success\":true}");
            } else {
                sendJson(exchange, 404,
                        "{\"success\":false,\"message\":\"历史记录不存在\"}");
            }
        } catch (SQLException ex) {
            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"删除失败，请稍后重试\"}");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            char[] buffer = new char[2048];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, length);
            }
        }
        return builder.toString();
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
                "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
