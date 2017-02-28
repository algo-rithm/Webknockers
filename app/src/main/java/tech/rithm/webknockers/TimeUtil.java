package tech.rithm.webknockers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rithm on 2/26/2017.
 */

public class TimeUtil {
    private static final List<Long> spans = Arrays.asList(
        TimeUnit.DAYS.toMillis(365),
        TimeUnit.DAYS.toMillis(30),
        TimeUnit.DAYS.toMillis(7),
        TimeUnit.DAYS.toMillis(1),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.SECONDS.toMillis(1)
    );
    private static final List<String> words = Arrays.asList(
        "year",
        "month",
        "week",
        "day",
        "hour",
        "minute",
        "second"
    );

    public static String toDuration(long duration) {
        StringBuilder time = new StringBuilder();

        for (int i = 0; i < TimeUtil.spans.size(); i++){
            Long current = TimeUtil.spans.get(i);
            long temp = duration/current;
            if(temp>0) {
                time.append(temp).append(" ").append(TimeUtil.words.get(i)).append(temp != 1 ? "s" : "").append(" ago");
                break;
            }
        }

        if("".equals(time.toString())){
            return "0 seconds ago";
        } else {
            return time.toString();
        }
    }
}
