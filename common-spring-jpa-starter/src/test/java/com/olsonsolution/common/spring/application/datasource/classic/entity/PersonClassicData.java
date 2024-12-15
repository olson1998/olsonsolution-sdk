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
@Table(name = "PRSDTA")
public class PersonClassicData {

    @Id
    @Column(name = "PRSID")
    private Long id;

    @Column(name = "PRSNM")
    private String name;

    @Column(name = "PRSSNM")
    private String surname;

    @Column(name = "PRSGN")
    private String gender;
}
