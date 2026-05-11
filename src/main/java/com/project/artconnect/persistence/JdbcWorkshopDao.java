package com.project.artconnect.persistence;


import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {
    private final Connection connection;

    public JdbcWorkshopDao(Connection connection) {
        this.connection = connection;
    }

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setWorkshopId(rs.getString("Workshop_ID"));
        workshop.setTitle(rs.getString("Title"));
        workshop.setDurationMinutes(rs.getInt("Duration_Minutes"));
        workshop.setMaxParticipants(rs.getInt("Max_Participants"));
        workshop.setPrice(rs.getDouble("Price"));
        workshop.setLocation(rs.getString("Location"));
        workshop.setDescription(rs.getString("Description"));
        workshop.setLevel(rs.getString("Level"));

        // Conversion DATETIME SQL → LocalDateTime Java
        Timestamp dateTime = rs.getTimestamp("Date_Time");
        if (dateTime != null) {
            workshop.setDate(dateTime.toLocalDateTime());
        }

        // On récupère l'artiste instructeur s'il est jointuré
        String artistId = rs.getString("Artist_Id");
        if (artistId != null) {
            Artist instructor = new Artist();
            instructor.setArtistId(artistId);
            instructor.setName(rs.getString("Artist_Name"));
            instructor.setCity(rs.getString("Artist_City"));
            workshop.setInstructor(instructor);
        }

        return workshop;
    }

    @Override
    public Optional<Workshop> findById(String id) {
        String sql = """
                SELECT w.*, a.Artist_Id, a.Name AS Artist_Name, a.City AS Artist_City
                FROM Workshop w
                LEFT JOIN Superviser s ON w.Workshop_ID = s.Workshop_ID
                LEFT JOIN Artist a ON s.Artist_Id = a.Artist_Id
                WHERE w.Workshop_ID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById Workshop : " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> workshops = new ArrayList<>();
        String sql = """
                SELECT w.*, a.Artist_Id, a.Name AS Artist_Name, a.City AS Artist_City
                FROM Workshop w
                LEFT JOIN Superviser s ON w.Workshop_ID = s.Workshop_ID
                LEFT JOIN Artist a ON s.Artist_Id = a.Artist_Id
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                workshops.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Workshop : " + e.getMessage(), e);
        }
        return workshops;
    }
}
