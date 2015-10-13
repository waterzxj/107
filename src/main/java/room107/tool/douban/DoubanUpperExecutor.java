package room107.tool.douban;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import room107.Constants;
import room107.datamodel.DoubanPoster;
import room107.util.EmailUtils;
import room107.util.JsonUtils;
import room107.util.StringUtils;

import com.google.gson.reflect.TypeToken;

/**
 * @author WangXiao
 */
@RequiredArgsConstructor
public class DoubanUpperExecutor implements Runnable {

    private volatile List<DoubanPoster> doubanPosters = new ArrayList<DoubanPoster>();

    @NonNull
    private boolean isLocal;

    /**
     * 1 min
     */
    private static final int UPDATE_TASK_INTERVAL = 60000;

    /**
     * <url, url>, just as a set.
     */
    private Map<String, Object> invalidUrls = new ConcurrentHashMap<String, Object>();

    @Override
    public void run() {
        /*
         * update doubanPosters from web site
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String url = getTaskUrl();
                    try {
                        String task = Jsoup.connect(url).timeout(10000)
                                .execute().body();
                        List<DoubanPoster> posters = JsonUtils.fromJson(
                                task, new TypeToken<List<DoubanPoster>>() {
                                }.getType());
                        if (CollectionUtils.isEmpty(posters)) {
                            System.err.println("WARN: empty poster");
                        }
                        System.out.println("Invalid URLs: " + invalidUrls
                                + ", size=" + invalidUrls.size());
                        doubanPosters = posters;
                        invalidUrls.clear();
                        Thread.sleep(UPDATE_TASK_INTERVAL);
                    } catch (Exception e) {
                        System.err.println("Update task url failed: taskUrl="
                                + url);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        /*
         * schedule up
         */
        new Url1Scheduler().start();
        new Url2Scheduler().start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getTaskUrl() {
        return (isLocal ? "http://localhost:8080" : "http://107room.com")
                + Constants.POSTER_PATH;
    }

    @AllArgsConstructor
    @ToString
    private abstract class PosterScheduler extends Thread {

        /**
         * in minute
         */
        private int morningInterval, dayInterval, nightInterval;

        /**
         * @return in ms
         */
        private int getInterval() {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int result;
            if (hour >= 2 && hour <= 7) {
                // 30-60 min
                result = RandomUtils.nextInt(30) + morningInterval;
            } else if (hour > 7 && hour <= 19) {
                // 5-10 min
                result = RandomUtils.nextInt(5) + dayInterval;
            } else {
                // 3-5 min
                result = RandomUtils.nextInt(2) + nightInterval;
            }
            // add random second
            return (result * 60 + RandomUtils.nextInt(10)) * 1000;
        }

        @Override
        public final void run() {
            while (true) {
                if (CollectionUtils.isEmpty(doubanPosters)) {
                    Thread.yield();
                    continue;
                }
                /*
                 * handle
                 */
                for (DoubanPoster poster : doubanPosters) {
                    System.out.println(getClass().getSimpleName()
                            + " handle poster: " + poster.getName());
                    try {
                        DoubanUpper upper = new DoubanUpper(poster);
                        List<String> urls = getUrls(poster);
                        for (String url : urls) {
                            if (invalidUrls.containsKey(url)) {
                                continue;
                            }
                            try {
                                upper.up(url);
                            } catch (HttpStatusException e) {
                                if (e.getStatusCode() == 404) {
                                    invalidUrls.put(url, getClass()
                                            .getSimpleName());
                                    EmailUtils.sendAdminMail(
                                            "Douban Upper",
                                            "Page not found: " + url
                                                    + ", name="
                                                    + poster.getName());
                                    System.err.println("Remove invalid URL: "
                                            + url);
                                } else {
                                    throw e;
                                }
                            } catch (Exception e) {
                                System.err.println("Up failed: url=" + url
                                        + ", name=" + poster.getName());
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(getInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * @return URLs to up
         */
        protected abstract List<String> getUrls(DoubanPoster poster);

        protected List<String> getUrls(String urlString) {
            if (StringUtils.isEmpty(urlString)) {
                return Collections.emptyList();
            }
            String[] urls = StringUtils.split(urlString, ", ");
            if (ArrayUtils.isEmpty(urls)) {
                return Collections.emptyList();
            }
            return Arrays.asList(urls);
        }

    }

    private class Url1Scheduler extends PosterScheduler {

        public Url1Scheduler() {
            super(30, 5, 3);
        }

        @Override
        protected List<String> getUrls(DoubanPoster poster) {
            return getUrls(poster.getUrl1());
        }

    }

    private class Url2Scheduler extends PosterScheduler {

        public Url2Scheduler() {
            super(30, 35, 30);
        }

        @Override
        protected List<String> getUrls(DoubanPoster poster) {
            return getUrls(poster.getUrl2());
        }

    }

    public static void main(String[] args) {
        new DoubanUpperExecutor(false).run();
    }

}
