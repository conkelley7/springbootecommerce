package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT a FROM Address a JOIN a.users u WHERE u.userId = :userId")
    Page<Address> findAllByUserId(@Param("userId")Long userId, Pageable pageable);
}
