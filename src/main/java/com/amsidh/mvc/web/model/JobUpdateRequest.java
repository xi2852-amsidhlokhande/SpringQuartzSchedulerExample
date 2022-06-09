package com.amsidh.mvc.web.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class JobUpdateRequest {
    @NotNull
    private String jobName;
    @NotNull
    private String jobGroup;
}
