package com.olsonsolution.common.spring.application.datasource.classic.entity;

import com.olsonsolution.common.spring.application.datasource.migration.annotation.ChangeSet;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@ChangeSet

@Entity
@Table(name = "TMMDTA")
public class ClassicTeamData {

    @Id
    @Column(name = "TMMID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TMMSEQ")
    @SequenceGenerator(name = "TMMSEQ", sequenceName = "TMMSEQ", allocationSize = 1)
    private Long id;

    @Column(name = "TMMCD", length = 7)
    private String code;

    @Column(name = "TMMNM", length = 63)
    private String name;

}
