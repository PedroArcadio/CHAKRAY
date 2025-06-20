package com.api.examen.utils;

import java.util.*;

public final class ApiResponse {

    public static Map<String, Object> internalServerError(List<String> details, UUID folio) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "500.chakray-examen.5000");
        response.put("message", "Error Interno del Servidor");
        response.put("folio", folio);
        response.put("details", details);
        return response;
    }

    public static Map<String, Object> badRequest(List<String> details, UUID folio) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "400.chakray-examen.4000");
        response.put("message", "Petición no válida, favor de validar su información");
        response.put("folio", folio);
        response.put("details", details);
        return response;
    }

    public static Map<String, Object> ok(Object data, UUID folio) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Operación Exitosa");
        response.put("folio", folio);
        if (data != null) {
            if (data.getClass().isArray()) {
                Object[] array = (Object[]) data;
                if (array.length > 0) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("records", array);
                    response.put("result", result);
                }
            } else if (data instanceof Collection) {
                Collection<?> collection = (Collection<?>) data;
                if (!collection.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("records", collection);
                    response.put("result", result);
                }
            } else {
                response.put("result", data);
            }
        }
        return response;
    }

    public static Map<String, Object> notFound(UUID folio) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "404.chakray-examen.4040");
        response.put("message", "Información no encontrada");
        response.put("folio", folio);
        List<String> details = List.of("No se encontro informacion para la combinación de parametros indicada");
        response.put("details", details);
        return response;
    }

}
