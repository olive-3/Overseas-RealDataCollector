package stock.overseas.schedule;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class ShutdownService {

    private static final long SHUTDOWN_CHECK_INTERVAL_MS = 60000;   //60초

    /**
     * 미국시간 오후 6시 정각에 프로그램 종료
     */
    public void closeProgram() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                TimeZone estTimeZone = TimeZone.getTimeZone("America/New_York");
                Calendar now = Calendar.getInstance(estTimeZone);
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);

                // 현재 시간이 10시 0분이면 프로그램을 종료합니다.
                if (hour == 10 && minute == 0) {
                    log.info("[{}] {}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()), "미국 증시가 종료되는 시간입니다. 프로그램을 종료합니다.");
                    System.exit(0);
                }
            }
        };

        timer.schedule(task, 0, SHUTDOWN_CHECK_INTERVAL_MS);
    }
}
