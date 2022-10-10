package com.borodulin.cloudrepository.dao;

import com.borodulin.cloudrepository.model.UserCloud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCloudRepository extends JpaRepository<UserCloud, Long> {
    UserCloud findByLogin(String login);
}
