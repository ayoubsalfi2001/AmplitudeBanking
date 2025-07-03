package org.example.productsubscription;

import org.example.productsubscription.product.*;
import org.example.productsubscription.product.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST SIMPLE pour liste des produits client
 * UNE SEULE API: Récupérer les produits d'un client
 */
@RestController
@RequestMapping("/api/banking/product-subscriptions")
@CrossOrigin(origins = "*")
public class ProductSubscriptionController {

    @Autowired
    private SimpleProductSubscriptionService productService;

    /**
     * API UNIQUE: GET /api/banking/product-subscriptions/customer/{customerNumber}
     * Récupère tous les produits souscrits par un client
     */
    @GetMapping("/customer/{customerNumber}")
    public ResponseEntity<?> getCustomerProducts(@PathVariable String customerNumber) {
        System.out.println("🏦 === GET /customer/" + customerNumber + " ===");

        try {
            GetProductSubscriptionListRequestFlow soapRequest = buildCustomerRequest(customerNumber);
            GetProductSubscriptionListResponseFlow soapResponse = productService.getProductSubscriptionList(soapRequest);

            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Client non trouvé: " + customerNumber));
            }

            Map<String, Object> result = convertToSimpleResponse(soapResponse);

            System.out.println("✅ Produits du client " + customerNumber + " retournés");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération produits: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur récupération produits: " + e.getMessage()));
        }
    }

    // ========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================

    /**
     * Construire une requête SOAP pour un client
     */
    private GetProductSubscriptionListRequestFlow buildCustomerRequest(String customerNumber) throws Exception {
        GetProductSubscriptionListRequestFlow request = new GetProductSubscriptionListRequestFlow();

        // Header
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("CUST_" + System.currentTimeMillis());
        requestHeader.setServiceName("getProductSubscriptionList");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("API_USER");
        request.setRequestHeader(requestHeader);

        // Critère de recherche par client
        GetProductSubscriptionListRequest listRequest = new GetProductSubscriptionListRequest();
        PopulationFile customer = new PopulationFile();
        RestrictedCustomer restrictedCustomer = new RestrictedCustomer();
        restrictedCustomer.setCustomerNumber(customerNumber);
        customer.setCustomer(restrictedCustomer);
        listRequest.setCustomer(customer);

        request.setGetProductSubscriptionListRequest(listRequest);
        return request;
    }

    /**
     * Convertir la réponse SOAP en format JSON simple
     */
    private Map<String, Object> convertToSimpleResponse(GetProductSubscriptionListResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            GetProductSubscriptionListResponse listResponse = soapResponse.getGetProductSubscriptionListResponse();

            if (listResponse != null && listResponse.getProductSubscription() != null &&
                    !listResponse.getProductSubscription().isEmpty()) {

                List<Map<String, Object>> products = new ArrayList<>();
                Map<String, Object> customerInfo = null;

                for (GetProductSubscriptionResponse subscription : listResponse.getProductSubscription()) {
                    // Informations client (une seule fois)
                    if (customerInfo == null && subscription.getCustomer() != null &&
                            subscription.getCustomer().getCustomer() != null) {
                        customerInfo = new HashMap<>();
                        customerInfo.put("customerNumber", subscription.getCustomer().getCustomer().getCustomerNumber());
                        customerInfo.put("customerName", subscription.getCustomer().getCustomer().getDisplayedName());
                        customerInfo.put("customerType", subscription.getCustomer().getCustomerType());

                        if (subscription.getCustomer().getActiveProfile() != null) {
                            customerInfo.put("profile", subscription.getCustomer().getActiveProfile().getDesignation());
                        }

                        if (subscription.getCustomer().getCustomerOfficer() != null) {
                            customerInfo.put("officer", subscription.getCustomer().getCustomerOfficer().getName());
                        }
                    }

                    // Produit souscrit
                    Map<String, Object> product = new HashMap<>();

                    if (subscription.getProduct() != null) {
                        product.put("productCode", subscription.getProduct().getCode());
                        product.put("productName", subscription.getProduct().getDesignation());
                        product.put("productType", subscription.getProduct().getProductAttribute());
                    }

                    // Compte associé
                    if (subscription.getAccountFile() != null &&
                            subscription.getAccountFile().getAccountNumber() != null &&
                            subscription.getAccountFile().getAccountNumber().getInternalFormatAccountOurBranch() != null) {

                        InternalFormatAccountOurBranch account = subscription.getAccountFile().getAccountNumber().getInternalFormatAccountOurBranch();
                        product.put("accountNumber", account.getAccount());

                        if (account.getBranch() != null) {
                            product.put("branchCode", account.getBranch().getCode());
                        }

                        if (account.getCurrency() != null) {
                            product.put("currency", account.getCurrency().getAlphaCode());
                        }
                    }

                    // Statut et dates
                    product.put("status", getProductStatus(subscription.getProcessingCode()));
                    product.put("startDate", subscription.getStartDateSubscription() != null ?
                            subscription.getStartDateSubscription().toString() : null);
                    product.put("endDate", subscription.getEndDateSubscription() != null ?
                            subscription.getEndDateSubscription().toString() : null);
                    product.put("reference", subscription.getReferenceSubscription());

                    products.add(product);
                }

                response.put("customer", customerInfo);
                response.put("products", products);
                response.put("totalProducts", products.size());
            } else {
                response.put("customer", null);
                response.put("products", new ArrayList<>());
                response.put("totalProducts", 0);
            }

            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "success");

        } catch (Exception e) {
            System.err.println("Erreur conversion réponse: " + e.getMessage());
            response.put("error", "Erreur lors de la conversion");
        }

        return response;
    }

    /**
     * Convertir le code de traitement en statut lisible
     */
    private String getProductStatus(String processingCode) {
        if (processingCode == null) return "UNKNOWN";

        switch (processingCode) {
            case "1": return "ACTIVE";
            case "9": return "CLOSED";
            case "2": return "SUSPENDED";
            default: return "UNKNOWN";
        }
    }

    /**
     * Créer une réponse d'erreur
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", "error");
        return error;
    }
}