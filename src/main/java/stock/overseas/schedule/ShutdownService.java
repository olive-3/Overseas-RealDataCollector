package stock.overseas.schedule;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class ShutdownService {

    private static final long SHUTDOWN_CHECK_INTERVAL_MS = 60000;   //60초

    /**
     * 프로그램 종료
     */
    public void closeProgram(LocalTime closingTime) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                TimeZone estTimeZone = TimeZone.getTimeZone("America/New_York");
                Calendar now = Calendar.getInstance(estTimeZone);
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);

                if (currentHour == closingTime.getHour() && currentMinute == closingTime.getMinute()) {
                    log.info("미국 증시가 종료되는 시간입니다. 프로그램을 종료합니다.");
                    System.exit(0);
                }
            }
        };

        timer.schedule(task, 0, SHUTDOWN_CHECK_INTERVAL_MS);
    }
}
