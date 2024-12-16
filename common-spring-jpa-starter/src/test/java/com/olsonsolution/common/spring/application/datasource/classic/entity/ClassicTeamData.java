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
@Table(name = "TMMDTA")
public class ClassicTeamData {

    @Id
    @Column(name = "TMMID")
    private Long id;

    @Column(name = "TMMCD")
    private String code;

    @Column(name = "TMMNM")
    private String name;

}
