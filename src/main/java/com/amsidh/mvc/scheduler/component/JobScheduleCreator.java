package com.amsidh.mvc.scheduler.component;

import com.amsidh.mvc.scheduler.job.RemoteServiceCallJob;
import com.amsidh.mvc.web.model.JobScheduleRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import static java.util.Optional.ofNullable;
import static org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;

@Component
@Slf4j
public class JobScheduleCreator {

    public static final String LOCAL_ZONE = "Asia/Kolkata";

    public JobDetail createJobDetail(ApplicationContext context, JobScheduleRequest jobScheduleRequest) {

        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(RemoteServiceCallJob.class);
        factoryBean.setDurability(true);
        factoryBean.setApplicationContext(context);
        ofNullable(jobScheduleRequest.getJobName()).ifPresent(factoryBean::setName);
        ofNullable(jobScheduleRequest.getJobGroup()).ifPresent(factoryBean::setGroup);
        ofNullable(jobScheduleRequest.getDesc()).ifPresent(factoryBean::setDescription);
        // set job data map
        JobDataMap jobDataMap = new JobDataMap();
        ofNullable(jobScheduleRequest.getRemoteMSUrl()).ifPresent(remoteMSUrl -> jobDataMap.put("remoteMSUrl", remoteMSUrl));
        if (!jobDataMap.isEmpty()) factoryBean.setJobDataMap(jobDataMap);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();

    }

    @SneakyThrows
    public CronTrigger createCronTrigger(JobDetail jobDetail, String cronExpression) {

        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(jobDetail.getKey().getName());
        factoryBean.setStartTime(new Date());
        factoryBean.setCronExpression(cronExpression);
        Optional.ofNullable(jobDetail.getJobDataMap()).ifPresent(factoryBean::setJobDataMap);
        Optional.ofNullable(jobDetail.getDescription()).ifPresent(factoryBean::setDescription);
        factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_FIRE_NOW);
        factoryBean.setTimeZone(TimeZone.getTimeZone(LOCAL_ZONE));
        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return factoryBean.getObject();
    }

    public SimpleTrigger createSimpleTrigger(JobDetail jobDetail, Long repeatIntervalTime) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(jobDetail.getKey().getName());
        factoryBean.setStartTime(new Date());
        factoryBean.setRepeatInterval(repeatIntervalTime);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        factoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_FIRE_NOW);
        Optional.ofNullable(jobDetail.getJobDataMap()).ifPresent(factoryBean::setJobDataMap);
        Optional.ofNullable(jobDetail.getDescription()).ifPresent(factoryBean::setDescription);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

}
