package org.example.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
        System.out.println("🏦 === Banking API démarrée ===");
        System.out.println("🏦 API REST disponible sur: http://localhost:8092/api/banking");
        System.out.println("🏦 Endpoints disponibles:");
        System.out.println("  GET    /api/banking/customer/{code}     - Détails client bancaire");
        System.out.println("  GET    /api/banking/customers           - Codes clients disponibles");
        System.out.println("  POST   /api/banking/customer/search     - Recherche avec requête complète");
        System.out.println("  GET    /api/banking/status              - Statut service");
        System.out.println("  GET    /api/banking/mock/info           - Informations Mock");
    }
}