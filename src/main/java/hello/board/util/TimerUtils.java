package hello.board.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimerUtils {

    private static long totalTime = 0;
    private static long count = 0;

    public static void addTime(long time) {
        totalTime += time;
        count++;
    }

    public static long getTotalTime() {
        return totalTime;
    }

    public static double getAverageTime() {
        return (double) totalTime / count;
    }

    public static long getCount() {
        return count;
    }

    public static void clear() {
        totalTime = 0;
        count = 0;
    }
}
