package room107.service.job;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author WangXiao
 */
@CommonsLog
@Component
public class JobScheduler {

    private Scheduler scheduler;

    @Autowired
    @Qualifier(value = "dailyAdminJob")
    private DailyAdminJob dailyAdminJob;

    @Autowired
    @Qualifier(value = "weiXinResubscribeJob")
    private WeiXinResubscribeJob weiXinResubscribeJob;
    
    @Autowired
    private FullGcJob fullGcJob;

    public JobScheduler() throws Exception {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    @PostConstruct
    public void init() throws Exception {
        schedule("0 0 3 * * ?", dailyAdminJob);
        schedule("0 30 * * * ?", weiXinResubscribeJob);
        schedule("0 45 * * * ?", fullGcJob);
    }

    private void schedule(String cron, SimpleJob job) throws Exception {
        Validate.notNull(cron);
        Validate.notNull(job);
        log.info("Schedule job: cron=" + cron + ", job=" + job.getName());
        JobDetail jobDetail = newJob(JobAdapter.class).build();
        jobDetail.getJobDataMap().put(SimpleJob.class.getSimpleName(), job);
        scheduler.scheduleJob(jobDetail,
                newTrigger().withSchedule(cronSchedule(cron)).build());
    }

    /**
     * Adapt {@link SimpleJob} to {@link Job}.
     * 
     * @author WangXiao
     */
    public static class JobAdapter implements Job {

        @Override
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            JobDataMap map = context.getMergedJobDataMap();
            SimpleJob job = (SimpleJob) map
                    .get(SimpleJob.class.getSimpleName());
            log.info("Run job: " + job.getClass().getSimpleName());
            try {
                job.run();
            } catch (Exception e) {
                log.error("Run job failed: " + job.getName());
            }
        }

    }

}
