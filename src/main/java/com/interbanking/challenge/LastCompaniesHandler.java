package com.interbanking.challenge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.Headers; 

public class LastCompaniesHandler implements HttpHandler {

    static class Empresa {
        int idEmpresa;
        String cuit;
        String razonSocial;
        Date fechaAdhesion;

        public Empresa(int idEmpresa, String cuit, String razonSocial, Date fechaAdhesion) {
            this.idEmpresa = idEmpresa;
            this.cuit = cuit;
            this.razonSocial = razonSocial;
            this.fechaAdhesion = fechaAdhesion;
        }

        @Override
        public String toString() {
            return String.format("{\"IdEmpresa\": %d, \"CUIT\": \"%s\", \"RazonSocial\": \"%s\", \"FechaAdhesion\": \"%s\"}",
                    idEmpresa, cuit, razonSocial, fechaAdhesion);
        }
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Headers headers = exchange.getRequestHeaders();
            if (headers.containsKey("days")) {
                try {
                    int days = Integer.parseInt(headers.getFirst("days"));
                    List<Empresa> empresas = getLastCompanies(days);
                    String response = empresas.isEmpty() ? "[]" : empresas.toString();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (NumberFormatException e) {
                    String response = "{\"error\":\"Es necesario enviar Header days con un numero entero\"}";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                String response = "{\"error\":\"Header days es requerido\"}";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); 
        }
    }

    private static List<Empresa> getLastCompanies(int days) {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUser(), DBConfig.getPassword())) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT IdEmpresa, CUIT, RazonSocial, FechaAdhesion FROM empresas WHERE FechaAdhesion >= CURDATE() - INTERVAL ? DAY ORDER BY FechaAdhesion DESC");
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                empresas.add(new Empresa(
                        rs.getInt("IdEmpresa"),
                        rs.getString("CUIT"),
                        rs.getString("RazonSocial"),
                        rs.getDate("FechaAdhesion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empresas;
    }
}

