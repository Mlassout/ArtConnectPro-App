package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;

import java.util.List;
import java.sql.*;
import java.util.ArrayList;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    private final Connection connection;

    public JdbcArtworkDao(Connection connection) {
        this.connection = connection;
    }

    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(rs.getString("Artwork_Id"));
        artwork.setTitle(rs.getString("Title"));
        artwork.setCreationYear(rs.getInt("Creation_Year"));
        artwork.setType(rs.getString("Type"));
        artwork.setMedium(rs.getString("Medium"));
        artwork.setDimensions(rs.getString("Dimensions"));
        artwork.setDescription(rs.getString("Descriptions"));
        artwork.setPrice(rs.getDouble("Price"));


        String statusStr = rs.getString("Status");
        if (statusStr != null) {
            switch (statusStr) {
                case "Sold" -> artwork.setStatus(Artwork.Status.SOLD);
                case "Exhibited" -> artwork.setStatus(Artwork.Status.EXHIBITED);
                default -> artwork.setStatus(Artwork.Status.FOR_SALE);
            }
        }
        Artist artist = new Artist();
        artist.setArtistId(rs.getString("Artist_Id"));
        artist.setName(rs.getString("Artist_Name"));
        artist.setCity(rs.getString("Artist_City"));
        artwork.setArtist(artist);

        return artwork;
    }

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();

        // On utilise la vue vw_artwork_details qui joint déjà Artist
        String sql = """
                SELECT Artwork_Id, Title, Creation_Year, Type, Medium,
                       Dimensions, Descriptions, Price, Status,
                       Artist_Id, Artist_Name, Artist_City
                FROM vw_artwork_details
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Artwork artwork = mapRow(rs);
                artworks.add(artwork);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll Artwork : " + e.getMessage(), e);
        }

        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        String sql = """
                INSERT INTO ArtWork
                    (Artwork_Id, Title, Creation_Year, Type, Medium, Dimensions, Descriptions, Price, Status, Artist_Id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artwork.getArtworkId());
            stmt.setString(2, artwork.getTitle());
            stmt.setInt(3, artwork.getCreationYear());
            stmt.setString(4, artwork.getType());
            stmt.setString(5, artwork.getMedium());
            stmt.setString(6, artwork.getDimensions());
            stmt.setString(7, artwork.getDescription());
            stmt.setDouble(8, artwork.getPrice());
            stmt.setString(9, artwork.getStatus().name());
            stmt.setString(10, artwork.getArtist().getArtistId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur save Artwork : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String sql = """
                UPDATE ArtWork
                SET Creation_Year = ?,
                    Type          = ?,
                    Medium        = ?,
                    Dimensions    = ?,
                    Descriptions  = ?,
                    Price         = ?,
                    Status        = ?
                WHERE Title = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artwork.getCreationYear());
            stmt.setString(2, artwork.getType());
            stmt.setString(3, artwork.getMedium());
            stmt.setString(4, artwork.getDimensions());
            stmt.setString(5, artwork.getDescription());
            stmt.setDouble(6, artwork.getPrice());
            stmt.setString(7, artwork.getStatus().name());
            stmt.setString(8, artwork.getTitle());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur update Artwork : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String title) {
        String sqlGetId = "SELECT Artwork_Id FROM ArtWork WHERE Title = ?";

        try (PreparedStatement stmtGetId = connection.prepareStatement(sqlGetId)) {
            stmtGetId.setString(1, title);

            try (ResultSet rs = stmtGetId.executeQuery()) {
                if (rs.next()) {
                    String artworkId = rs.getString("Artwork_Id");

                    // Suppression des tags liés
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM `Catégoriser` WHERE Artwork_Id = ?")) {
                        s.setString(1, artworkId);
                        s.executeUpdate();
                    }
                    // Suppression des liens avec les expositions
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM `Présenter` WHERE Artwork_Id = ?")) {
                        s.setString(1, artworkId);
                        s.executeUpdate();
                    }

                    // Suppression des reviews liées
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Review WHERE Artwork_Id = ?")) {
                        s.setString(1, artworkId);
                        s.executeUpdate();
                    }

                    // Suppression de l'oeuvre elle-même
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM ArtWork WHERE Artwork_Id = ?")) {
                        s.setString(1, artworkId);
                        s.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete Artwork : " + e.getMessage(), e);
        }

    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String sql = """
                SELECT Artwork_Id, Title, Creation_Year, Type, Medium,
                       Dimensions, Descriptions, Price, Status,
                       Artist_Id, Artist_Name, Artist_City
                FROM vw_artwork_details
                WHERE Artist_Name = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artistName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = mapRow(rs);
                    artworks.add(artwork);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByArtistName Artwork : " + e.getMessage(), e);
        }
        return artworks;
    }
}
