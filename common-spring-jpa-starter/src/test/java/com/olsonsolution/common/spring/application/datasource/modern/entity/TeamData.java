package com.olsonsolution.common.spring.application.datasource.modern.entity;

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
@Table(name = "team")
public class TeamData {

    @Id
    private Long id;

    private String code;

    private String name;

}
