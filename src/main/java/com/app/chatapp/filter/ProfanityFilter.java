package com.app.chatapp.filter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProfanityFilter {
    private static ProfanityFilter filter = new ProfanityFilter();
    private static Set<String> forbiddenWords;
    private ProfanityFilter(){

        forbiddenWords = new HashSet<>();
        loadForbiddenWords();
    }

    public static ProfanityFilter getInstance(){
        return filter;
    }

    private void loadForbiddenWords() {
        // Przykładowe wulgaryzmy - można dodać więcej
        forbiddenWords.add("chuj");
        forbiddenWords.add("kurwa");
        forbiddenWords.add("pierdol");
        forbiddenWords.add("UwU");
    }

    // Funkcja do maskowania wulgaryzmu
    private static String maskWord(String word) {
        return word.charAt(0) + "*".repeat(word.length() - 2) + word.charAt(word.length() - 1);
    }

    private static int levenshteinDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) {
            for (int j = 0; j <= word2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[word1.length()][word2.length()];
    }

    public static String filterMessage(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder filteredText = new StringBuilder();

        for (String word : words) {
            String cleanedWord = word.replaceAll("[^a-zA-Z0-9]", ""); // Usuwanie znaków specjalnych
            boolean isSimilar = forbiddenWords.stream()
                    .anyMatch(forbidden -> levenshteinDistance(cleanedWord.toLowerCase(), forbidden) <= 2);

            if (isSimilar) {
                filteredText.append(maskWord(word));
            } else {
                filteredText.append(word);
            }
            filteredText.append(" ");
        }

        return filteredText.toString().trim();
    }


}
