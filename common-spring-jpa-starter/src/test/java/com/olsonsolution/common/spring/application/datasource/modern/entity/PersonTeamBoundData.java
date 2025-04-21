package com.olsonsolution.common.spring.application.datasource.modern.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import com.olsonsolution.common.spring.application.datasource.modern.entity.embaddable.PersonTeamBound;
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
@Table(name = "person_team_bound")
public class PersonTeamBoundData {

    @EmbeddedId
    private PersonTeamBound valueMap;

}
