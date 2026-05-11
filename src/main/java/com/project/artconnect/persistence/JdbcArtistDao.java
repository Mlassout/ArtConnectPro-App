package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;

/**
 * JDBC implementation for ArtistDao.
 * TODO: Students must implement this using JDBC and SQL.
 */
public class JdbcArtistDao implements ArtistDao {
    private final Connection connection;

    public JdbcArtistDao(Connection connection) {
        this.connection = connection;
    }

    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setArtistId(rs.getString("Artist_Id"));
        artist.setName(rs.getString("Name"));
        artist.setBio(rs.getString("Bio"));
        artist.setContactEmail(rs.getString("Contact_Email"));
        artist.setPhone(rs.getString("Phone"));
        artist.setCity(rs.getString("City"));
        artist.setWebsite(rs.getString("Website"));
        artist.setSocialMedia(rs.getString("Social_Media"));
        artist.setActive(rs.getBoolean("Is_Active"));

        // Birth_Year est une DATE en SQL, on extrait juste l'année
        Date birthDate = rs.getDate("Birth_Year");
        if (birthDate != null) {
            artist.setBirthYear(birthDate.toLocalDate().getYear());
        }

        return artist;
    }

    @Override
    public List<Artist> findAll() {
        // TODO: Implement SELECT * FROM artist
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                artists.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Artist : " + e.getMessage(), e);
        }

        return artists;
    }

    @Override
    public void save(Artist artist) {
        // TODO: Implement INSERT INTO artist(...) VALUES(...)
        String sql = """
                INSERT INTO Artist
                    (Artist_Id, Name, Bio, Birth_Year, Contact_Email, Phone, City, Website, Social_Media, Is_Active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artist.getArtistId());
            stmt.setString(2, artist.getName());
            stmt.setString(3, artist.getBio());
            stmt.setInt(4, artist.getBirthYear());
            stmt.setString(5, artist.getContactEmail());
            stmt.setString(6, artist.getPhone());
            stmt.setString(7, artist.getCity());
            stmt.setString(8, artist.getWebsite());
            stmt.setString(9, artist.getSocialMedia());
            stmt.setBoolean(10, artist.isActive());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Artist : " + e.getMessage(), e);
        }

    }

    @Override
    public void update(Artist artist) {
        // TODO: Implement UPDATE artist SET ... WHERE name = ?
        String sql = """
                UPDATE Artist
                SET Bio          = ?,
                    Contact_Email = ?,
                    Phone        = ?,
                    City         = ?,
                    Website      = ?,
                    Social_Media = ?,
                    Is_Active    = ?
                WHERE Name = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artist.getBio());
            stmt.setString(2, artist.getContactEmail());
            stmt.setString(3, artist.getPhone());
            stmt.setString(4, artist.getCity());
            stmt.setString(5, artist.getWebsite());
            stmt.setString(6, artist.getSocialMedia());
            stmt.setBoolean(7, artist.isActive());
            stmt.setString(8, artist.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Artist : " + e.getMessage(), e);
        }

    }

    @Override
    public void delete(String artistName) {
        // TODO: Implement DELETE FROM artist WHERE name = ?
        String sqlGetId = "SELECT Artist_Id FROM Artist WHERE Name = ?";

        try (PreparedStatement stmtGetId = connection.prepareStatement(sqlGetId)) {
            stmtGetId.setString(1, artistName);

            try (ResultSet rs = stmtGetId.executeQuery()) {
                if (rs.next()) {
                    String artistId = rs.getString("Artist_Id");

                    // Suppression des liens Pratiquer
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Pratiquer WHERE Artist_Id = ?")) {
                        s.setString(1, artistId);
                        s.executeUpdate();
                    }
                    // Suppression des liens Superviser
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Superviser WHERE Artist_Id = ?")) {
                        s.setString(1, artistId);
                        s.executeUpdate();
                    }

                    // Suppression de l'artiste lui-même
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Artist WHERE Artist_Id = ?")) {
                        s.setString(1, artistId);
                        s.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Artist : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        // TODO: Implement SELECT * FROM artist WHERE city = ?
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM Artist WHERE City = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, city);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    artists.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByCity Artist : " + e.getMessage(), e);
        }

        return artists;
    }
}
