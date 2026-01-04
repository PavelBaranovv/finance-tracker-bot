package financetracker.service.statistic;

import financetracker.constant.Message;

public enum StatisticPeriod {

    DAY("STAT_DAY", Message.periodDayHeader),
    DAYS_7("STAT_7D", Message.period7DaysHeader),
    MONTH("STAT_MONTH", Message.periodMonthHeader),
    YEAR("STAT_YEAR", Message.periodYearHeader);

    private final String callback;
    private final String header;

    StatisticPeriod(String callback, String header) {
        this.callback = callback;
        this.header = header;
    }

    public String callback() {
        return callback;
    }

    public String header() {
        return header;
    }

    public static StatisticPeriod fromCallback(String callback) {
        for (StatisticPeriod period : values()) {
            if (period.callback.equals(callback)) {
                return period;
            }
        }
        return null;
    }
}


