package com.olsonsolution.common.spring.application.datasource.modern.entity.embaddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class PersonTeamBound {

    private Long teamId;

    private Long personId;

}
