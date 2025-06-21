package com.api.examen.controllers;

import com.api.examen.entities.Address;
import com.api.examen.entities.User;
import com.api.examen.services.UserService;
import com.api.examen.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.api.examen.security.Hash;

@RestController
@RequestMapping(value = "${api.base.url}")
public class UserController {


    @Autowired
    private UserService userService;

    @GetMapping(value = "/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(@RequestParam(required = false) String sortedBy, @RequestParam(required = false, defaultValue = "asc") String order) {
        UUID uuid = UUID.randomUUID();
        try {
            List<User> users = userService.findAll();
            if (users.isEmpty()) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            } else {
                if (sortedBy != null && !sortedBy.isEmpty()) {
                    Comparator<User> comparator = getComparator(sortedBy);
                    if ("desc".equalsIgnoreCase(order)) {
                        comparator = comparator.reversed();
                    }
                    users.sort(comparator);
                }
                return new ResponseEntity<>(ApiResponse.ok(users, uuid), HttpStatus.OK);
            }
        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Comparator<User> getComparator(String sortedBy) {
        switch (sortedBy) {
            case "name":
                return Comparator.comparing(User::getName);
            case "email":
                return Comparator.comparing(User::getEmail);
            case "created_at":
                return Comparator.comparing(User::getCreated_at);
            default:
                return Comparator.comparing(User::getId);
        }
    }


    @PostMapping(value = "/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        UUID uuid = UUID.randomUUID();
        try {
            // Hashea la contraseña
            String hashedPassword = Hash.SHA1(user.getPassword());
            user.setPassword(hashedPassword);
            // Establece la relación bidireccional de direcciones del nuevo usuario
            if (user.getAddresses() != null) {
                for (Address address : user.getAddresses()) {
                    address.setUser(user);
                }
            }
            // Guarda el registro
            userService.save(user);
            return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.CREATED);

        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping(value = "/users/{id}")
    public ResponseEntity<Map<String, Object>> patchUser(@PathVariable("id") int id, @RequestBody User updatedUser) {
        UUID uuid = UUID.randomUUID();
        try {
            Optional<User> optionalUser = userService.findById(id);
            if (optionalUser.isEmpty()) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            } else {
                User user = optionalUser.get();
                // Solo actualiza los campos que vienen en el JSON
                if (updatedUser.getName() != null) {
                    user.setName(updatedUser.getName());
                }
                if (updatedUser.getEmail() != null) {
                    user.setEmail(updatedUser.getEmail());
                }
                if (updatedUser.getPassword() != null) {
                    user.setPassword(Hash.SHA1(updatedUser.getPassword()));
                }
                userService.save(user);
                return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK);
            }
        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") int id) {
        UUID uuid = UUID.randomUUID();
        try {
            if (userService.existsById(id)) {
                userService.deleteById(id);
                return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK); // Eliminado con éxito
            } else {
                List<String> details = List.of("No se pudo ejecutar de manera correcta su petición");
                return new ResponseEntity<>(ApiResponse.badRequest(details, uuid), HttpStatus.BAD_REQUEST); // No existe el usuario
            }
        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
