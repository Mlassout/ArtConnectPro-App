package com.project.artconnect.persistence;


import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;

import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        return List.of();
    }
}
