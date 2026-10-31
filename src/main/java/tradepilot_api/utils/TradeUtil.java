package tradepilot_api.utils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeUtil {
    public static final Set<String> SUPPORTED_CURRENCIES =
            Set.of("NGN", "USD", "GBP", "EUR", "CAD", "AUD", "GHS", "KES", "ZAR");


    public static int extractInactivityDays(String question) {
        Pattern pattern = Pattern.compile(
                "(-?\\d+)\\s*(day|days|week|weeks|month|months)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(question);

        if (!matcher.find()) {
            return 30;
        }

        int value;

        try {
            value = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return 30;
        }

        if (value <= 0) {
            return 30;
        }

        String unit = matcher.group(2).toLowerCase(Locale.ROOT);

        long days = switch (unit) {
            case "week", "weeks" -> value * 7L;
            case "month", "months" -> value * 30L;
            default -> value;
        };

        if (days > 365) {
            return 30;
        }

        return (int) days;
    }
}