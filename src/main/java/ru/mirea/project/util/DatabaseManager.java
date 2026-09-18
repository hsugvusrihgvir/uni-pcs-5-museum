package ru.mirea.project.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseManager {
    // final по типу const
    // static - объект принажлежит самому классу а не объекту
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // private в конструкторе чтобы нльзя было создать объект извне
    private DatabaseManager(){
    }
    // Connection представляет открытое соединение с базой данных
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
