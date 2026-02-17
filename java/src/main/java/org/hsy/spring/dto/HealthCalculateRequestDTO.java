package org.hsy.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthCalculateRequestDTO {
    private String gender;
    private Double height;
    private Double weight;
    private Integer age;
    private Double activityCoefficient;
    private String lifestylePattern;
    private String dietGoal;
}
