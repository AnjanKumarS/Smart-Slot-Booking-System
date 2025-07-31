package com.smartslot.repository;

import com.smartslot.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    List<Venue> findByIsActiveTrue();
    
    List<Venue> findByCapacityGreaterThanEqualAndIsActiveTrue(Integer capacity);
    
    List<Venue> findByLocationContainingIgnoreCaseAndIsActiveTrue(String location);
    
    @Query("SELECT v FROM Venue v WHERE v.name LIKE %:name% AND v.isActive = true")
    List<Venue> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);
    
    @Query("SELECT v FROM Venue v WHERE v.hourlyRate BETWEEN :minRate AND :maxRate AND v.isActive = true")
    List<Venue> findByHourlyRateBetweenAndIsActiveTrue(@Param("minRate") BigDecimal minRate, 
                                                       @Param("maxRate") BigDecimal maxRate);
    
    @Query("SELECT v FROM Venue v WHERE v.capacity >= :minCapacity AND v.capacity <= :maxCapacity AND v.isActive = true")
    List<Venue> findByCapacityBetweenAndIsActiveTrue(@Param("minCapacity") Integer minCapacity, 
                                                     @Param("maxCapacity") Integer maxCapacity);
    
    @Query("SELECT COUNT(v) FROM Venue v WHERE v.isActive = true")
    Long countActiveVenues();
}

