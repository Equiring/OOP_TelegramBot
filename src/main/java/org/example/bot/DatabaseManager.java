package org.example.bot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public DatabaseManager() {
        createTasksTable();
    }

    // Метод для создания таблицы задач, если ее нет в базе данных
    private void createTasksTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS tasks (" +
                             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                             "user_id INTEGER," +
                             "task_text TEXT," +
                             "status TEXT DEFAULT 'pending')"
             )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для добавления новой задачи в базу данных
    public void addTask(long userId, String taskText) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO tasks (user_id, task_text) VALUES (?, ?)"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, taskText);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения списка активных задач пользователя
    public List<String> getUserTasks(long userId) {
        List<String> tasks = new ArrayList<>();
        String query = "SELECT id, task_text FROM tasks WHERE user_id = ? AND status = 'pending'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String taskText = rs.getString("task_text");
                    tasks.add(id + ": " + taskText);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Метод для удаления задачи по ID задачи и ID пользователя
    public boolean deleteTask(int taskId, long userId) {
        String query = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setLong(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для завершения задачи, обновляет статус на "completed"
    public boolean completeTask(int taskId, long userId) {
        String query = "UPDATE tasks SET status = 'completed' WHERE id = ? AND user_id = ? AND status = 'pending'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            stmt.setLong(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}