package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.ArtworkService;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryGalleryService implements GalleryService {
    private final GalleryDao galleryDao;
    private final ExhibitionDao exhibitionDao;
    public InMemoryGalleryService(GalleryDao galleryDao, ExhibitionDao exhibitionDao) {
        // initData after other services if needed, but Gallery is top-level
        this.galleryDao=galleryDao;
        this.exhibitionDao=exhibitionDao;
    }



    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return galleryDao.findAll().stream()
                .filter(g->g.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null)
            return Collections.emptyList();
        return exhibitionDao.findAll().stream()
                .filter(e->e.getGallery()!=null && e.getGallery().getName().equalsIgnoreCase(gallery.getName()))
                .collect(Collectors.toList());
    }
}
