package com.melodymart.common.repository;
import com.melodymart.common.model.Listener;

import com.melodymart.common.model.Listener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListenerRepository extends JpaRepository<Listener, Integer> {
}
