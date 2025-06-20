package com.api.examen.controllers;

import com.api.examen.models.Address;
import com.api.examen.models.User;
import com.api.examen.repository.AddressRepository;
import com.api.examen.repository.UserRepository;
import com.api.examen.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.api.examen.security.Hash;

@RestController
@RequestMapping(value = "/chakray/spring-boot/v1")
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AddressRepository addressRepository;

    @GetMapping(value = "/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(@RequestParam(required = false) String sortedBy, @RequestParam(required = false, defaultValue = "asc") String order) {
        UUID uuid = UUID.randomUUID();
        try {
            List<User> users = userRepository.findAll();
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
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Comparator<User> getComparator(String sortedBy) {
        switch (sortedBy) {
            case "id":
                return Comparator.comparing(User::getId);
            case "name":
                return Comparator.comparing(User::getName);
            case "email":
                return Comparator.comparing(User::getEmail);
            case "created_at":
                return Comparator.comparing(User::getCreated_at);
            default:
                return Comparator.comparing(User::getId); // Por defecto
        }
    }

    @GetMapping(value = "/users/{user_id}/addresses")
    public ResponseEntity<Map<String, Object>> getAddressesByUserId(@PathVariable("user_id") int user_id) {
        UUID uuid = UUID.randomUUID();
        try {
            List<Address> addresses = addressRepository.findByUserId(user_id);
            if (addresses.isEmpty()) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(ApiResponse.ok(addresses, uuid), HttpStatus.OK);
            }
        } catch (Exception ex) {
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/users/{user_id}/addresses/{address_id}")
    public ResponseEntity<Map<String, Object>> updateAddressByUserId(@PathVariable("user_id") int user_id, @PathVariable("address_id") int address_id, @RequestBody Address address) {
        UUID uuid = UUID.randomUUID();
        try {
            // Buscar la dirección
            Address existingAddress = addressRepository.findById(address_id).orElse(null);
            if (existingAddress == null) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            }
            // Verificar que la dirección pertenece al usuario
            if (existingAddress.getUser() == null || existingAddress.getUser().getId() != user_id) {
                List<String> details = List.of("No se pudo ejecutar de manera correcta su petición");
                return new ResponseEntity<>(ApiResponse.badRequest(details, uuid), HttpStatus.BAD_REQUEST);
            }
            // Actualizar campos permitidos
            existingAddress.setName(address.getName());
            existingAddress.setStreet(address.getStreet());
            existingAddress.setCountry_code(address.getCountry_code());
            // Guardar cambios
            addressRepository.save(existingAddress);
            return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK);
        } catch (Exception ex) {
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        UUID uuid = UUID.randomUUID();
        try {
            // Hashear la contraseña
            String hashedPassword = Hash.SHA1(user.getPassword());
            user.setPassword(hashedPassword);
            // Establecer la relación bidireccional de direcciones
            if (user.getAddresses() != null) {
                for (Address address : user.getAddresses()) {
                    address.setUser(user);
                }
            }
            // Guardar el usuario
            userRepository.save(user);
            return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.CREATED);

        } catch (Exception ex) {
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping(value = "/users/{id}")
    public ResponseEntity<Map<String, Object>> patchUser(@PathVariable("id") int id, @RequestBody User updatedUser) {
        UUID uuid = UUID.randomUUID();
        try {
            Optional<User> optionalUser = userRepository.findById(id);
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
                userRepository.save(user);
                return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK);
            }
        } catch (Exception ex) {
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") int id) {
        UUID uuid = UUID.randomUUID();
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK); // Eliminado con éxito
            } else {
                List<String> details = List.of("No se pudo ejecutar de manera correcta su petición");
                return new ResponseEntity<>(ApiResponse.badRequest(details, uuid), HttpStatus.BAD_REQUEST); // No existe el usuario
            }
        } catch (Exception ex) {
            List<String> details = Arrays.asList("Error interno del servidor", "Por favor intente más tarde");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
