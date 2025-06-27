package com.example.userservice.repository;

import com.example.userservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> { }
