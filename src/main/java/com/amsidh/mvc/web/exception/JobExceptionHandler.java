package com.amsidh.mvc.web.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class JobExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ScheduleJobException.class})
    public ResponseEntity handleScheduleJobException(ScheduleJobException scheduleJobException) {
        return ResponseEntity.badRequest().body(JobErrorResponse.builder()
                .jobName(scheduleJobException.getJobName())
                .groupName(scheduleJobException.getGroupName())
                .errorCode(scheduleJobException.getErrorCode())
                .errorMessage(scheduleJobException.getErrorMessage())
                .build());
    }

    @ExceptionHandler(value = {JobNoFoundException.class})
    public ResponseEntity handleJobNoFound(JobNoFoundException jobNoFoundException) {
        return ResponseEntity.ok().body(JobErrorResponse.builder()
                .jobName(jobNoFoundException.getJobName())
                .groupName(jobNoFoundException.getGroupName())
                .errorCode(jobNoFoundException.getErrorCode())
                .errorMessage(jobNoFoundException.getErrorMessage())
                .build());
    }


}
