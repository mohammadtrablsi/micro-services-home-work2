package com.example.userservice.controller;

import com.example.userservice.entity.Item;
import com.example.userservice.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class ItemController {
    private final ItemRepository repo;
    public ItemController(ItemRepository repo) { this.repo = repo; }

    @PostMapping
    public Item create(@RequestBody Item item) { return repo.save(item); }

    @GetMapping
    public List<Item> getAll() { return repo.findAll(); }

    @GetMapping("/<built-in function id>")
    public Item getOne(@PathVariable Long id) { return repo.findById(id).orElseThrow(); }
}
