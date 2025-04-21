package com.olsonsolution.common.spring.application.datasource.organization.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.annotation.migration.ForeignKey;
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

@ChangeSet

@Entity
@Table(name = "company")
public class CompanyData {

    @Id
    @Column(name = "code", nullable = false, unique = true)
    private Integer code;

    @Column(name = "organization_code", nullable = false, unique = true)
    @ForeignKey(name = "fk_organization_code", referenceTable = "organization", referenceColumn = "code")
    private Integer organizationCode;

    @Column(name = "long_name", nullable = false, unique = true)
    private String longName;

    @Column(name = "name", length = 63, nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", length = 15, nullable = false, unique = true)
    private String shortName;

    private String description;

}
