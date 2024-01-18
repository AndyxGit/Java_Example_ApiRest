package com.interbanking.challenge;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/lasttransfer", new LastTransferHandler());
        server.createContext("/lastcompanies", new LastCompaniesHandler());
        server.createContext("/addcompany", new AddCompanyHandler());
        
        server.setExecutor(null);
        server.start();
    }
}
