package com.olsonsolution.common.spring.application.datasource.classic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "TEMDATA")
public class ClassicTeamData {

    @Id
    @Column(name = "TEMID")
    private Long id;

    @Column(name = "TEMCD")
    private String code;

    @Column(name = "TEMNM")
    private String name;

}
