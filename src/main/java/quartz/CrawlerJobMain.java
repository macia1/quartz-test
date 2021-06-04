package quartz;


import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author 朝花夕誓
 * @Date 2021/6/4 17:14
 * @Version 1.0
 * @Description
 */
public class CrawlerJobMain {

    private CrawlerJobMain(){};

    private static volatile CrawlerJobMain instance;

    public static CrawlerJobMain getInstance(){
        if (instance == null){
            synchronized (CrawlerJobMain.class){
                if (instance == null){
                    instance = new CrawlerJobMain();
                }
            }
        }
        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static void main(String[] args) throws Exception {
        CrawlerJobMain crawlerJobMain = new CrawlerJobMain();
        crawlerJobMain.run();
    }

    private static Scheduler scheduler = null;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static final void run() throws Exception{
        // 描述job实现类及其他相关的静态信息
        JobBuilder jobBuilder = JobBuilder.newJob(Demo.class);
        jobBuilder.usingJobData("demo", "demo");
        JobDetail job = jobBuilder.withIdentity("jobName", "jobGroup").withDescription("quartz 定时器").build();

        // 创建trigger对象
        CronTrigger trigger = TriggerBuilder.newTrigger()
                                            .withIdentity("trigger crawler 1", "trigger crawler group 1")
                                            .withDescription("爬虫定时任务")
                                            .withSchedule(
                                                CronScheduleBuilder.cronSchedule("*/10 * * * * ?")
                                            ).build();

        // 创建 scheduled 调度器
        Date date = scheduler.scheduleJob(job, trigger);

        // 启动
        scheduler.start();

        // 获取调度器中正在运行的任务组信息
        List<String> jobGroupNames = scheduler.getJobGroupNames();

        // 定时任务目标获取的信息
        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();

        for (String jobGroupName : jobGroupNames) {
            GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.jobGroupEquals(jobGroupName);
            Set<JobKey> jobKeys = scheduler.getJobKeys(jobKeyGroupMatcher);
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                System.out.println("发现任务组中的正在执行的任务 : " + jobDetail.getDescription());
//                scheduler.scheduleJob(jobDetail, trigger);
            }
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(job.getKey() + "已被安排执行：" + simpleDateFormat.format(date) +
                            ", 并且以如下重复规则重复执行：" + trigger.getCronExpression());
    }

}
