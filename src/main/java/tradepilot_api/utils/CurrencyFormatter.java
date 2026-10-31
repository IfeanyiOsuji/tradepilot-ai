package tradepilot_api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyFormatter {

    private CurrencyFormatter() {
        // Utility class; prevent instantiation.
    }

    public static String format(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "N/A";
        }

        BigDecimal roundedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        if (currencyCode == null || currencyCode.isBlank()) {
            return roundedAmount.toPlainString();
        }

        Currency currency = Currency.getInstance(
                currencyCode.trim().toUpperCase(Locale.ROOT)
        );

        NumberFormat formatter;

        if ("NGN".equals(currency.getCurrencyCode())) {
            formatter = NumberFormat.getNumberInstance(Locale.US);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            return "₦" + formatter.format(roundedAmount);
        }

        formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setCurrency(currency);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        return formatter.format(roundedAmount);
    }
}