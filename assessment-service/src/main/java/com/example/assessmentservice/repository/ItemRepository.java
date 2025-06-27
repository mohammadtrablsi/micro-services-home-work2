package com.example.assessmentservice.repository;

import com.example.assessmentservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> { }
