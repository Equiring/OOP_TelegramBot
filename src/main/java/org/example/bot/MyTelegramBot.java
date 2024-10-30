package org.example.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "YourBotUsername"; // Замените на имя вашего бота
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN"); // Получение токена из переменной окружения
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendMessage(chatId, "Привет! Я ваш помощник по задачам. Напишите /help для списка команд.");
            } else if (messageText.equals("/help")) {
                sendHelpMessage(chatId);
            } else if (messageText.equals("/addtask")) {
                sendMessage(chatId, "Введите текст новой задачи в формате 'Задача: описание задачи'");
            } else if (messageText.startsWith("Задача: ")) {
                String taskText = messageText.replace("Задача: ", "");
                addTask(chatId, taskText);
            } else if (messageText.equals("/viewtasks")) {
                sendTasksList(chatId);
            } else if (messageText.startsWith("/deletetask ")) {
                String idStr = messageText.replace("/deletetask ", "").trim();
                try {
                    int taskId = Integer.parseInt(idStr);
                    deleteTask(chatId, taskId);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Неверный формат ID задачи. Пожалуйста, укажите числовой ID.");
                }
            } else if (messageText.startsWith("/completetask ")) {
                String idStr = messageText.replace("/completetask ", "").trim();
                try {
                    int taskId = Integer.parseInt(idStr);
                    completeTask(chatId, taskId);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Неверный формат ID задачи. Пожалуйста, укажите числовой ID.");
                }
            }
        }
    }

    private void sendHelpMessage(long chatId) {
        String helpMessage = "Список команд:\n" +
                "/start - Начать работу с ботом\n" +
                "/help - Показать справку по командам\n" +
                "/addtask - Добавить новую задачу\n" +
                "/viewtasks - Показать список задач\n" +
                "/deletetask <ID> - Удалить задачу по ID\n" +
                "/completetask <ID> - Отметить задачу как завершенную по ID";
        sendMessage(chatId, helpMessage);
    }

    private void addTask(long chatId, String taskText) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.addTask(chatId, taskText);
        sendMessage(chatId, "Задача добавлена: " + taskText);
    }

    private void sendTasksList(long chatId) {
        DatabaseManager dbManager = new DatabaseManager();
        List<String> tasks = dbManager.getUserTasks(chatId);

        if (tasks.isEmpty()) {
            sendMessage(chatId, "У вас нет активных задач.");
        } else {
            StringBuilder response = new StringBuilder("Ваши задачи:\n");
            for (String task : tasks) {
                response.append(task).append("\n");
            }
            sendMessage(chatId, response.toString());
        }
    }

    private void deleteTask(long chatId, int taskId) {
        DatabaseManager dbManager = new DatabaseManager();
        boolean success = dbManager.deleteTask(taskId, chatId);
        if (success) {
            sendMessage(chatId, "Задача с ID " + taskId + " успешно удалена.");
        } else {
            sendMessage(chatId, "Задача с ID " + taskId + " не найдена.");
        }
    }

    private void completeTask(long chatId, int taskId) {
        DatabaseManager dbManager = new DatabaseManager();
        boolean success = dbManager.completeTask(taskId, chatId);
        if (success) {
            sendMessage(chatId, "Задача с ID " + taskId + " успешно завершена.");
        } else {
            sendMessage(chatId, "Задача с ID " + taskId + " не найдена или уже завершена.");
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}