package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.repository.SitterProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SitterProfileService {

    @Autowired
    private SitterProfileRepository sitterProfileRepository;

    public SitterProfile create(SitterProfile sitterProfile) {
        return sitterProfileRepository.save(sitterProfile);
    }

    public List<SitterProfile> getAll() {
        return sitterProfileRepository.findAll();
    }

    public SitterProfile getById(UUID id) {
        return sitterProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SitterProfile not found with id: " + id));
    }

    public SitterProfile update(UUID id, SitterProfile updated) {
        SitterProfile existing = getById(id);
        existing.setDisplayName(updated.getDisplayName());
        existing.setIntroduction(updated.getIntroduction());
        existing.setMyPlace(updated.getMyPlace());
        existing.setServices(updated.getServices());
        existing.setAddressDetail(updated.getAddressDetail());
        existing.setDistrict(updated.getDistrict());
        existing.setSubDistrict(updated.getSubDistrict());
        existing.setProvince(updated.getProvince());
        existing.setPostCode(updated.getPostCode());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setExperienceYears(updated.getExperienceYears());
        existing.setBankName(updated.getBankName());
        existing.setAccountNumber(updated.getAccountNumber());
        existing.setAccountName(updated.getAccountName());
        existing.setBankCode(updated.getBankCode());
        existing.setBookBankImageUrl(updated.getBookBankImageUrl());
        return sitterProfileRepository.save(existing);
    }

    public void delete(UUID id) {
        SitterProfile existing = getById(id);
        sitterProfileRepository.delete(existing);
    }
}
