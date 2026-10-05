package com.melodymart.common.repository;
import com.melodymart.common.model.Administrator;

import com.melodymart.common.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Integer> {
}
