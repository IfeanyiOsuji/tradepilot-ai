package tradepilot_api.utils;



import java.time.DayOfWeek;
import java.time.LocalDate;
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

    public static DateRange resolveDateRange(String question) {

        LocalDate today = LocalDate.now();

        if (question.contains("last week")) {
            throw new IllegalArgumentException(
                    "Last-week queries are not supported yet. "
                            + "Try asking about this week or all-time totals."
            );
        }

        if (question.contains("this week")
                || question.contains("this week's")
                || question.contains("weekly")) {

            LocalDate monday = today.with(DayOfWeek.MONDAY);

            return new DateRange(
                    monday,
                    today,
                    "from " + monday + " to " + today
            );
        }

        if (question.contains("this month")
                || question.contains("this month's")
                || question.contains("monthly")) {

            LocalDate firstDay = today.withDayOfMonth(1);

            return new DateRange(
                    firstDay,
                    today,
                    "from " + firstDay + " to " + today
            );
        }

        if (question.contains("today")
                || question.contains("today's")) {

            return new DateRange(
                    today,
                    today,
                    "for " + today
            );
        }

        return new DateRange(
                null,
                null,
                "across all recorded dates"
        );
    }

    public static DateRange resolveTrendDateRange(String question) {

        LocalDate today = LocalDate.now();

        if (question.contains("last week")) {
            LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
            LocalDate lastMonday = thisMonday.minusWeeks(1);
            LocalDate lastSunday = thisMonday.minusDays(1);

            return new DateRange(
                    lastMonday,
                    lastSunday,
                    "from " + lastMonday + " to " + lastSunday
            );
        }

        if (question.contains("this week")
                || question.contains("this week's")
                || question.contains("weekly")) {

            LocalDate monday = today.with(DayOfWeek.MONDAY);

            return new DateRange(
                    monday,
                    today,
                    "from " + monday + " to " + today
            );
        }

        if (question.contains("this month")
                || question.contains("this month's")
                || question.contains("monthly")) {

            LocalDate firstDay = today.withDayOfMonth(1);

            return new DateRange(
                    firstDay,
                    today,
                    "from " + firstDay + " to " + today
            );
        }

        if (question.contains("yesterday")) {
            LocalDate yesterday = today.minusDays(1);

            return new DateRange(
                    yesterday,
                    yesterday,
                    "for " + yesterday
            );
        }

        if (question.contains("today")) {
            return new DateRange(
                    today,
                    today,
                    "for " + today
            );
        }

        LocalDate startDate = today.minusDays(6);

        return new DateRange(
                startDate,
                today,
                "from " + startDate + " to " + today
        );
    }

    public record DateRange(
            LocalDate startDate,
            LocalDate endDate,
            String description) {
    }
}