package com.amsidh.mvc.web.controller;

import com.amsidh.mvc.web.exception.ScheduleJobException;
import com.amsidh.mvc.web.model.JobScheduleRequest;
import com.amsidh.mvc.web.model.JobScheduleResponse;
import com.amsidh.mvc.web.model.JobUpdateRequest;
import com.amsidh.mvc.web.service.SchedulerJobService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/scheduler-service/job")
public class JobController {
    private final SchedulerJobService scheduleJobService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<JobScheduleResponse> getAllJobs() throws SchedulerException {
        log.info("Request received to get all jobs");
        return scheduleJobService.getAllJobList();
    }

    @GetMapping("/metaData")
    public SchedulerMetaData metaData() throws SchedulerException {
        log.info("Request received to get jobs metadata");
        return scheduleJobService.getMetaData();
    }

    @PostMapping(value = "/submit")
    public JobScheduleResponse saveOrUpdate(@Valid @RequestBody JobScheduleRequest job) throws JsonProcessingException {
        log.info("Request received to save job with {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(job));
        try {
            return scheduleJobService.saveOrUpdate(job);
        } catch (Exception e) {
            log.error("Exception occurred while submitting new job", e);
            throw ScheduleJobException.builder().jobName(job.getJobName()).groupName(job.getJobGroup()).errorCode("E0001").errorMessage(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/trigger")
    public JobScheduleResponse runJob(@Valid @RequestBody JobUpdateRequest jobUpdateRequest) throws JsonProcessingException {
        log.info("Request received to trigger the job with {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jobUpdateRequest));
        try {
            return scheduleJobService.startJobNow(jobUpdateRequest);
        } catch (Exception e) {
            log.error("Exception occurred in trigger job", e);
            throw ScheduleJobException.builder().jobName(jobUpdateRequest.getJobName()).groupName(jobUpdateRequest.getJobGroup()).errorCode("E0001").errorMessage(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/pause")
    public JobScheduleResponse pauseJob(@Valid @RequestBody JobUpdateRequest jobUpdateRequest) {
        log.info("Request received to pause job with {}", jobUpdateRequest);
        try {
            return scheduleJobService.pauseJob(jobUpdateRequest);
        } catch (Exception e) {
            log.error("Exception occurred during pause job", e);
            throw ScheduleJobException.builder().jobName(jobUpdateRequest.getJobName()).groupName(jobUpdateRequest.getJobGroup()).errorCode("E0001").errorMessage(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/resume")
    public JobScheduleResponse resumeJob(@Valid @RequestBody JobUpdateRequest jobUpdateRequest) {
        log.info("Request received to resume job with {}", jobUpdateRequest);
        try {
            return scheduleJobService.resumeJob(jobUpdateRequest);
        } catch (Exception e) {
            log.error("Exception occurred while resuming job", e);
            throw ScheduleJobException.builder().jobName(jobUpdateRequest.getJobName()).groupName(jobUpdateRequest.getJobGroup()).errorCode("E0001").errorMessage(e.getMessage()).build();
        }
    }

    @PostMapping(value = "/delete")
    public JobScheduleResponse deleteJob(@Valid @RequestBody JobUpdateRequest jobUpdateRequest) {
        log.info("Request received to delete job with {}", jobUpdateRequest);
        try {
            return scheduleJobService.deleteJob(jobUpdateRequest);
        } catch (SchedulerException e) {
            log.error("Exception occurred while deleting the job", e);
            throw ScheduleJobException.builder().jobName(jobUpdateRequest.getJobName()).groupName(jobUpdateRequest.getJobGroup()).errorCode("E0001").errorMessage(e.getMessage()).build();
        }
    }
}
