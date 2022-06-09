package com.amsidh.mvc.web.model;

import lombok.*;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class JobScheduleRequest {

    @NotNull
    private String jobName;
    @NotNull
    private String jobGroup;

    @NotNull
    @URL
    private String remoteMSUrl;

    private String cronExpression;
    @NotNull
    private String desc;
    private Long repeatIntervalTime;
}
