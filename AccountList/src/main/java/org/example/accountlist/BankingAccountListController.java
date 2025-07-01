package org.example.accountlist;

import org.example.accountList.Account.*;
import org.example.accountList.Account.RequestHeader;
import org.example.accountlist.MockBankingAccountListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST pour service de liste des comptes
 */
@RestController
@RequestMapping("/api/banking/accounts")
@CrossOrigin(origins = "*")
public class BankingAccountListController {

    @Autowired
    private MockBankingAccountListService accountListService;

    /**
     * GET /api/banking/accounts/all - Récupérer TOUS les comptes (approche simple)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAccounts() {
        System.out.println("🏦 === GET /api/banking/accounts/all ===");

        try {
            List<Map<String, Object>> allAccounts = generateMockAccounts();

            Map<String, Object> response = new HashMap<>();
            response.put("totalAccounts", allAccounts.size());
            response.put("accounts", allAccounts);
            response.put("timestamp", System.currentTimeMillis());
            response.put("description", "Liste complète des comptes bancaires pour filtrage frontend");
            response.put("note", "Filtrage et recherche à effectuer côté frontend");

            System.out.println("✅ " + allAccounts.size() + " comptes retournés pour filtrage frontend");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération comptes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur récupération comptes: " + e.getMessage()));
        }
    }

    /**
     * POST /api/banking/accounts/search - Recherche avec critères (utilise getAccountList)
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchAccounts(@RequestBody Map<String, Object> searchCriteria) {
        System.out.println("🏦 === POST /api/banking/accounts/search ===");
        System.out.println("Critères reçus: " + searchCriteria);

        try {
            GetAccountListRequestFlow soapRequest = buildAccountListRequest(searchCriteria);
            GetAccountListResponseFlow soapResponse = accountListService.getAccountList(soapRequest);

            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode().toString())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Erreur recherche: " +
                                soapResponse.getResponseStatus().getStatusCode().toString()));
            }

            boolean fullResponse = Boolean.TRUE.equals(searchCriteria.get("fullResponse"));

            if (fullResponse) {
                return ResponseEntity.ok(soapResponse);
            } else {
                Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);
                return ResponseEntity.ok(restResponse);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche comptes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur recherche: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/accounts/customer/{customerCode} - Comptes d'un client
     */
    @GetMapping("/customer/{customerCode}")
    public ResponseEntity<?> getAccountsByCustomer(@PathVariable String customerCode) {
        System.out.println("🏦 === GET /api/banking/accounts/customer/" + customerCode + " ===");

        try {
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("customerCode", customerCode);

            GetAccountListRequestFlow soapRequest = buildAccountListRequest(searchCriteria);
            GetAccountListResponseFlow soapResponse = accountListService.getAccountList(soapRequest);

            Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);

            System.out.println("✅ Comptes du client " + customerCode + " récupérés");
            return ResponseEntity.ok(restResponse);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération comptes client: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur récupération comptes client: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/accounts/status - Statut du service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAccountListStatus() {
        System.out.println("🏦 === GET /api/banking/accounts/status ===");

        try {
            GetStatusRequestFlow statusRequest = new GetStatusRequestFlow();
            statusRequest.setGetStatusRequest("status_check");

            GetStatusResponseFlow statusResponse = accountListService.getStatus(statusRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", statusResponse.getGetStatusResponse().getServiceName());
            response.put("timestamp", statusResponse.getGetStatusResponse().getTimeStamp());
            response.put("mode", "MOCK_DIRECT");
            response.put("description", "Service de liste des comptes Mock Sopra");
            response.put("totalMockAccounts", accountListService.getMockAccountsCount());
            response.put("available", true);

            System.out.println("✅ Statut service comptes retourné");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur statut service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur statut: " + e.getMessage()));
        }
    }

    // ========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================

    private GetAccountListRequestFlow buildAccountListRequest(Map<String, Object> criteria) throws Exception {
        GetAccountListRequestFlow request = new GetAccountListRequestFlow();

        // Header de requête
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("REST_ACC_" + System.currentTimeMillis());
        requestHeader.setServiceName("getAccountList");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("REPRISE");
        request.setRequestHeader(requestHeader);

        // Critères de recherche
        GetAccountListRequest accountRequest = new GetAccountListRequest();

        // Filtre par client
        if (criteria.containsKey("customerCode")) {
            AccountFile accountFile = new AccountFile();
            PopulationFile popFile = new PopulationFile();
            RestrictedCustomer customer = new RestrictedCustomer();
            customer.setCustomerNumber((String) criteria.get("customerCode"));
            popFile.setCustomer(customer);
            accountFile.setCustomer(popFile);
            accountRequest.setAccount(accountFile);
        }

        // Filtre par statut
        if (criteria.containsKey("accountStatus")) {
            String status = (String) criteria.get("accountStatus");
            accountRequest.setAccountStatus(AccountStatus.fromValue(status));
        }

        // Filtre par type
        if (criteria.containsKey("accountType")) {
            AccountType accountType = new AccountType();
            accountType.setCode((String) criteria.get("accountType"));
            accountRequest.setAccountType(accountType);
        }

        request.setGetAccountListRequest(accountRequest);
        return request;
    }

    private Map<String, Object> convertToSimpleResponse(GetAccountListResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            GetAccountListResponse accountListResponse = soapResponse.getGetAccountListResponse();
            List<Map<String, Object>> accounts = new ArrayList<>();

            if (accountListResponse != null && accountListResponse.getAccount() != null) {
                for (GetAccountResponse accountResp : accountListResponse.getAccount()) {
                    Map<String, Object> accountMap = new HashMap<>();

                    // Informations de base
                    if (accountResp.getAccount() != null &&
                            accountResp.getAccount().getAccountNumber() != null) {

                        if (accountResp.getAccount().getAccountNumber().getInternalFormatAccountOurBranch() != null) {
                            accountMap.put("accountNumber",
                                    accountResp.getAccount().getAccountNumber()
                                            .getInternalFormatAccountOurBranch().getAccount());
                            accountMap.put("suffix",
                                    accountResp.getAccount().getAccountNumber()
                                            .getInternalFormatAccountOurBranch().getSuffix());
                        }

                        if (accountResp.getAccount().getAccountNumber().getIbanFormatAccount() != null) {
                            accountMap.put("iban",
                                    accountResp.getAccount().getAccountNumber()
                                            .getIbanFormatAccount().getValue());
                        }
                    }

                    // Client
                    if (accountResp.getAccount() != null &&
                            accountResp.getAccount().getCustomer() != null &&
                            accountResp.getAccount().getCustomer().getCustomer() != null) {
                        accountMap.put("customerCode",
                                accountResp.getAccount().getCustomer().getCustomer().getCustomerNumber());
                        accountMap.put("customerName",
                                accountResp.getAccount().getCustomer().getCustomer().getDisplayedName());
                        accountMap.put("customerType",
                                accountResp.getAccount().getCustomer().getCustomerType().toString());
                    }

                    // Type de compte
                    if (accountResp.getAccountType() != null) {
                        Map<String, String> accountType = new HashMap<>();
                        accountType.put("code", accountResp.getAccountType().getCode());
                        accountType.put("designation", accountResp.getAccountType().getDesignation());
                        accountMap.put("accountType", accountType);
                    }

                    // Produit
                    if (accountResp.getProduct() != null) {
                        Map<String, String> product = new HashMap<>();
                        product.put("code", accountResp.getProduct().getCode());
                        product.put("designation", accountResp.getProduct().getDesignation());
                        product.put("attribute", accountResp.getProduct().getProductAttribute().toString());
                        accountMap.put("product", product);
                    }

                    // Agence
                    if (accountResp.getBranch() != null) {
                        Map<String, String> branch = new HashMap<>();
                        branch.put("code", accountResp.getBranch().getCode());
                        branch.put("designation", accountResp.getBranch().getDesignation());
                        accountMap.put("branch", branch);
                    }

                    // Statut et soldes
                    if (accountResp.getAccountStatus() != null) {
                        accountMap.put("status", accountResp.getAccountStatus().toString());
                        accountMap.put("statusDescription", getStatusDescription(accountResp.getAccountStatus().toString()));
                    }

                    if (accountResp.getIndicativeBalance() != null) {
                        accountMap.put("balance", accountResp.getIndicativeBalance().doubleValue());
                    }

                    // Titre du compte
                    if (accountResp.getAccountTitle() != null) {
                        accountMap.put("title", accountResp.getAccountTitle().getValue());
                    }

                    // Dates
                    if (accountResp.getOpeningDate() != null) {
                        accountMap.put("openingDate", accountResp.getOpeningDate().toString());
                    }

                    if (accountResp.getCheckKey() != null) {
                        accountMap.put("checkKey", accountResp.getCheckKey());
                    }

                    // Devise
                    Map<String, String> currency = new HashMap<>();
                    currency.put("code", "XOF");
                    currency.put("designation", "FRANC CFA");
                    accountMap.put("currency", currency);

                    accounts.add(accountMap);
                }
            }

            response.put("totalAccounts", accounts.size());
            response.put("accounts", accounts);
            response.put("responseId", soapResponse.getResponseHeader().getResponseId());
            response.put("timestamp", soapResponse.getResponseHeader().getTimestamp());
            response.put("serviceVersion", soapResponse.getResponseHeader().getServiceVersion());
            response.put("statusCode", soapResponse.getResponseStatus().getStatusCode().toString());

        } catch (Exception e) {
            System.err.println("Erreur conversion réponse: " + e.getMessage());
            response.put("error", "Erreur lors de la conversion de la réponse");
        }

        return response;
    }

    /**
     * Génération des données Mock des comptes (pour endpoint /all simple)
     */
    private List<Map<String, Object>> generateMockAccounts() {
        List<Map<String, Object>> accounts = new ArrayList<>();

        // Comptes pour DIOUF ABLAYE (00100002)
        accounts.add(createAccountMap(
                "SN08SN0100010012345678901", "12345678901", "01", "00100002", "DIOUF ABLAYE",
                "CCL", "COMPTE COURANT LIBRE", "O", 850000.0, "2020-01-15", "COMPTE PRINCIPAL DIOUF"
        ));
        accounts.add(createAccountMap(
                "SN08SN0100010098765432109", "98765432109", "02", "00100002", "DIOUF ABLAYE",
                "EPG", "LIVRET EPARGNE", "O", 250000.0, "2021-06-10", "LIVRET EPARGNE DIOUF"
        ));

        // Comptes pour FALL MAMADOU (00100004)
        accounts.add(createAccountMap(
                "SN08SN0100010011111111111", "11111111111", "01", "00100004", "FALL MAMADOU",
                "CCL", "COMPTE COURANT LIBRE", "O", 450000.0, "2019-03-20", "COMPTE COURANT FALL"
        ));

        // Comptes pour DIALLO AISSATOU (00100005)
        accounts.add(createAccountMap(
                "SN08SN0100010022222222222", "22222222222", "01", "00100005", "DIALLO AISSATOU",
                "CCL", "COMPTE COURANT LIBRE", "O", 320000.0, "2020-08-15", "COMPTE PRINCIPAL DIALLO"
        ));
        accounts.add(createAccountMap(
                "SN08SN0100010033333333333", "33333333333", "02", "00100005", "DIALLO AISSATOU",
                "EPG", "LIVRET EPARGNE", "O", 180000.0, "2021-12-01", "EPARGNE DIALLO"
        ));
        accounts.add(createAccountMap(
                "SN08SN0100010044444444444", "44444444444", "03", "00100005", "DIALLO AISSATOU",
                "PLA", "PLACEMENT TERME", "O", 500000.0, "2022-02-14", "PLACEMENT DIALLO"
        ));

        // Comptes pour TECHCORP SARL (00100003)
        accounts.add(createAccountMap(
                "SN08SN0100010055555555555", "55555555555", "01", "00100003", "TECHCORP SARL",
                "PRO", "COMPTE PROFESSIONNEL", "O", 1200000.0, "2018-05-10", "COMPTE PRINCIPAL TECHCORP"
        ));
        accounts.add(createAccountMap(
                "SN08SN0100010066666666666", "66666666666", "02", "00100003", "TECHCORP SARL",
                "DEV", "COMPTE DEVISES", "O", 75000.0, "2019-11-22", "COMPTE USD TECHCORP"
        ));

        // Comptes pour GLOBAL SOLUTIONS (00100006)
        accounts.add(createAccountMap(
                "SN08SN0100010077777777777", "77777777777", "01", "00100006", "GLOBAL SOLUTIONS",
                "PRO", "COMPTE PROFESSIONNEL", "O", 2500000.0, "2017-01-30", "COMPTE PRINCIPAL GLOBAL"
        ));
        accounts.add(createAccountMap(
                "SN08SN0100010088888888888", "88888888888", "02", "00100006", "GLOBAL SOLUTIONS",
                "INV", "COMPTE INVESTISSEMENT", "O", 800000.0, "2020-04-05", "INVESTISSEMENTS GLOBAL"
        ));

        // Comptes fermés
        accounts.add(createAccountMap(
                "SN08SN0100010099999999999", "99999999999", "04", "00100002", "DIOUF ABLAYE",
                "CCL", "COMPTE COURANT LIBRE", "F", 0.0, "2018-01-10", "ANCIEN COMPTE DIOUF (FERMÉ)"
        ));

        return accounts;
    }

    /**
     * Créer un objet compte Mock pour endpoint simple
     */
    private Map<String, Object> createAccountMap(String iban, String accountNumber, String suffix,
                                                 String customerCode, String customerName,
                                                 String accountTypeCode, String accountTypeDesc,
                                                 String status, Double balance, String openingDate, String title) {
        Map<String, Object> account = new HashMap<>();

        // Identifiants
        account.put("iban", iban);
        account.put("accountNumber", accountNumber);
        account.put("suffix", suffix);
        account.put("customerCode", customerCode);
        account.put("customerName", customerName);

        // Type et produit
        Map<String, String> accountType = new HashMap<>();
        accountType.put("code", accountTypeCode);
        accountType.put("designation", accountTypeDesc);
        account.put("accountType", accountType);

        Map<String, String> product = new HashMap<>();
        product.put("code", accountTypeCode);
        product.put("designation", accountTypeDesc);
        product.put("attribute", "ACCOUNT PRODUCT");
        account.put("product", product);

        // Agence
        Map<String, String> branch = new HashMap<>();
        branch.put("code", "00001");
        branch.put("designation", "AGENCE PRINCIPALE");
        account.put("branch", branch);

        // Devise
        Map<String, String> currency = new HashMap<>();
        currency.put("code", "XOF");
        currency.put("designation", "FRANC CFA");
        account.put("currency", currency);

        // Informations financières
        account.put("balance", balance);
        account.put("status", status);
        account.put("statusDescription", getStatusDescription(status));

        // Dates
        account.put("openingDate", openingDate);
        account.put("lastUpdate", "2025-07-01");

        // Autres informations
        account.put("title", title);
        account.put("checkKey", generateCheckKey());

        // Type client
        if (customerCode.equals("00100003") || customerCode.equals("00100006")) {
            account.put("customerType", "2");
            account.put("customerTypeDescription", "Entreprise");
        } else {
            account.put("customerType", "1");
            account.put("customerTypeDescription", "Particulier");
        }

        // Chargé de clientèle
        Map<String, String> officer = new HashMap<>();
        officer.put("code", "002");
        officer.put("name", "FAYE MARIANE");
        account.put("customerOfficer", officer);

        return account;
    }

    private String getStatusDescription(String status) {
        switch (status) {
            case "O": return "Ouvert";
            case "F": return "Fermé";
            case "I": return "Inactif";
            default: return "Inconnu";
        }
    }

    private String generateCheckKey() {
        return String.format("%02d", (int)(Math.random() * 100));
    }

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", "error");
        return error;
    }
}