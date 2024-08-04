package stock.overseas.calculator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

public class USStockMarketHolidayCalculator {

    public String checkWeekendOrHoliday() {

        ZoneId americaZoneId = ZoneId.of("America/New_York");
        LocalDate now = LocalDate.now(americaZoneId);
        int year = now.getYear();

        //주말
        if(now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "오늘은 주말이어서 미국 주식 시장이 휴장합니다.";
        }

        Map<LocalDate, String> holidayMap = new HashMap<>();
        holidayMap.put(getNewYearDay(year), "새해 첫날 (New Year's Day)");
        holidayMap.put(getMartinLutherKingJrDay(year), "마틴 루터 킹 주니어 데이 (Martin Luther King, Jr. Day)");
        holidayMap.put(getPresidentsDay(year), "미국 대통령의 날 (Washington's Birthday)");
        holidayMap.put(getGoodFriday(year), "성 금요일 (Good Friday)");
        holidayMap.put(getMemorialDay(year), "메모리얼 데이 (Memorial Day)");
        holidayMap.put(getJunteenthNationalIndependenceDay(year), "준틴스 데이 (Juneteenth National Independence Day)");
        holidayMap.put(getIndependenceDay(year), "독립 기념일 (Independence Day)");
        holidayMap.put(getLaborDay(year), "노동절 (Labor Day)");
        holidayMap.put(getThanksgivingDate(year), "추수감사절 (Thanksgiving Day)");
        holidayMap.put(getChristmasDay(year), "크리스마스 (Christmas Day)");

        //공휴일
        for (LocalDate holiday : holidayMap.keySet()) {
            if(holiday.equals(now)) {
                return "오늘은 " + holidayMap.get(holiday) + "로 미국 주식 시장이 휴장합니다.";
            }
        }

        return null;
    }

    /**
     * 새해 첫날(New Year's Day)
     * 매년 1월 1일
     */
    private LocalDate getNewYearDay(int year) {
        LocalDate newYearDay = LocalDate.of(year, 1, 1);
        if(newYearDay.getDayOfWeek() == DayOfWeek.SUNDAY)  {
            return newYearDay.plusDays(1);
        }

        return newYearDay;
    }

    /**
     * 마틴 루터 킹 주니어 데이(Martin Luther King Jr. Day)
     * 매년 1월의 세 번째 월요일
     */
    public static LocalDate getMartinLutherKingJrDay(int year) {
        LocalDate januaryFirst = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate firstMonday = januaryFirst.with(DayOfWeek.MONDAY);
        if (firstMonday.getDayOfMonth() > 7) {
            firstMonday = firstMonday.plusWeeks(1);
        }

        return firstMonday.plusWeeks(2);
    }

    /**
     * 미국 대통령의 날(President's Day) 또는 조지 워싱턴 생일(George Washington's Birthday)
     * 매년 2월의 세 번째 월요일
     */
    private LocalDate getPresidentsDay(int year) {
        LocalDate februaryFirst = LocalDate.of(year, Month.FEBRUARY, 1);
        LocalDate firstMonday = februaryFirst.with(DayOfWeek.MONDAY);
        if (firstMonday.getDayOfMonth() > 7) {
            firstMonday = firstMonday.plusWeeks(1);
        }

        return firstMonday.plusWeeks(2);
    }

    /**
     * 성 금요일(Good Friday)
     * 부활절(성주일) 전의 금요일
     */
    private LocalDate getGoodFriday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        LocalDate easterSunday = LocalDate.of(year, month, day);
        return easterSunday.minusDays(2);
    }

    /**
     * 메모리얼 데이(Memorial Day)
     * 매년 5월의 마지막 월요일
     */
    private LocalDate getMemorialDay(int year) {
        LocalDate mayFirst = LocalDate.of(year, Month.MAY, 1);
        LocalDate firstMonday = mayFirst.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        LocalDate lastMonday = firstMonday.plusWeeks(4);
        if (lastMonday.getMonth() != Month.MAY) {
            lastMonday = lastMonday.minusWeeks(1);
        }

        return lastMonday;
    }

    /**
     * 준틴스 데이(Juneteenth National Independence Day)
     * 매년 6월 19일
     */
    private LocalDate getJunteenthNationalIndependenceDay(int year) {
        LocalDate junteenthDay = LocalDate.of(year, 6, 19);
        if(junteenthDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return junteenthDay.minusDays(1);
        } else if(junteenthDay.getDayOfWeek() == DayOfWeek.SUNDAY)  {
            return junteenthDay.plusDays(1);
        }

        return junteenthDay;
    }

    /**
     * 독립 기념일(Independence Day)
     * 매년 7월 4일
     */
    private LocalDate getIndependenceDay(int year) {
        LocalDate independenceDay = LocalDate.of(year, 7, 4);
        if(independenceDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return independenceDay.minusDays(1);
        } else if(independenceDay.getDayOfWeek() == DayOfWeek.SUNDAY)  {
            return independenceDay.plusDays(1);
        }

        return independenceDay;
    }

    /**
     * 노동절(Labor Day)
     * 매년 9월의 첫 번째 월요일
     */
    private LocalDate getLaborDay(int year) {
        LocalDate septemberFirst = LocalDate.of(year, Month.SEPTEMBER, 1);
        LocalDate laborDay = septemberFirst;
        while (laborDay.getDayOfWeek() != DayOfWeek.MONDAY) {
            laborDay = laborDay.plusDays(1);
        }

        return laborDay;
    }

    /**
     * 추수감사절(Thanksgiving Day)
     * 매년 11월의 네 번째 목요일
     */
    private LocalDate getThanksgivingDate(int year) {
        LocalDate novemberFirst = LocalDate.of(year, Month.NOVEMBER, 1);
        LocalDate firstThursday = novemberFirst.with(DayOfWeek.THURSDAY);
        if (firstThursday.getDayOfMonth() > 7) {
            firstThursday = firstThursday.plusWeeks(1);
        }

        return firstThursday.plusWeeks(3);
    }

    /**
     * 크리스마스 (Christmas Day)
     * 매년 11월의 네 번째 목요일
     */
    private LocalDate getChristmasDay(int year) {
        LocalDate christmasDay = LocalDate.of(year, 12, 25);
        if(christmasDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return christmasDay.minusDays(1);
        } else if(christmasDay.getDayOfWeek() == DayOfWeek.SUNDAY)  {
            return christmasDay.plusDays(1);
        }

        return christmasDay;
    }
}
