package com.olsonsolution.common.spring.application.datasource.classic.entity;

import com.olsonsolution.common.spring.application.datasource.classic.entity.embaddable.ClassicPersonTeamBound;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "PTMBND")
public class ClassicPersonTeamBoundData {

    @EmbeddedId
    private ClassicPersonTeamBound valueMap;

}
