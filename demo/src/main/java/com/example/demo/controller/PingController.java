package com.example.demo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PingController {

    List<String> users = new ArrayList<>();

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }
    @PostMapping("/post")
    public String addUser(@Valid @Size(min = 3,max=15) @RequestParam String user) { /*@Valid es una anotación para validar los datos
                                                                            ingresados, en este caso valida que no sea nulo*/
        users.add(user);
        return "Usuario añadido!";
    }
    @GetMapping("/users")
    public List<String> getUsers() {
        return users;
    }

}
