package org.example.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
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
                sendMessage(chatId, "Привет! Я ваш помощник по задачам. Вот доступные команды:");
                sendReplyKeyboard(chatId);  // Отправляем клавиатуру после команды /start
            } else if (messageText.equals("/help")) {
                sendHelpMessage(chatId);
            } else if (messageText.equals("/addtask")) {
                sendMessage(chatId, "Введите текст новой задачи в формате 'Задача: описание задачи'");
                sendDynamicKeyboard(chatId, "Выберите дальнейшее действие:", List.of("/viewtasks", "Вернуться в меню"));
            } else if (messageText.startsWith("Задача: ")) {
                String taskText = messageText.replace("Задача: ", "");
                addTask(chatId, taskText);
                sendMessage(chatId, "Задача добавлена: " + taskText);
            } else if (messageText.equals("/viewtasks")) {
                sendTasksList(chatId);
            } else if (messageText.equals("/completedtasks")) {
                sendCompletedTasksList(chatId);
            } else if (messageText.startsWith("/deletetask ")) {
                String idStr = messageText.replace("/deletetask ", "").trim();
                try {
                    int taskId = Integer.parseInt(idStr);
                    confirmDeleteTask(chatId, taskId); // Запрос на подтверждение удаления
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
            } else {
                sendDynamicKeyboard(chatId, "Неизвестная команда. Попробуйте одну из следующих:",
                        List.of("/start", "/help", "/viewtasks", "/addtask"));
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.startsWith("confirm_delete:")) {
                int taskId = Integer.parseInt(callbackData.split(":")[1]);
                deleteTask(chatId, taskId);
                editMessageText(chatId, messageId, "Задача с ID " + taskId + " успешно удалена.");
            } else if (callbackData.equals("cancel_delete")) {
                editMessageText(chatId, messageId, "Удаление отменено.");
            }
        }
    }

    private void sendHelpMessage(long chatId) {
        String helpMessage = "Список команд:\n" +
                "/start - Начать работу с ботом\n" +
                "/help - Показать справку по командам\n" +
                "/addtask - Добавить новую задачу\n" +
                "/viewtasks - Показать список задач\n" +
                "/completedtasks - Показать завершенные задачи\n" +
                "/deletetask <ID> - Удалить задачу по ID\n" +
                "/completetask <ID> - Отметить задачу как завершенную по ID";
        sendMessage(chatId, helpMessage);
    }

    private void sendReplyKeyboard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите команду:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/viewtasks"));
        row1.add(new KeyboardButton("/addtask"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/completedtasks"));
        row2.add(new KeyboardButton("/help"));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDynamicKeyboard(long chatId, String messageText, List<String> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (String buttonLabel : buttons) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(buttonLabel));
            keyboardRows.add(row);
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void confirmDeleteTask(long chatId, int taskId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы уверены, что хотите удалить задачу с ID " + taskId + "?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("confirm_delete:" + taskId);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("cancel_delete");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yesButton);
        row.add(noButton);
        rows.add(row);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
            for (String task : tasks) {
                String[] parts = task.split(": ");
                int taskId = Integer.parseInt(parts[0]);
                String taskText = parts[1];

                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(taskText);

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                InlineKeyboardButton completeButton = new InlineKeyboardButton();
                completeButton.setText("Завершить");
                completeButton.setCallbackData("complete_task:" + taskId);

                InlineKeyboardButton deleteButton = new InlineKeyboardButton();
                deleteButton.setText("Удалить");
                deleteButton.setCallbackData("delete_task:" + taskId);

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(completeButton);
                row.add(deleteButton);
                rows.add(row);

                markup.setKeyboard(rows);
                message.setReplyMarkup(markup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendCompletedTasksList(long chatId) {
        DatabaseManager dbManager = new DatabaseManager();
        List<String> completedTasks = dbManager.getCompletedTasks(chatId);

        if (completedTasks.isEmpty()) {
            sendMessage(chatId, "У вас нет завершенных задач.");
        } else {
            StringBuilder response = new StringBuilder("Ваши завершенные задачи:\n");
            for (String task : completedTasks) {
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

    private void editMessageText(long chatId, int messageId, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}