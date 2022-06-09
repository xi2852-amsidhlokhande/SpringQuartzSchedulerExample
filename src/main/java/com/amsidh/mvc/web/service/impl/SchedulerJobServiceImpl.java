package com.amsidh.mvc.web.service.impl;

import com.amsidh.mvc.scheduler.component.JobScheduleCreator;
import com.amsidh.mvc.web.exception.JobNoFoundException;
import com.amsidh.mvc.web.model.JobScheduleRequest;
import com.amsidh.mvc.web.model.JobScheduleResponse;
import com.amsidh.mvc.web.model.JobUpdateRequest;
import com.amsidh.mvc.web.service.SchedulerJobService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Transactional
@Service
public class SchedulerJobServiceImpl implements SchedulerJobService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobScheduleCreator scheduleCreator;
    @Override
    public SchedulerMetaData getMetaData() throws SchedulerException {
        return scheduler.getMetaData();
    }

    @Override
    public List<JobScheduleResponse> getAllJobList() throws SchedulerException {
        log.info("Getting all jobs from quartz scheduler");
        List<JobScheduleResponse> jobScheduleResponses = scheduler.getJobGroupNames().parallelStream().flatMap(groupName -> {
            try {
                Stream<JobScheduleResponse> jobResponseStream = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).parallelStream().map(jobKey -> {
                    return JobScheduleResponse.builder().jobName(jobKey.getName()).jobGroup(jobKey.getGroup()).build();
                });
                return jobResponseStream;
            } catch (SchedulerException e) {
                log.error("Error while getting jobs from quartz scheduler", e);
            }
            return Stream.empty();
        }).collect(Collectors.toList());
        return jobScheduleResponses;
    }

    @Override
    public JobScheduleResponse deleteJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobUpdateRequest.getJobName(), jobUpdateRequest.getJobGroup());
        JobDetail jobDetail = Optional.ofNullable(scheduler.getJobDetail(jobKey)).orElseThrow(() -> JobNoFoundException.builder().jobName(jobKey.getName()).groupName(jobKey.getGroup()).errorCode("E001").errorMessage("Job not found").build());
        if (jobDetail != null && scheduler.deleteJob(jobKey)) {
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job deleted successfully").build();
        } else {
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job not deleted").build();
        }
    }

    @Override
    public JobScheduleResponse pauseJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobUpdateRequest.getJobName(), jobUpdateRequest.getJobGroup());
        JobDetail jobDetail = Optional.ofNullable(scheduler.getJobDetail(jobKey)).orElseThrow(() -> JobNoFoundException.builder().jobName(jobKey.getName()).groupName(jobKey.getGroup()).errorCode("E001").errorMessage("Job not found").build());
        if (jobDetail != null) {
            scheduler.pauseJob(jobKey);
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job paused successfully").build();
        } else {
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job not pause deleted").build();
        }
    }

    @Override
    public JobScheduleResponse resumeJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobUpdateRequest.getJobName(), jobUpdateRequest.getJobGroup());
        JobDetail jobDetail = Optional.ofNullable(scheduler.getJobDetail(jobKey)).orElseThrow(() -> JobNoFoundException.builder().jobName(jobKey.getName()).groupName(jobKey.getGroup()).errorCode("E001").errorMessage("Job not found").build());
        if (jobDetail != null) {
            scheduler.resumeJob(jobKey);
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job resumed successfully").build();
        } else {
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job not resumed").build();
        }
    }

    @Override
    public JobScheduleResponse startJobNow(JobUpdateRequest jobUpdateRequest) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobUpdateRequest.getJobName(), jobUpdateRequest.getJobGroup());
        JobDetail jobDetail = Optional.ofNullable(scheduler.getJobDetail(jobKey)).orElseThrow(() -> JobNoFoundException.builder().jobName(jobKey.getName()).groupName(jobKey.getGroup()).errorCode("E001").errorMessage("Job not found").build());
        if (jobDetail != null) {
            scheduler.triggerJob(jobKey);
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job started successfully").build();
        } else {
            return JobScheduleResponse.builder().jobName(jobUpdateRequest.getJobName()).jobGroup(jobUpdateRequest.getJobGroup()).desc("Job not started").build();
        }
    }

    @Override
    public JobScheduleResponse saveOrUpdate(JobScheduleRequest jobScheduleRequest) throws SchedulerException {
        JobScheduleResponse.JobScheduleResponseBuilder jobResponseBuilder = JobScheduleResponse.builder();
        jobResponseBuilder.jobName(jobScheduleRequest.getJobName());
        jobResponseBuilder.jobGroup(jobScheduleRequest.getJobGroup());
        if (!isJobExist(jobScheduleRequest.getJobName(), jobScheduleRequest.getJobGroup())) {
            scheduleNewJob(jobScheduleRequest, jobResponseBuilder);
        } else {
            updateScheduleJob(jobScheduleRequest, jobResponseBuilder);
        }
        return jobResponseBuilder.build();
    }

    private void scheduleNewJob(JobScheduleRequest jobScheduleRequest, JobScheduleResponse.JobScheduleResponseBuilder jobResponseBuilder) {
        try {
            JobKey jobKey = new JobKey(jobScheduleRequest.getJobName(), jobScheduleRequest.getJobGroup());
            if (!scheduler.checkExists(jobKey)) {
                JobDetail jobDetail = this.scheduleCreator.createJobDetail(context, jobScheduleRequest);
                Trigger trigger = StringUtils.isNotBlank(jobScheduleRequest.getCronExpression()) ? this.scheduleCreator.createCronTrigger(jobDetail, jobScheduleRequest.getCronExpression()) : this.scheduleCreator.createSimpleTrigger(jobDetail, jobScheduleRequest.getRepeatIntervalTime());
                scheduler.scheduleJob(jobDetail, trigger);
                jobResponseBuilder.jobStatus("SCHEDULED");
                jobResponseBuilder.desc("Job scheduled successfully");
            } else {
                log.error("Job already exist");
                jobResponseBuilder.jobStatus("NOT SCHEDULED");
                jobResponseBuilder.desc("Job already exist");
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            jobResponseBuilder.desc(e.getMessage());
        }
    }

    private void updateScheduleJob(JobScheduleRequest jobScheduleRequest, JobScheduleResponse.JobScheduleResponseBuilder jobResponseBuilder) {
        JobDetail jobDetail = this.scheduleCreator.createJobDetail(context, jobScheduleRequest);
        Trigger newTrigger;
        if (StringUtils.isNotBlank(jobScheduleRequest.getCronExpression())) {
            newTrigger = this.scheduleCreator.createCronTrigger(jobDetail, jobScheduleRequest.getCronExpression());
        } else {
            newTrigger = this.scheduleCreator.createSimpleTrigger(jobDetail, jobScheduleRequest.getRepeatIntervalTime());
        }
        try {
            this.scheduler.rescheduleJob(TriggerKey.triggerKey(jobScheduleRequest.getJobName()), newTrigger);
            jobResponseBuilder.jobStatus("EDITED & SCHEDULED");
            jobResponseBuilder.desc("Job updated successfully");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            jobResponseBuilder.jobStatus(e.getMessage());
            jobResponseBuilder.desc("Job not updated");
        }
    }

    private boolean isJobExist(String jobName, String groupName) throws SchedulerException {
        return this.scheduler.checkExists(JobKey.jobKey(jobName, groupName));
    }
}
