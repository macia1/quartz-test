package quartz;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.List;
import java.util.Set;

/**
 * @Author 朝花夕誓
 * @Date 2021/6/4 17:06
 * @Version 1.0
 * @Description quartz 定时器框架测试
 */
@Log4j2
@DisallowConcurrentExecution
public class Demo implements Job {

    /**
     * 任务调度器
     */
    private Scheduler scheduler;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        // 获取job传输过来的数据
        System.out.println("job translate data is -> " + jobExecutionContext.getJobDetail().getJobDataMap().getString("demo"));
        System.out.println("定时正在运行 : " + System.currentTimeMillis());
        // 获取 trigger
        Trigger trigger = jobExecutionContext.getTrigger();
        System.out.println("trigger description is -> " + trigger.getDescription());

        CrawlerJobMain instance = CrawlerJobMain.getInstance();

        this.scheduler = instance.getScheduler();

        jobGroupDeal(jobExecutionContext);

    }

    /**
     * 任务组处理
     * @param jobExecutionContext
     * @throws SchedulerException
     */
    public void jobGroupDeal(JobExecutionContext jobExecutionContext) throws SchedulerException {
        // 定时trigger组
        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        for (String triggerGroupName : triggerGroupNames) {
            GroupMatcher<TriggerKey> jobKeyGroupMatcher = GroupMatcher.triggerGroupEquals(triggerGroupName);
            Set<TriggerKey> jobKeys = scheduler.getTriggerKeys(jobKeyGroupMatcher);
            for (TriggerKey jobKey : jobKeys) {
                String name = jobKey.getName();
                System.out.println("run time name is -> " + name);

                // 依据trigger name移除想要移除的的任务
                if ("unuse".equals(name)){
                    // 移除trigger任务
                    scheduler.unscheduleJob(jobKey);
                    // 删除任务
//                    scheduler.deleteJob()
                }

                // 依据trigger name补充想要添加的任务
                if ("trigger crawler 1".equals(name)){
                    Trigger build = TriggerBuilder.newTrigger().withDescription("补充缺少的任务")
                            .withIdentity("唯一id", "对应的trigger组")
                            .withSchedule(
                              CronScheduleBuilder.cronSchedule("*/3 * * * * ?")
                            )
                            .build();
                    JobDetail jobDetail2 = JobBuilder.newJob(CrawlerJob.class).build();
                    scheduler.scheduleJob(jobDetail2, build);
                }
            }
        }
    }

    /**
     * 定时推送任务
     */
    public static class CrawlerJob implements Job{

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("CrawlerJob is running!");
        }
    }


}