package com.olsonsolution.common.spring.application.datasource.modern.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "team")
public class TeamData {

    @Id
    private Long id;

    private String code;

    private String name;

}
