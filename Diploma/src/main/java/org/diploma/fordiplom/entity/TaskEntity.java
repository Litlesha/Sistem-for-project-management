package org.diploma.fordiplom.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task", schema = "diploma")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tusk", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;
    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @Column(name = "priority", length = 50)
    private String priority;
    @Size(max=45)
    @Column(name="task_type")
    private String taskType;
    @Column(name="task_key")
    private String taskKey;
    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;
    @Column(name = "position")
    private Integer position;
    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    private Instant updatedAt;
    @Column(name = "is_complited")
    private Boolean isCompleted;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executor_user_id") // новое поле
    private UserEntity executor;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sprint_id")
    private SprintEntity sprint;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "task_tag", schema = "diploma",
            joinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id_tusk"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id_tag")
    )
    private Set<TagEntity> tags = new HashSet<>();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id") //
    private TeamEntity team;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskFileRelation> fileRelations = new HashSet<>();

}
