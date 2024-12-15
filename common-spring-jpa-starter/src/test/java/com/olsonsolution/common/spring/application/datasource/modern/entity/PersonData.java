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
@Table(name = "person")
public class PersonData {

    @Id
    private Long id;

    private String name;

    private String surname;

    private String gender;

}
