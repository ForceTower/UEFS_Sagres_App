package com.forcetower.uefs.sagres_sdk.parsers;

import com.forcetower.uefs.sagres_sdk.domain.SagresMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João Paulo on 18/11/2017.
 */

public class SagresMessagesParser {
    private static final String MESSAGE_CLASS_RECEIVED = "class=\"recado-escopo\">";
    private static final String MESSAGE_DATE_RECEIVED = "class=\"recado-data\">";
    private static final String MESSAGE_MESSAGE_RECEIVED = "class=\"recado-texto\">";
    private static final String MESSAGE_FROM_RECEIVED = "class=\"recado-remetente\">";

    public static List<SagresMessage> getStartPageMessages(String html) {
        List<SagresMessage> messages = new ArrayList<>();

        int position = 0;
        boolean found = html.indexOf("<article id", position) != -1;

        while (found) {
            int start = html.indexOf("<article id", position);
            int end = html.indexOf("</article>", start);

            if (start == -1)
                return messages;

            String article = html.substring(start, end);

            SagresMessage message = extractInfoArticle(article);
            messages.add(message);
            position = end;
        }

        return messages;
    }

    private static SagresMessage extractInfoArticle(String article) {
        String clazz = extractArticleForm1(MESSAGE_CLASS_RECEIVED, article);
        String date = extractArticleForm1(MESSAGE_DATE_RECEIVED, article);
        String message = extractArticleForm2(MESSAGE_MESSAGE_RECEIVED, article);
        String from = extractArticleForm2(MESSAGE_FROM_RECEIVED, article);
        return new SagresMessage(from, message, date, clazz);
    }

    private static String extractArticleForm2(String regex, String article) {
        int startRRE = article.indexOf(regex);
        if (startRRE != -1) {
            int endRRE = article.indexOf("</span>", startRRE);
            String message = article.substring(startRRE, endRRE).trim();
            message = message.substring(regex.length() + 1);

            message = message.substring(message.indexOf(">") + 1).trim();
            return message;
        }

        return null;
    }

    private static String extractArticleForm1(String regex, String article) {
        int startCRE = article.indexOf(regex);
        if (startCRE != -1) {
            int endCRE = article.indexOf("</span>", startCRE);
            String extracted = article.substring(startCRE, endCRE);

            //CLASS NAME
            extracted = extracted.substring(extracted.indexOf(">") + 1).trim();
            return extracted;
        }

        return null;
    }
}
