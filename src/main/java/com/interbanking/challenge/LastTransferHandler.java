package com.interbanking.challenge;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.Headers; 

public class LastTransferHandler implements HttpHandler {

    static class Transferencia {
        double importe;
        int idEmpresa;
        String cuentaDebito;
        String cuentaCredito;
        Date fecha;
        String cuit;
        String razonSocial;

        public Transferencia(double importe, int idEmpresa, String cuentaDebito, String cuentaCredito, Date fecha, String cuit, String razonSocial) {
            this.importe = importe;
            this.idEmpresa = idEmpresa;
            this.cuentaDebito = cuentaDebito;
            this.cuentaCredito = cuentaCredito;
            this.fecha = fecha;
            this.cuit = cuit;
            this.razonSocial = razonSocial;
        }

        @Override
        public String toString() {
            return String.format("{\"Importe\": %f, \"IdEmpresa\": %d, \"CuentaDebito\": \"%s\", \"CuentaCredito\": \"%s\", \"Fecha\": \"%s\", \"CUIT\": \"%s\", \"RazonSocial\": \"%s\"}",
                    importe, idEmpresa, cuentaDebito, cuentaCredito, fecha, cuit, razonSocial);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Headers headers = exchange.getRequestHeaders();
            if (headers.containsKey("count")) {
                try {
                    int count = Integer.parseInt(headers.getFirst("count"));
                    List<Transferencia> transferencias = getLastTransfers(count);
                    String response = transferencias.isEmpty() ? "[]" : transferencias.toString();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (NumberFormatException e) {
                    String response = "{\"error\":\"Es necesario enviar Header count con un numero entero\"}";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                String response = "{\"error\":\"Header count es requerido\"}";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
        }
    }

    private static List<Transferencia> getLastTransfers(int count) {
        List<Transferencia> transferencias = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUser(), DBConfig.getPassword())) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT Importe, IdEmpresa, CuentaDebito, CuentaCredito, Fecha FROM transferencias ORDER BY Fecha DESC LIMIT ?");
            stmt.setInt(1, count);
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                int idEmpresa = rs.getInt("IdEmpresa");
                Transferencia transferencia = new Transferencia(
                        rs.getDouble("Importe"), idEmpresa, rs.getString("CuentaDebito"),
                        rs.getString("CuentaCredito"), rs.getDate("Fecha"), "", "");
    
                PreparedStatement stmtEmpresa = conn.prepareStatement(
                        "SELECT CUIT, RazonSocial FROM empresas WHERE IdEmpresa = ?");
                stmtEmpresa.setInt(1, idEmpresa);
                ResultSet rsEmpresa = stmtEmpresa.executeQuery();
                if (rsEmpresa.next()) {
                    transferencia.cuit = rsEmpresa.getString("CUIT");
                    transferencia.razonSocial = rsEmpresa.getString("RazonSocial");
                }
    
                transferencias.add(transferencia);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transferencias;
    }
}
