package org.example.amortizable;

import org.example.amortizable.credit.*;
import org.example.amortizable.credit.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST pour service de prêts amortissables - VERSION SIMPLIFIÉE
 */
@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class AmortizableLoanController {

    @Autowired
    private MockAmortizableLoanService loanService;

    /**
     * GET /api/loans/customer/{customerNumber} - Récupérer les prêts d'un client
     */
    @GetMapping("/customer/{customerNumber}")
    public ResponseEntity<?> getCustomerLoans(@PathVariable String customerNumber) {
        System.out.println("🏦 === GET /api/loans/customer/" + customerNumber + " ===");

        try {
            GetAmortizableLoanListRequestFlow soapRequest = buildSoapRequest(customerNumber);
            GetAmortizableLoanListResponseFlow soapResponse = loanService.getAmortizableLoanList(soapRequest);

            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Prêts non trouvés pour client: " + customerNumber));
            }

            Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);

            System.out.println("✅ Prêts récupérés pour client: " + customerNumber);
            return ResponseEntity.ok(restResponse);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération prêts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur serveur: " + e.getMessage()));
        }
    }

    /**
     * POST /api/loans/search - Recherche avec critères avancés
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchLoansWithCriteria(@RequestBody Map<String, Object> requestData) {
        System.out.println("🏦 === POST /api/loans/search ===");
        System.out.println("Critères reçus: " + requestData);

        try {
            GetAmortizableLoanListRequestFlow soapRequest = buildDetailedSoapRequest(requestData);
            GetAmortizableLoanListResponseFlow soapResponse = loanService.getAmortizableLoanList(soapRequest);

            boolean fullResponse = Boolean.TRUE.equals(requestData.get("fullResponse"));

            if (fullResponse) {
                return ResponseEntity.ok(soapResponse);
            } else {
                Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);
                return ResponseEntity.ok(restResponse);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche prêts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur recherche: " + e.getMessage()));
        }
    }

    /**
     * GET /api/loans/all - Lister tous les prêts disponibles
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllLoans() {
        System.out.println("🏦 === GET /api/loans/all ===");

        try {
            List<Map<String, Object>> allLoans = loanService.getAllMockLoans();

            Map<String, Object> response = new HashMap<>();
            response.put("totalLoans", allLoans.size());
            response.put("loans", allLoans);
            response.put("description", "Tous les prêts amortissables disponibles");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ " + allLoans.size() + " prêts retournés");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur listing prêts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur listing: " + e.getMessage()));
        }
    }

    /**
     * GET /api/loans/status - Statut du service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        System.out.println("🏦 === GET /api/loans/status ===");

        try {
            GetStatusRequestFlow statusRequest = new GetStatusRequestFlow();
            statusRequest.setGetStatusRequest("status_check");

            GetStatusResponseFlow statusResponse = loanService.getStatus(statusRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", statusResponse.getGetStatusResponse().getServiceName());
            response.put("timestamp", statusResponse.getGetStatusResponse().getTimeStamp());
            response.put("mode", "MOCK_LOANS");
            response.put("description", "Service de prêts amortissables Mock");
            response.put("totalMockLoans", loanService.getMockLoansCount());
            response.put("available", true);

            System.out.println("✅ Statut service retourné");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur statut service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur statut: " + e.getMessage()));
        }
    }

    /**
     * GET /api/loans/mock/info - Informations détaillées du Mock
     */
    @GetMapping("/mock/info")
    public ResponseEntity<Map<String, Object>> getMockInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("application", "Amplitude Loan Mock API");
        info.put("description", "API REST Mock pour service de prêts amortissables Sopra");
        info.put("version", "1.0.0");
        info.put("mode", "Mock Amortizable Loans Service");
        info.put("technology", Map.of(
                "springBoot", "3.2.0",
                "java", "17",
                "cxf", "4.0.5"
        ));
        info.put("features", new String[]{
                "✅ Récupération liste prêts amortissables",
                "✅ Recherche par critères multiples",
                "✅ Support filtres avancés",
                "✅ Données conformes WSDL Sopra",
                "✅ Gestion complète des erreurs"
        });

        info.put("totalMockLoans", loanService.getMockLoansCount());
        info.put("availableCustomers", loanService.getAvailableCustomers());

        return ResponseEntity.ok(info);
    }

    // ========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================

    private GetAmortizableLoanListRequestFlow buildSoapRequest(String customerNumber) throws Exception {
        GetAmortizableLoanListRequestFlow request = new GetAmortizableLoanListRequestFlow();

        // Header de requête
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("ATMF_" + System.currentTimeMillis());
        requestHeader.setServiceName("getAmortizableLoanList");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("IDOUBBA");
        request.setRequestHeader(requestHeader);

        // Requête prêts
        GetAmortizableLoanListRequest loanRequest = new GetAmortizableLoanListRequest();

        // Client
        PopulationFile populationFile = new PopulationFile();
        RestrictedCustomer customer = new RestrictedCustomer();
        customer.setCustomerNumber(customerNumber);
        populationFile.setCustomer(customer);
        loanRequest.setCustomer(populationFile);

        request.setGetAmortizableLoanListRequest(loanRequest);

        return request;
    }

    private GetAmortizableLoanListRequestFlow buildDetailedSoapRequest(Map<String, Object> requestData) throws Exception {
        GetAmortizableLoanListRequestFlow request = new GetAmortizableLoanListRequestFlow();

        // Header détaillé
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId((String) requestData.getOrDefault("requestId", "ATMF_" + System.currentTimeMillis()));
        requestHeader.setServiceName("getAmortizableLoanList");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode((String) requestData.getOrDefault("userCode", "IDOUBBA"));
        request.setRequestHeader(requestHeader);

        // Requête avec critères
        GetAmortizableLoanListRequest loanRequest = new GetAmortizableLoanListRequest();

        // Client
        if (requestData.containsKey("customerNumber")) {
            PopulationFile populationFile = new PopulationFile();
            RestrictedCustomer customer = new RestrictedCustomer();
            customer.setCustomerNumber((String) requestData.get("customerNumber"));
            populationFile.setCustomer(customer);
            loanRequest.setCustomer(populationFile);
        }

        // Type de prêt
        if (requestData.containsKey("loanTypeCode")) {
            AmortizableLoanType loanType = new AmortizableLoanType();
            loanType.setCode((String) requestData.get("loanTypeCode"));
            loanRequest.setLoanType(loanType);
        }

        // État
        if (requestData.containsKey("state")) {
            String state = (String) requestData.get("state");
            loanRequest.setState(AmortizableLoanFileStatus.fromValue(state));
        }

        request.setGetAmortizableLoanListRequest(loanRequest);

        return request;
    }

    private Map<String, Object> convertToSimpleResponse(GetAmortizableLoanListResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            GetAmortizableLoanListResponse loanListResponse = soapResponse.getGetAmortizableLoanListResponse();

            if (loanListResponse != null && !loanListResponse.getAmortizableLoan().isEmpty()) {
                List<Map<String, Object>> loans = new ArrayList<>();

                for (GetAmortizableLoanResponse loan : loanListResponse.getAmortizableLoan()) {
                    Map<String, Object> loanMap = new HashMap<>();

                    // Identifiant crédit
                    if (loan.getCreditIdentifier() != null) {
                        Map<String, Object> creditId = new HashMap<>();
                        creditId.put("fileNumber", loan.getCreditIdentifier().getFileNumber());
                        creditId.put("orderNumber", loan.getCreditIdentifier().getOrderNumber());
                        creditId.put("amendmentNumber", loan.getCreditIdentifier().getAmendmentNumber());

                        if (loan.getCreditIdentifier().getBranch() != null) {
                            Map<String, String> branch = new HashMap<>();
                            branch.put("code", loan.getCreditIdentifier().getBranch().getCode());
                            branch.put("designation", loan.getCreditIdentifier().getBranch().getDesignation());
                            creditId.put("branch", branch);
                        }
                        loanMap.put("creditIdentifier", creditId);
                    }

                    // Devise
                    if (loan.getCurrency() != null) {
                        Map<String, String> currency = new HashMap<>();
                        currency.put("alphaCode", loan.getCurrency().getAlphaCode());
                        currency.put("numericCode", loan.getCurrency().getNumericCode());
                        currency.put("designation", loan.getCurrency().getDesignation());
                        loanMap.put("currency", currency);
                    }

                    // Client
                    if (loan.getCustomer() != null) {
                        Map<String, Object> customer = new HashMap<>();
                        if (loan.getCustomer().getCustomer() != null) {
                            customer.put("customerNumber", loan.getCustomer().getCustomer().getCustomerNumber());
                            customer.put("displayedName", loan.getCustomer().getCustomer().getDisplayedName());
                        }
                        customer.put("customerType", loan.getCustomer().getCustomerType());

                        if (loan.getCustomer().getActiveProfile() != null) {
                            Map<String, String> profile = new HashMap<>();
                            profile.put("code", loan.getCustomer().getActiveProfile().getCode());
                            profile.put("designation", loan.getCustomer().getActiveProfile().getDesignation());
                            customer.put("activeProfile", profile);
                        }

                        if (loan.getCustomer().getCustomerOfficer() != null) {
                            Map<String, String> officer = new HashMap<>();
                            officer.put("code", loan.getCustomer().getCustomerOfficer().getCode());
                            officer.put("name", loan.getCustomer().getCustomerOfficer().getName());
                            customer.put("customerOfficer", officer);
                        }

                        loanMap.put("customer", customer);
                    }

                    // Type de prêt
                    if (loan.getLoanType() != null) {
                        Map<String, String> loanType = new HashMap<>();
                        loanType.put("code", loan.getLoanType().getCode());
                        loanType.put("label", loan.getLoanType().getLabel());
                        loanMap.put("loanType", loanType);
                    }

                    // Dates
                    if (loan.getLastInstallmentDate() != null) {
                        Map<String, Object> lastInstallment = new HashMap<>();
                        lastInstallment.put("date", loan.getLastInstallmentDate().getDate1());
                        lastInstallment.put("comparator", loan.getLastInstallmentDate().getComparator());
                        loanMap.put("lastInstallmentDate", lastInstallment);
                    }

                    if (loan.getEstablishmentDate() != null) {
                        Map<String, Object> establishment = new HashMap<>();
                        establishment.put("date", loan.getEstablishmentDate().getDate1());
                        establishment.put("comparator", loan.getEstablishmentDate().getComparator());
                        loanMap.put("establishmentDate", establishment);
                    }

                    // SOLUTION SIMPLE: Utiliser toString() pour tous les objets
                    loanMap.put("state", loan.getState() != null ? loan.getState().toString() : null);
                    loanMap.put("installmentPeriods", loan.getInstallmentPeriods() != null ? loan.getInstallmentPeriods().toString() : null);

                    // Durée
                    if (loan.getMonthsDuration() != null) {
                        Map<String, Object> duration = new HashMap<>();
                        duration.put("months", loan.getMonthsDuration().getNumber1());
                        duration.put("comparator", loan.getMonthsDuration().getComparator());
                        loanMap.put("monthsDuration", duration);
                    }

                    // Montant
                    if (loan.getAmount() != null) {
                        Map<String, Object> amount = new HashMap<>();
                        amount.put("amount", loan.getAmount().getAmount1());
                        amount.put("comparator", loan.getAmount().getComparator());
                        loanMap.put("amount", amount);
                    }

                    // Capital restant dû
                    if (loan.getOutstandingCapital() != null) {
                        Map<String, Object> outstanding = new HashMap<>();
                        outstanding.put("amount", loan.getOutstandingCapital().getAmount());
                        loanMap.put("outstandingCapital", outstanding);
                    }

                    // Taux de crédit
                    if (loan.getCreditRate() != null) {
                        Map<String, Object> rate = new HashMap<>();
                        rate.put("interestRate", loan.getCreditRate().getInterestRate());
                        rate.put("teg", loan.getCreditRate().getTeg());
                        loanMap.put("creditRate", rate);
                    }

                    // Autres informations
                    loanMap.put("numberRemainingInstallments", loan.getNumberRemainingInstallments());

                    // Montant impayé
                    if (loan.getUnpaidAmount() != null) {
                        Map<String, Object> unpaid = new HashMap<>();
                        unpaid.put("amount", loan.getUnpaidAmount().getAmount());
                        loanMap.put("unpaidAmount", unpaid);
                    }

                    loans.add(loanMap);
                }

                response.put("loans", loans);
                response.put("totalLoans", loans.size());
            } else {
                response.put("loans", new ArrayList<>());
                response.put("totalLoans", 0);
            }

            // Métadonnées de réponse
            response.put("responseId", soapResponse.getResponseHeader().getResponseId());
            response.put("timestamp", soapResponse.getResponseHeader().getTimestamp());
            response.put("serviceVersion", soapResponse.getResponseHeader().getServiceVersion());
            response.put("statusCode", soapResponse.getResponseStatus().getStatusCode());

        } catch (Exception e) {
            System.err.println("Erreur conversion réponse: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Erreur lors de la conversion de la réponse");
        }

        return response;
    }

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", "error");
        return error;
    }
}