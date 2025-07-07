package org.diploma.fordiplom.entity;

import jakarta.persistence.*;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="git_hub_repos")
public class RepositoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Name("id")
    private Long id;
    @Name("name")
    private String name;
    @Name("owner")
    private String owner;
    @Name("url")
    private String url;

    @ManyToMany(mappedBy = "repositories")
    private Set<UserEntity> users = new HashSet<>();


}

