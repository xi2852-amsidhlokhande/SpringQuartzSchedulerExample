package com.amsidh.mvc.web.service;

import com.amsidh.mvc.web.model.JobScheduleRequest;
import com.amsidh.mvc.web.model.JobScheduleResponse;
import com.amsidh.mvc.web.model.JobUpdateRequest;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;

import java.util.List;

public interface SchedulerJobService {

    SchedulerMetaData getMetaData() throws SchedulerException;

    List<JobScheduleResponse> getAllJobList() throws SchedulerException;

    JobScheduleResponse deleteJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException;

    JobScheduleResponse pauseJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException;

    JobScheduleResponse resumeJob(JobUpdateRequest jobUpdateRequest) throws SchedulerException;

    JobScheduleResponse startJobNow(JobUpdateRequest jobUpdateRequest) throws SchedulerException;

    JobScheduleResponse saveOrUpdate(JobScheduleRequest scheduleJob) throws SchedulerException;
}
