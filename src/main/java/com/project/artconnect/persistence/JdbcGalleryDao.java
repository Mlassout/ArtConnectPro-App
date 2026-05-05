package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    private final Connection connection;

    public JdbcGalleryDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT * FROM Gallery WHERE Gallery_Id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(id));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById Gallery : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        String sql = "SELECT * FROM Gallery";
        List<Gallery> galleries = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                galleries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Gallery : " + e.getMessage(), e);
        }
        return galleries;
    }

    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setGalleryId(rs.getString("Gallery_Id"));
        g.setName(rs.getString("Name"));
        g.setAddress(rs.getString("Address"));
        g.setOwnerName(rs.getString("Owner_Name"));
        g.setOpeningHours(rs.getString("Opening_Hours"));
        g.setContactPhone(rs.getString("Contact_Phone"));
        g.setRating(rs.getDouble("Rating"));
        g.setWebsite(rs.getString("Website"));
        return g;
    }
}
