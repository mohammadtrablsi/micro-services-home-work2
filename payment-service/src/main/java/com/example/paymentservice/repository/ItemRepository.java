package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> { }
