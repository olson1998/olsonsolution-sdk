package com.olsonsolution.common.spring.application.datasource.classic.entity.embaddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class ClassicPersonTeamBound {

    @Column(name = "TEMID")
    private Long teamId;

    @Column(name = "PRSID")
    private Long personId;

}
