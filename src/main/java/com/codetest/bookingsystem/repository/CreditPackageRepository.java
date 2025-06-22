package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.model.CreditPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditPackageRepository extends JpaRepository<CreditPackage, Long> {
    List<CreditPackage> findByCountry(Country country);
}