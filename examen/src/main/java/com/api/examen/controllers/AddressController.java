package com.api.examen.controllers;

import com.api.examen.entities.Address;
import com.api.examen.services.AddressService;
import com.api.examen.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "${api.base.url}")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping(value = "/users/{user_id}/addresses")
    public ResponseEntity<Map<String, Object>> getAddressesByUserId(@PathVariable("user_id") int user_id) {
        UUID uuid = UUID.randomUUID();
        try {
            List<Address> addresses = addressService.findAllAddressById(user_id);
            if (addresses.isEmpty()) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(ApiResponse.ok(addresses, uuid), HttpStatus.OK);
            }
        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/users/{user_id}/addresses/{address_id}")
    public ResponseEntity<Map<String, Object>> updateAddressByUserId(@PathVariable("user_id") int user_id, @PathVariable("address_id") int address_id, @RequestBody Address address) {
        UUID uuid = UUID.randomUUID();
        try {
            // Busca la dirección para verificar que exista
            Address existingAddress = addressService.findById(address_id).orElse(null);
            if (existingAddress == null) {
                return new ResponseEntity<>(ApiResponse.notFound(uuid), HttpStatus.NOT_FOUND);
            }
            // Verificar que la dirección pertenece al usuario
            if (existingAddress.getUser() == null || existingAddress.getUser().getId() != user_id) {
                List<String> details = List.of("No se pudo ejecutar de manera correcta su petición");
                return new ResponseEntity<>(ApiResponse.badRequest(details, uuid), HttpStatus.BAD_REQUEST);
            }
            // Actualiza los campos permitidos
            existingAddress.setName(address.getName());
            existingAddress.setStreet(address.getStreet());
            existingAddress.setCountry_code(address.getCountry_code());
            // Guarda cambios
            addressService.save(existingAddress);
            return new ResponseEntity<>(ApiResponse.ok(null, uuid), HttpStatus.OK);
        } catch (Exception ex) {
            List<String> details = List.of("No se pudo establecer conexión con el servicio");
            return new ResponseEntity<>(ApiResponse.internalServerError(details, uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
