package ru.mirea.project;

import ru.mirea.project.ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = ConsoleUI.getInstance();
        ui.start();
    }
}