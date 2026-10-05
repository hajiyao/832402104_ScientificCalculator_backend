package com.course.calculator.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/** 托管前端静态文件，文件在 jar 内 /webapp 下。 */
public final class StaticFileHandler implements HttpHandler {
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put(".html", "text/html; charset=UTF-8");
        CONTENT_TYPES.put(".css", "text/css; charset=UTF-8");
        CONTENT_TYPES.put(".js", "application/javascript; charset=UTF-8");
        CONTENT_TYPES.put(".json", "application/json; charset=UTF-8");
        CONTENT_TYPES.put(".png", "image/png");
        CONTENT_TYPES.put(".jpg", "image/jpeg");
        CONTENT_TYPES.put(".svg", "image/svg+xml");
        CONTENT_TYPES.put(".ico", "image/x-icon");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())
                    && !"HEAD".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            if ("/".equals(path)) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            InputStream inputStream = getClass().getResourceAsStream("/webapp" + path);
            if (inputStream == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = readAll(inputStream);
            exchange.getResponseHeaders().set("Content-Type", getContentType(path));
            exchange.sendResponseHeaders(200, bytes.length);
            if ("GET".equals(exchange.getRequestMethod())) {
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(bytes);
                }
            }
        } finally {
            exchange.close();
        }
    }

    private byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    private String getContentType(String path) {
        int index = path.lastIndexOf('.');
        if (index >= 0) {
            String type = CONTENT_TYPES.get(path.substring(index).toLowerCase());
            if (type != null) {
                return type;
            }
        }
        return "application/octet-stream";
    }
}
