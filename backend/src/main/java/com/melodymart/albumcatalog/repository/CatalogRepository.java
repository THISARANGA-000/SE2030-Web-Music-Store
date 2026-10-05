package com.melodymart.albumcatalog.repository;
import com.melodymart.albumcatalog.model.Catalog;

import com.melodymart.albumcatalog.model.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Integer> {
    List<Catalog> findByCatalogNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
