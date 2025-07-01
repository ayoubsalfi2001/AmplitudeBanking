package org.example.accountlist;

import org.example.accountlist.MockBankingAccountListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AccountListApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AccountListApplication.class, args);

        System.out.println("🏦 ==========================================");
        System.out.println("🏦    ACCOUNT LIST API DÉMARRÉE AVEC SUCCÈS");
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 API REST disponible sur: http://localhost:8094/api/banking/accounts");
        System.out.println("🏦");
        System.out.println("🏦 📋 ENDPOINTS DISPONIBLES:");
        System.out.println("🏦   GET    /api/banking/accounts/all              - TOUS les comptes (simple)");
        System.out.println("🏦   POST   /api/banking/accounts/search           - Recherche avec critères");
        System.out.println("🏦   GET    /api/banking/accounts/customer/{code}  - Comptes d'un client");
        System.out.println("🏦   GET    /api/banking/accounts/status           - Statut service");
        System.out.println("🏦");
        System.out.println("🏦 💡 EXEMPLES D'UTILISATION:");
        System.out.println("🏦   curl http://localhost:8094/api/banking/accounts/all");
        System.out.println("🏦   curl http://localhost:8094/api/banking/accounts/customer/00100002");
        System.out.println("🏦   curl -X POST http://localhost:8094/api/banking/accounts/search \\");
        System.out.println("🏦        -H \"Content-Type: application/json\" \\");
        System.out.println("🏦        -d '{\"customerCode\":\"00100002\",\"accountStatus\":\"O\"}'");
        System.out.println("🏦");

        // Afficher la liste des comptes au démarrage
        try {
            MockBankingAccountListService service = context.getBean(MockBankingAccountListService.class);
            service.printMockStatus();
        } catch (Exception e) {
            System.err.println("❌ Erreur affichage comptes: " + e.getMessage());
        }

        System.out.println("🏦 ==========================================");
        System.out.println("🏦       ✅ API PRÊTE POUR LES REQUÊTES");
        System.out.println("🏦 ==========================================");
    }
}