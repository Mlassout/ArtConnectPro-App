package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;

import java.util.List;
import java.util.Optional;

public class JbdcCommunityMemberDao implements CommunityMemberDao {
    @Override
    public Optional<CommunityMember> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        return List.of();
    }
}
