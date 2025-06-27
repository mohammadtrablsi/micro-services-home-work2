package com.example.courseservice.controller;

import com.example.courseservice.entity.Item;
import com.example.courseservice.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class ItemController {
    private final ItemRepository repo;
    public ItemController(ItemRepository repo) { this.repo = repo; }

    @PostMapping
    public Item create(@RequestBody Item item) { return repo.save(item); }

    @GetMapping
    public List<Item> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Item getOne(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
}
