package com.interbanking.challenge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import java.util.Date;

public class AddCompanyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);

            if (json.has("IdEmpresa") && json.has("CUIT") && json.has("RazonSocial")) {
                int idEmpresa = json.getInt("IdEmpresa");
                String cuit = json.getString("CUIT");
                String razonSocial = json.getString("RazonSocial");

                insertCompany(idEmpresa, cuit, razonSocial, exchange);
            } else {
                String response = "{\"error\":\"Es necesario enviar Body con fields (int IdEmpresa, String CUIT, String RazonSocial)\"}";
                sendResponse(exchange, response, 400);
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void insertCompany(int idEmpresa, String cuit, String razonSocial, HttpExchange exchange) throws IOException {
        try (Connection conn = DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUser(), DBConfig.getPassword())) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO empresas (IdEmpresa, CUIT, RazonSocial, FechaAdhesion) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, idEmpresa);
            stmt.setString(2, cuit);
            stmt.setString(3, razonSocial);
            stmt.setDate(4, new java.sql.Date(new Date().getTime())); 

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                String response = "{\"success\":\"Empresa agregada correctamente\"}";
                sendResponse(exchange, response, 200);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String response = "{\"error\":\"Error al insertar en la base de datos: " + e + "\"}";
            sendResponse(exchange, response, 500);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
