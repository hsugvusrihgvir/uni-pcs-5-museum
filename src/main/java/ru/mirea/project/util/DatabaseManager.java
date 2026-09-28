package ru.mirea.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseManager {

    private static final Dotenv dotenv = Dotenv.load();
    
    // раздельные части данных из .env
    private static final String HOST = dotenv.get("DB_HOST");
    private static final String PORT = dotenv.get("DB_PORT");
    private static final String NAME = dotenv.get("DB_NAME");
    
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private DatabaseManager() {
    }

    // создает и возвращает подключение, динамически склеивая URL
    public static Connection getConnection() throws SQLException {
        // jdbc:postgresql://хост:порт/имя_базы
        String url = String.format("jdbc:postgresql://%s:%s/%s", HOST, PORT, NAME);
        
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}
