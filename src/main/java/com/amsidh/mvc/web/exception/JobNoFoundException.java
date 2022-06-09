package com.amsidh.mvc.web.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class JobNoFoundException extends RuntimeException {
    private String jobName;
    private String groupName;
    private String errorCode;
    private String errorMessage;
}
