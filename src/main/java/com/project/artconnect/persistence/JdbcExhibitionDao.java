package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class JdbcExhibitionDao implements ExhibitionDao {
    private final Connection connection;

    public JdbcExhibitionDao(Connection connection) {
        this.connection = connection;
    }

    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setExhibitionId(rs.getString("Exhibition_Id"));
        exhibition.setTitle(rs.getString("Title"));
        exhibition.setStartDate(rs.getDate("Start_Date").toLocalDate());
        exhibition.setEndDate(rs.getDate("End_Date").toLocalDate());
        exhibition.setDescription(rs.getString("Description"));
        exhibition.setCuratorName(rs.getString("Curator_Name"));
        exhibition.setTheme(rs.getString("Theme"));
        return exhibition;
    }

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        String sql = """
        SELECT Exhibition_Id, Title, Start_Date, End_Date, Description,
               Curator_Name, Theme,
               Gallery_Id, Gallery_Name, Address,
               Owner_Name, Rating, Website
        FROM vw_exhibition_gallery
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Exhibition exhibition = mapRow(rs);
                Gallery gallery = new Gallery();
                gallery.setGalleryId(rs.getString("Gallery_Id"));
                gallery.setName(rs.getString("Gallery_Name"));
                gallery.setAddress(rs.getString("Address"));
                gallery.setOwnerName(rs.getString("Owner_Name"));
                gallery.setContactPhone(rs.getString("Contact_Phone"));
                gallery.setRating(rs.getDouble("Rating"));
                gallery.setWebsite(rs.getString("Website"));

                exhibition.setGallery(gallery);
                exhibitions.add(exhibition);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des expositions", e);
        }

        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sqlExhibition = """
            INSERT INTO Exhibition
                (Exhibition_Id, Title, Start_Date, End_Date, Description, Curator_Name, Theme)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        String generatedId = exhibition.getExhibitionId();
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sqlExhibition)) {
                stmt.setString(1, generatedId);
                stmt.setString(2, exhibition.getTitle());
                stmt.setDate(3, Date.valueOf(exhibition.getStartDate()));
                stmt.setDate(4, Date.valueOf(exhibition.getEndDate()));
                stmt.setString(5, exhibition.getDescription());
                stmt.setString(6, exhibition.getCuratorName());
                stmt.setString(7, exhibition.getTheme());
                stmt.executeUpdate();
            }

            if (exhibition.getGallery() != null) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO Exposer (Gallery_Id, Exhibition_Id) VALUES (?, ?)")) {
                    stmt.setString(1, exhibition.getGallery().getGalleryId());
                    stmt.setString(2, generatedId);
                    stmt.executeUpdate();
                }
            }

            connection.commit();

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erreur lors de l'enregistrement de l'exposition", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }

    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = """
                UPDATE Exhibition
                SET Start_Date   = ?,
                    End_Date     = ?,
                    Description  = ?,
                    Curator_Name = ?,
                    Theme        = ?
                WHERE Title = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(exhibition.getStartDate()));
            stmt.setDate(2, Date.valueOf(exhibition.getEndDate()));
            stmt.setString(3, exhibition.getDescription());
            stmt.setString(4, exhibition.getCuratorName());
            stmt.setString(5, exhibition.getTheme());
            stmt.setString(6, exhibition.getTitle());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'exposition", e);
        }

    }

    @Override
    public void delete(String title) {
        // On supprime d'abord les dépendances (Exposer et Présenter) avant l'exposition
        String sqlGetId = "SELECT Exhibition_Id FROM Exhibition WHERE Title = ?";
        try (PreparedStatement stmtGetId = connection.prepareStatement(sqlGetId)) {
            stmtGetId.setString(1, title);

            try (ResultSet rs = stmtGetId.executeQuery()) {
                if (rs.next()) {
                    String exhibitionId = rs.getString("Exhibition_Id");

                    // Suppression des liens Présenter
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM `Présenter` WHERE Exhibition_Id = ?")) {
                        s.setString(1, exhibitionId);
                        s.executeUpdate();
                    }

                    // Suppression des liens Exposer
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Exposer WHERE Exhibition_Id = ?")) {
                        s.setString(1, exhibitionId);
                        s.executeUpdate();
                    }

                    // Suppression de l'exposition elle-même
                    try (PreparedStatement s = connection.prepareStatement(
                            "DELETE FROM Exhibition WHERE Exhibition_Id = ?")) {
                        s.setString(1, exhibitionId);
                        s.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'exposition", e);
        }
    }
}
