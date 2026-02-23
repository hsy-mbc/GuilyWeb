package org.hsy.spring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hsy.spring.entity.DietLogEntity;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DietLogResponseDTO {
    private Long id;
    private Long foodNo;
    private String mealType;
    private Double quantity;
    private LocalDate eatDate;

    public static DietLogResponseDTO from(DietLogEntity entity) {
        return DietLogResponseDTO.builder()
                .id(entity.getLogNo())
                .foodNo(entity.getFood().getFoodNo())
                .mealType(entity.getMealType())
                .quantity(entity.getQuantity())
                .eatDate(entity.getEatDate())
                .build();
    }
}