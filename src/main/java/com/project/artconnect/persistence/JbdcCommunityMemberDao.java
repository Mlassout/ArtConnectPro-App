package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JbdcCommunityMemberDao implements CommunityMemberDao {
    private final Connection connection;

    public JbdcCommunityMemberDao(Connection connection) {
        this.connection = connection;
    }

    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember member = new CommunityMember();
        member.setMemberId(rs.getString("Member_Id"));
        member.setName(rs.getString("Name"));
        member.setEmail(rs.getString("Email"));
        member.setPhone(rs.getString("Phone"));
        member.setCity(rs.getString("City"));
        member.setMembershipType(rs.getString("MemberShip_Type"));

        // Birth_Year est une DATE en SQL, on extrait juste l'année
        Date birthDate = rs.getDate("Birth_Year");
        if (birthDate != null) {
            member.setBirthYear(birthDate.toLocalDate().getYear());
        }

        return member;
    }

    @Override
    public Optional<CommunityMember> findById(String id) {
        String sql = "SELECT * FROM Community_Member WHERE Member_Id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById CommunityMember : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> members = new ArrayList<>();
        String sql = "SELECT * FROM Community_Member";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll CommunityMember : " + e.getMessage(), e);
        }

        return members;
    }
}
