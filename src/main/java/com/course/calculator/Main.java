package com.course.calculator;

import com.course.calculator.controller.ApiHandler;
import com.course.calculator.controller.StaticFileHandler;
import com.course.calculator.db.DatabaseManager;
import com.course.calculator.db.HistoryRepository;
import com.course.calculator.service.CalculatorService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/** 启动 HTTP 服务。 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);
        String dbPath = System.getenv("CALC_DB");
        if (dbPath == null || dbPath.trim().isEmpty()) {
            dbPath = "data/calculator.db";
        }

        DatabaseManager databaseManager = new DatabaseManager(dbPath);
        HistoryRepository historyRepository = new HistoryRepository(databaseManager);
        CalculatorService calculatorService = new CalculatorService(historyRepository);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler(calculatorService));
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            databaseManager.close();
        }));

        System.out.println("服务已启动，端口 " + port);
        System.out.println("访问 http://localhost:" + port);
    }

    /** 端口：环境变量 PORT > 启动参数 > 8080。 */
    private static int resolvePort(String[] args) {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return Integer.parseInt(envPort.trim());
        }
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        return 8080;
    }
}
