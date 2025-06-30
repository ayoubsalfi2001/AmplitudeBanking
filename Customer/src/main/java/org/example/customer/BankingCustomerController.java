package org.example.customer;// ========================================


import org.example.customer.client.*;
import org.example.customer.MockBankingCustomerService;
import org.example.customer.client.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST pour service bancaire
 */
@RestController
@RequestMapping("/api/banking")
@CrossOrigin(origins = "*")
public class BankingCustomerController {

    @Autowired
    private MockBankingCustomerService bankingService;

    /**
     * GET /api/banking/customer/{customerCode} - Récupérer UN client
     */
    @GetMapping("/customer/{customerCode}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable String customerCode) {
        System.out.println("🏦 === GET /api/banking/customer/" + customerCode + " ===");

        try {
            GetCustomerDetailRequestFlow soapRequest = buildSoapRequest(customerCode);
            GetCustomerDetailResponseFlow soapResponse = bankingService.getCustomerDetail(soapRequest);

            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Client non trouvé: " + customerCode));
            }

            Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);

            System.out.println("✅ Client bancaire récupéré: " + customerCode);
            return ResponseEntity.ok(restResponse);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération client: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse("Erreur serveur: " + e.getMessage()));
        }
    }

    /**
     * POST /api/banking/customer/search - Recherche avec requête complète
     */
    @PostMapping("/customer/search")
    public ResponseEntity<?> searchCustomerWithFullRequest(@RequestBody Map<String, Object> requestData) {
        System.out.println("🏦 === POST /api/banking/customer/search ===");
        System.out.println("Données reçues: " + requestData);

        try {
            String customerCode = (String) requestData.get("customerCode");
            if (customerCode == null || customerCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Le code client est obligatoire"));
            }

            GetCustomerDetailRequestFlow soapRequest = buildDetailedSoapRequest(customerCode, requestData);
            GetCustomerDetailResponseFlow soapResponse = bankingService.getCustomerDetail(soapRequest);

            boolean fullResponse = Boolean.TRUE.equals(requestData.get("fullResponse"));

            if (fullResponse) {
                return ResponseEntity.ok(soapResponse);
            } else {
                Map<String, Object> restResponse = convertToSimpleResponse(soapResponse);
                return ResponseEntity.ok(restResponse);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche client: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur recherche: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/customers - Lister les codes clients disponibles
     */
    @GetMapping("/customers")
    public ResponseEntity<?> getAvailableCustomers() {
        System.out.println("🏦 === GET /api/banking/customers ===");

        try {
            Set<String> customerCodes = bankingService.getAvailableCustomerCodes();

            Map<String, Object> response = new HashMap<>();
            response.put("totalCustomers", customerCodes.size());
            response.put("customerCodes", customerCodes);
            response.put("description", "Codes clients disponibles dans le Mock bancaire");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ " + customerCodes.size() + " codes clients disponibles");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur listing clients: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur listing: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/status - Statut du service bancaire
     */
    @GetMapping("/status")
    public ResponseEntity<?> getBankingStatus() {
        System.out.println("🏦 === GET /api/banking/status ===");

        try {
            GetStatusRequestFlow statusRequest = new GetStatusRequestFlow();
            statusRequest.setGetStatusRequest("status_check");

            GetStatusResponseFlow statusResponse = bankingService.getStatus(statusRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", statusResponse.getGetStatusResponse().getServiceName());
            response.put("timestamp", statusResponse.getGetStatusResponse().getTimeStamp());
            response.put("mode", "MOCK_DIRECT");
            response.put("description", "Service bancaire Mock Sopra - Mode sécurisé");
            response.put("totalMockCustomers", bankingService.getMockCustomersCount());
            response.put("springBootVersion", "3.2.0");
            response.put("javaVersion", "17");
            response.put("available", true);

            System.out.println("✅ Statut service bancaire retourné");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur statut service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur statut: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/mock/info - Informations détaillées du Mock
     */
    @GetMapping("/mock/info")
    public ResponseEntity<Map<String, Object>> getMockInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("application", "Amplitude Banking Mock API");
        info.put("description", "API REST Mock pour service bancaire Sopra Amplitude sécurisé");
        info.put("version", "1.0.0");
        info.put("mode", "Mock Banking Service Direct");
        info.put("technology", Map.of(
                "springBoot", "3.2.0",
                "java", "17",
                "cxf", "4.0.5",
                "jakarta", "EE 9+"
        ));
        info.put("features", new String[]{
                "✅ Récupération détails client bancaire (getCustomerDetail)",
                "✅ Vérification statut service (getStatus)",
                "✅ Données réalistes conformes XML Sopra",
                "✅ Support particuliers et entreprises",
                "✅ Gestion complète des erreurs",
                "✅ Simulation délais réseau bancaire"
        });

        info.put("totalMockCustomers", bankingService.getMockCustomersCount());
        info.put("availableCustomers", bankingService.getAvailableCustomerCodes());

        bankingService.printMockStatus();

        return ResponseEntity.ok(info);
    }

    // ========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================

    private GetCustomerDetailRequestFlow buildSoapRequest(String customerCode) throws Exception {
        GetCustomerDetailRequestFlow request = new GetCustomerDetailRequestFlow();

        // Header de requête
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("REST_" + System.currentTimeMillis());
        requestHeader.setServiceName("getCustomerDetail");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("REPRISE");
        request.setRequestHeader(requestHeader);

        // Requête client
        GetCustomerDetailRequest customerRequest = new GetCustomerDetailRequest();
        CustomerIdentifier identifier = new CustomerIdentifier();
        identifier.setCustomerCode(customerCode);
        customerRequest.setCustomerIdentifier(identifier);
        request.setGetCustomerDetailRequest(customerRequest);

        return request;
    }

    private GetCustomerDetailRequestFlow buildDetailedSoapRequest(String customerCode, Map<String, Object> requestData) throws Exception {
        GetCustomerDetailRequestFlow request = new GetCustomerDetailRequestFlow();

        // Header détaillé
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId((String) requestData.getOrDefault("requestId", "REST_" + System.currentTimeMillis()));
        requestHeader.setServiceName("getCustomerDetail");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode((String) requestData.getOrDefault("userCode", "REPRISE"));

        if (requestData.containsKey("languageCode")) {
            requestHeader.setLanguageCode((String) requestData.get("languageCode"));
        }

        request.setRequestHeader(requestHeader);

        // Requête client
        GetCustomerDetailRequest customerRequest = new GetCustomerDetailRequest();
        CustomerIdentifier identifier = new CustomerIdentifier();
        identifier.setCustomerCode(customerCode);
        customerRequest.setCustomerIdentifier(identifier);
        request.setGetCustomerDetailRequest(customerRequest);

        return request;
    }

    private Map<String, Object> convertToSimpleResponse(GetCustomerDetailResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            GetCustomerDetailResponse customerDetail = soapResponse.getGetCustomerDetailResponse();

            // Informations de base
            response.put("customerCode", customerDetail.getCustomerCode());
            response.put("customerType", customerDetail.getCustomerType());
            response.put("lastname", customerDetail.getLastname());
            response.put("nameToReturn", customerDetail.getNameToReturn());

            // Titre
            if (customerDetail.getTitleCode() != null) {
                Map<String, String> title = new HashMap<>();
                title.put("code", customerDetail.getTitleCode().getCode());
                title.put("designation", customerDetail.getTitleCode().getDesignation());
                response.put("title", title);
            }

            // Nationalité
            if (customerDetail.getSituation() != null && customerDetail.getSituation().getNationalityCode() != null) {
                Map<String, String> nationality = new HashMap<>();
                nationality.put("code", customerDetail.getSituation().getNationalityCode().getCode());
                nationality.put("designation", customerDetail.getSituation().getNationalityCode().getDesignation());
                response.put("nationality", nationality);
            }

            // Informations individuelles
            if (customerDetail.getSpecificInformation() != null &&
                    customerDetail.getSpecificInformation().getIndividualSpecInfo() != null) {

                CustomerIndividualSpecInfo individualInfo = customerDetail.getSpecificInformation().getIndividualSpecInfo();

                // Prénom
                if (individualInfo.getIndividualGeneralInfo() != null) {
                    response.put("firstname", individualInfo.getIndividualGeneralInfo().getFirstname());

                    if (individualInfo.getIndividualGeneralInfo().getFamilyStatusCode() != null) {
                        Map<String, String> familyStatus = new HashMap<>();
                        familyStatus.put("code", individualInfo.getIndividualGeneralInfo().getFamilyStatusCode().getCode());
                        familyStatus.put("designation", individualInfo.getIndividualGeneralInfo().getFamilyStatusCode().getDesignation());
                        response.put("familyStatus", familyStatus);
                    }
                }

                // Naissance
                if (individualInfo.getBirth() != null) {
                    Map<String, Object> birth = new HashMap<>();
                    birth.put("sex", individualInfo.getBirth().getHolderSex());
                    birth.put("birthDate", individualInfo.getBirth().getBirthDate());
                    birth.put("birthCity", individualInfo.getBirth().getBirthCity());
                    response.put("birth", birth);
                }

                // Papiers d'identité
                if (individualInfo.getIdPaper() != null) {
                    Map<String, Object> idPaper = new HashMap<>();
                    idPaper.put("number", individualInfo.getIdPaper().getIdPaperNumber());
                    idPaper.put("deliveryDate", individualInfo.getIdPaper().getIdPaperDeliveryDate());
                    idPaper.put("validityDate", individualInfo.getIdPaper().getIdPaperValidityDate());
                    if (individualInfo.getIdPaper().getType() != null) {
                        idPaper.put("type", individualInfo.getIdPaper().getType().getDesignation());
                    }
                    response.put("idPaper", idPaper);
                }
            }

            // Attributs généraux
            if (customerDetail.getGeneralAttributes() != null) {
                Map<String, Object> generalAttributes = new HashMap<>();

                if (customerDetail.getGeneralAttributes().getBranchCode() != null) {
                    Map<String, String> branch = new HashMap<>();
                    branch.put("code", customerDetail.getGeneralAttributes().getBranchCode().getCode());
                    branch.put("designation", customerDetail.getGeneralAttributes().getBranchCode().getDesignation());
                    generalAttributes.put("branch", branch);
                }

                if (customerDetail.getGeneralAttributes().getCustomerOfficer() != null) {
                    Map<String, String> officer = new HashMap<>();
                    officer.put("code", customerDetail.getGeneralAttributes().getCustomerOfficer().getCode());
                    officer.put("name", customerDetail.getGeneralAttributes().getCustomerOfficer().getName());
                    generalAttributes.put("customerOfficer", officer);
                }

                generalAttributes.put("taxableCustomer", customerDetail.getGeneralAttributes().isTaxableCustomer());
                response.put("generalAttributes", generalAttributes);
            }

            // Téléphones
            if (customerDetail.getPhoneNumbers() != null &&
                    !customerDetail.getPhoneNumbers().getCustomerPhoneNumber().isEmpty()) {

                List<Map<String, String>> phones = new ArrayList<>();
                for (CustomerPhoneNumber phone : customerDetail.getPhoneNumbers().getCustomerPhoneNumber()) {
                    Map<String, String> phoneMap = new HashMap<>();
                    phoneMap.put("number", phone.getPhoneNumber());
                    if (phone.getPhoneType() != null) {
                        phoneMap.put("type", phone.getPhoneType().getDesignation());
                    }
                    phones.add(phoneMap);
                }
                response.put("phoneNumbers", phones);
            }

            // Adresses
            if (customerDetail.getAddressesDetail() != null &&
                    !customerDetail.getAddressesDetail().getCustomerAddressDetail().isEmpty()) {

                List<Map<String, Object>> addresses = new ArrayList<>();
                for (CustomerAddressDetail address : customerDetail.getAddressesDetail().getCustomerAddressDetail()) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("line1", address.getAddressLine1());
                    addressMap.put("line2", address.getAddressLine2());
                    addressMap.put("city", address.getCity());
                    addressMap.put("postalCode", address.getPostalCode());
                    addressMap.put("county", address.getCounty());
                    addressMap.put("region", address.getRegion());
                    addresses.add(addressMap);
                }
                response.put("addresses", addresses);
            }

            // Profil actif
            if (customerDetail.getActiveProfile() != null &&
                    customerDetail.getActiveProfile().getActiveProfile() != null) {
                Map<String, String> profile = new HashMap<>();
                profile.put("code", customerDetail.getActiveProfile().getActiveProfile().getCode());
                profile.put("designation", customerDetail.getActiveProfile().getActiveProfile().getDesignation());
                response.put("activeProfile", profile);
            }

            // Métadonnées de réponse
            response.put("responseId", soapResponse.getResponseHeader().getResponseId());
            response.put("timestamp", soapResponse.getResponseHeader().getTimestamp());
            response.put("serviceVersion", soapResponse.getResponseHeader().getServiceVersion());
            response.put("statusCode", soapResponse.getResponseStatus().getStatusCode());

        } catch (Exception e) {
            System.err.println("Erreur conversion réponse: " + e.getMessage());
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