import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {
    private static final String BOT_USERNAME = "BinaryToolCrackingHash_bot";
    private static final String BOT_TOKEN = "7794808374:AAHThdCTXmtlGREBggWFeC_gL2EYsKmPsHY";

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
            String chatId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            if (messageText.equalsIgnoreCase("/start")) {
                sendResponse(chatId, "❗\uFE0FMade with @VanoStrey❗\uFE0F\nThis is a prototype program, so the variety of symbols in combinations is limited. But the algorithm allows you to use any characters in combinations.");
                sendResponse(chatId, "👋 Hi! Send me the hash.\nAnd I'll crack it right away.\uD83D\uDE08\uD83D\uDE08\uD83D\uDE08.\nSupported algorithms:\n\nSHA256\n\nThe dictionary contains natural numbers from 1 to 5.000.000.000");

            } else {
                long startTime = System.currentTimeMillis();
                String result = null;
                try {
                    result = Main.ThreeByteCrackSHA256(messageText);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                long endTime = System.currentTimeMillis();

                sendResponse(chatId, result);
                sendResponse(chatId, "⏳  " + (endTime - startTime) + " ms");
            }
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
