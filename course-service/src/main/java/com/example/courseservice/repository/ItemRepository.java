package com.example.courseservice.repository;

import com.example.courseservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> { }
