package com.ChickenTest.demoChickenTest.repository;

import com.ChickenTest.demoChickenTest.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFarmRepository extends JpaRepository<Farm, Long> {
}
