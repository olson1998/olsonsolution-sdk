package com.olsonsolution.common.spring.application.datasource.organization.entity.embadabble;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor

@Embeddable
@NoArgsConstructor
public class Cell {

    @Column(name = "organization_code", nullable = false)
    private Integer organizationCode;

    @Column(name = "branch", length = 31, nullable = false)
    private String branch;

}
