package com.api.examen.controllers;

import com.api.examen.models.Address;
import com.api.examen.models.User;
import com.api.examen.repository.AddressRepository;
import com.api.examen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import com.api.examen.security.Hash;

@RestController
@RequestMapping(value = "/chakray/spring-boot/v1")
public class UserController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AddressRepository addressRepository;

    @GetMapping(value = "/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String sortedBy, @RequestParam(required = false, defaultValue = "asc") String order) {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            if (sortedBy != null && !sortedBy.isEmpty()) {
                Comparator<User> comparator = getComparator(sortedBy);
                if ("desc".equalsIgnoreCase(order)) {
                    comparator = comparator.reversed();
                }
                users.sort(comparator);
            }
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable("user_id") int user_id) {
        try {
            List<Address> addresses = addressRepository.findByUserId(user_id);
            if (addresses.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(addresses, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/users/{user_id}/addresses/{address_id}")
    public ResponseEntity<Address> updateAddressByUserId(@PathVariable("user_id") int user_id, @PathVariable("address_id") int address_id, @RequestBody Address address) {
        try {
            // Buscar la dirección
            Address existingAddress = addressRepository.findById(address_id).orElse(null);
            if (existingAddress == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Verificar que la dirección pertenece al usuario
            if (existingAddress.getUser() == null || existingAddress.getUser().getId() != user_id) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Actualizar campos permitidos
            existingAddress.setName(address.getName());
            existingAddress.setStreet(address.getStreet());
            existingAddress.setCountry_code(address.getCountry_code());

            // Guardar cambios
            Address saved = addressRepository.save(existingAddress);
            return new ResponseEntity<>(saved, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
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
            User savedUser = userRepository.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping(value = "/users/{id}")
    public ResponseEntity<User> patchUser(@PathVariable("id") int id, @RequestBody User updatedUser) {
        try {
            return userRepository.findById(id).map(user -> {
                // Solo actualiza si el campo viene en el JSON (no null)
                if (updatedUser.getName() != null) {
                    user.setName(updatedUser.getName());
                }
                if (updatedUser.getEmail() != null) {
                    user.setEmail(updatedUser.getEmail());
                }
                if (updatedUser.getPassword() != null) {
                    user.setPassword(Hash.SHA1(updatedUser.getPassword())); // usa el método de SHA-1
                }

                // Guarda el usuario actualizado
                User savedUser = userRepository.save(user);
                return new ResponseEntity<>(savedUser, HttpStatus.OK);
            }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") int id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK); // Eliminado con éxito
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // No existe el usuario
            }
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
