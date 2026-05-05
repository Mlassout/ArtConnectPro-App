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
                SELECT e.Title, e.Start_Date, e.End_Date, e.Description,
                       e.Curator_Name, e.Theme,
                       g.Gallery_Id, g.Name AS Gallery_Name, g.Address,
                       g.Owner_Name, g.Contact_Phone, g.Rating, g.Website
                FROM vw_exhibition_gallery e
                JOIN Gallery g ON e.Gallery_Id = g.Gallery_Id
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


    }

    @Override
    public void update(Exhibition exhibition) {

    }

    @Override
    public void delete(String title) {

    }
}
