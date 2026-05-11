package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.persistence.*;
import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;
import java.sql.*;
/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    private static final Connection connection = DatabaseConfig.getConnection();
    private static final InMemoryArtistService artistService = new InMemoryArtistService(new JdbcArtistDao(connection));
    private static final InMemoryArtworkService artworkService = new InMemoryArtworkService(new JdbcArtworkDao(connection));
    private static final InMemoryGalleryService galleryService = new InMemoryGalleryService(new JdbcGalleryDao(connection),new JdbcExhibitionDao(connection));
    private static final InMemoryWorkshopService workshopService = new InMemoryWorkshopService(new JdbcWorkshopDao(connection));
    private static final InMemoryCommunityService communityService = new InMemoryCommunityService(new JbdcCommunityMemberDao(connection));



    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}
