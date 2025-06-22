package com.api.examen.controllers;

import com.api.examen.services.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WebAppConfiguration
class AddressControllerTest {

    private final static String URL = "/chakray/spring-boot/v1";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAddressesByUserId200() throws Exception {
        int existingUserId = 45; // Asegurar que este usuario tenga direcciones en la BD

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users/" + existingUserId + "/addresses").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void getAddressesByUserId404() throws Exception {
        int userWithoutAddresses = 99; // Este ID debe existir pero no tener direcciones

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users/" + userWithoutAddresses + "/addresses").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    void getAddressesByUserId500() throws Exception {
        int anyUserId = 45;

        // Simula excepción en el servicio
        Mockito.when(addressService.findAllAddressById(anyUserId)).thenThrow(new RuntimeException("Falla simulada"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users/" + anyUserId + "/addresses").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    void updateAddressByUserId200() throws Exception {
        int userId = 45; // Asegúrate que este usuario y dirección existan en la BD
        int addressId = 37;

        Map<String, Object> updatedAddress = new HashMap<>();
        updatedAddress.put("name", "Nueva Dirección");
        updatedAddress.put("street", "Calle Actualizada");
        updatedAddress.put("country_code", "MX");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(updatedAddress);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "/users/" + userId + "/addresses/" + addressId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void updateAddressByUserId404() throws Exception {
        int userId = 45;
        int nonExistentAddressId = 9999; // Este ID no debe existir

        Map<String, Object> updatedAddress = Map.of(
                "name", "Test",
                "street", "No existe",
                "country_code", "MX"
        );

        String jsonBody = new ObjectMapper().writeValueAsString(updatedAddress);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "/users/" + userId + "/addresses/" + nonExistentAddressId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    void updateAddressByUserId400() throws Exception {
        int wrongUserId = 45; // Este usuario no es dueño de la dirección
        int addressId = 37;   // Esta dirección pertenece a otro usuario

        Map<String, Object> updatedAddress = Map.of(
                "name", "Dirección equivocada",
                "street", "Calle X",
                "country_code", "MX"
        );

        String jsonBody = new ObjectMapper().writeValueAsString(updatedAddress);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "/users/" + wrongUserId + "/addresses/" + addressId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void updateAddressByUserId500() throws Exception {
        int userId = 45;
        int addressId = 37;

        Map<String, Object> updatedAddress = Map.of(
                "name", "Test",
                "street", "Falla simulada",
                "country_code", "MX"
        );

        String jsonBody = new ObjectMapper().writeValueAsString(updatedAddress);

        // Simula que el servicio lanza una excepción
        Mockito.when(addressService.findById(addressId)).thenThrow(new RuntimeException("Error simulado"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "/users/" + userId + "/addresses/" + addressId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }
}