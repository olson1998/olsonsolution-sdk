package com.olsonsolution.common.spring.application.datasource.organization.entity;

import com.olsonsolution.common.spring.application.annotation.migration.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.organization.entity.embadabble.Cell;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "organization_cell")
public class CellData {

    @EmbeddedId
    private Cell data;

}
