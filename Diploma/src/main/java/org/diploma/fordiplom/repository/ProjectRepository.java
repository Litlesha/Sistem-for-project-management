package org.diploma.fordiplom.repository;

import org.diploma.fordiplom.entity.ProjectEntity;
import org.diploma.fordiplom.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity,Long> {
    @Query("SELECT p FROM ProjectEntity p JOIN p.users u WHERE u.email = :email AND p.isDeleted = false ")
    List<ProjectEntity> findByUserEmail(@Param("email") String email);
    @Query("""
    select pur.project from ProjectUserRoleEntity pur
    where pur.user.email = :email
      and pur.project.isDeleted = false
    order by pur.project.createdAt desc
""")
    List<ProjectEntity> findRecentProjectsForUser(@Param("email") String email, Pageable pageable);
}
