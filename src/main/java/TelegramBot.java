import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {
    private static final String BOT_USERNAME = "";
    private static final String BOT_TOKEN = "";

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            String response;
            if (messageText.equalsIgnoreCase("/start")) {
                response = "👋 Привет! Отправь мне хэш, и я моментально его взломаю\uD83D\uDE08\uD83D\uDE08\uD83D\uDE08.\nПоддерживаемые алгоритмы:\nMD5\nSHA1\nSHA256\n\nЗашифрованные положительные числа от 1 до 2^26";
            } else {
                response = Main.universalCrackHash(messageText); // Вызываем твою функцию
            }

            sendResponse(chatId, response);
        }
    }

    private void sendResponse(String chatId, String text) {
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
