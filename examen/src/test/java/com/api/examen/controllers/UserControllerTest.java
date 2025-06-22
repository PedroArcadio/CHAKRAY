package com.api.examen.controllers;

import com.api.examen.entities.User;
import com.api.examen.security.Hash;
import com.api.examen.services.UserService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WebAppConfiguration
class UserControllerTest {

    private final static String URL = "/chakray/spring-boot/v1";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllUsersReturns200() throws Exception {
        // Preparar los datos
        User user = new User();
        user.setEmail("usuario@mail.com");
        user.setName("Usuario Test");
        user.setPassword(Hash.SHA1("1234"));
        userService.save(user);

        // Llamar al endpoint
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users?sortedBy=id&order=desc").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void getAllUsersReturns404WhenEmpty() throws Exception {
        // Elimina todos los usuarios para simular BD vacía
        userService.deleteAll();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    void getAllUsersReturns500Exception() throws Exception {
        // Simular error del servicio
        Mockito.when(userService.findAll()).thenThrow(new RuntimeException("Error de BD"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/users").accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    void createUserReturns201() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("email", "pedro.arcadio.dev@gmail.com");
        user.put("name", "Pedro Arcadio Dionicio");
        user.put("password", "1234"); // sin hashear, el backend lo hace
        List<Map<String, String>> addresses = new ArrayList<>();
        Map<String, String> address = new HashMap<>();
        address.put("name", "Casa");
        address.put("street", "Calle 123");
        address.put("country_code", "MX");
        addresses.add(address);
        user.put("addresses", addresses);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(user);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/users").contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    void createUserReturns500() throws Exception {
        Map<String, Object> user = new HashMap<>();
        user.put("email", "fallo@mail.com");
        user.put("name", "Fallo Prueba");
        user.put("password", "1234");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(user);

        // Simular error en el servicio
        Mockito.doThrow(new RuntimeException("Fallo interno")).when(userService).save(Mockito.any(User.class));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + "/users").contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    void patchUserReturns200() throws Exception {
        int existingUserId = 29; // Asegurar de que este usuario existe

        User updatedUser = new User();
        updatedUser.setName("Nombre actualizado desde test");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(updatedUser);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(URL + "/users/" + existingUserId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void patchUserReturns404() throws Exception {
        int nonExistentUserId = 999999;

        User updatedUser = new User();
        updatedUser.setName("Nuevo nombre");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(updatedUser);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(URL + "/users/" + nonExistentUserId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    void patchUserReturns500() throws Exception {
        int userId = 1;

        Mockito.when(userService.findById(userId)).thenThrow(new RuntimeException("Fallo simulado"));

        User updatedUser = new User();
        updatedUser.setName("Causa fallo");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(updatedUser);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(URL + "/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).content(jsonBody)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    void deleteUserReturns200() throws Exception {
        int existingUserId = 29; // Asegurar de que este usuario existe en la base de datos

        assertTrue(userService.existsById(existingUserId), "El usuario debe existir antes de eliminarse");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/users/" + existingUserId).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void deleteUserReturns400() throws Exception {
        int nonExistentUserId = 999999;

        assertFalse(userService.existsById(nonExistentUserId), "El usuario NO debe existir");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/users/" + nonExistentUserId).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void deleteUserReturns500() throws Exception {
        int anyUserId = 1;

        Mockito.when(userService.existsById(anyUserId)).thenThrow(new RuntimeException("Fallo simulado"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/users/" + anyUserId).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(500, result.getResponse().getStatus());
    }
}