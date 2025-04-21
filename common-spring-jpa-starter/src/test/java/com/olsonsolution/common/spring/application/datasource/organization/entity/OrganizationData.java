package com.olsonsolution.common.spring.application.datasource.organization.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "organization")
public class OrganizationData {

    @Id
    @Column(name = "code", nullable = false, unique = true)
    private Integer code;

    @Column(name = "long_name", nullable = false, unique = true)
    private String longName;

    @Column(name = "name", length = 63, nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", length = 15, nullable = false, unique = true)
    private String shortName;

    private String description;

}
