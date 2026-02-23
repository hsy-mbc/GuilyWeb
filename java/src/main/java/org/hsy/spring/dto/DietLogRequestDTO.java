package org.hsy.spring.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DietLogRequestDTO {
    private Long foodNo;
    private String mealType;
    private Double quantity;
    private LocalDate eatDate;
}