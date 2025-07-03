package org.example.productsubscription;

import org.example.productsubscription.product.*;
import org.example.productsubscription.product.Module;
import org.example.productsubscription.product.Package;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service Mock SIMPLE pour les souscriptions de produits
 * Implémente SEULEMENT getProductSubscriptionList
 */
@Service
public class SimpleProductSubscriptionService implements GetProductSubscriptionListPortType {

    private final List<MockProductData> mockProducts = new ArrayList<>();

    public SimpleProductSubscriptionService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦   SIMPLE PRODUCT SERVICE ACTIVÉ");
        System.out.println("🏦   API: Liste des produits par client");
        System.out.println("🏦 ==========================================");
        initializeMockData();
    }

    @Override
    public GetProductSubscriptionListResponseFlow getProductSubscriptionList(GetProductSubscriptionListRequestFlow request) {
        System.out.println("🏦 MOCK: getProductSubscriptionList() appelé");

        try {
            // Simulation d'une latence
            Thread.sleep(200);

            GetProductSubscriptionListResponseFlow responseFlow = new GetProductSubscriptionListResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(request.getRequestHeader().getRequestId()));
            responseFlow.setResponseStatus(createSuccessStatus());

            // Extraire le numéro de client de la requête
            String customerNumber = extractCustomerNumber(request.getGetProductSubscriptionListRequest());

            // Filtrer les produits pour ce client
            List<MockProductData> customerProducts = filterProductsByCustomer(customerNumber);

            // Construire la réponse
            GetProductSubscriptionListResponse listResponse = new GetProductSubscriptionListResponse();
            for (MockProductData productData : customerProducts) {
                GetProductSubscriptionResponse subscription = buildProductResponse(productData);
                listResponse.getProductSubscription().add(subscription);
            }

            responseFlow.setGetProductSubscriptionListResponse(listResponse);

            System.out.println("✅ " + customerProducts.size() + " produits retournés pour client " + customerNumber);
            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Product Service: " + e.getMessage());
            return buildErrorResponse(request.getRequestHeader().getRequestId(), e.getMessage());
        }
    }

    @Override
    public GetStatusResponseFlow getStatus(GetStatusRequestFlow request) {
        System.out.println("🏦 MOCK: getStatus() appelé");

        GetStatusResponseFlow responseFlow = new GetStatusResponseFlow();
        GetStatusResponse statusResponse = new GetStatusResponse();

        try {
            statusResponse.setTimeStamp(createXMLTimestamp(LocalDateTime.now()));
            statusResponse.setServiceName("Simple Product Subscription Service");
            responseFlow.setGetStatusResponse(statusResponse);

        } catch (Exception e) {
            System.err.println("❌ Erreur Status: " + e.getMessage());
        }

        return responseFlow;
    }

    // ========================================
    // INITIALISATION DES DONNÉES MOCK
    // ========================================

    private void initializeMockData() {
        System.out.println("📋 Initialisation des produits clients...");

        // DIOUF ABLAYE - 3 produits
        addProduct("00100002", "DIOUF ABLAYE", "1", "PARTICULIER STANDARD", "MARIE DIALLO",
                "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT", "00100002015", "00001", "XOF",
                "1", "0000195200100002015", "2020-01-15", null);

        addProduct("00100002", "DIOUF ABLAYE", "1", "PARTICULIER STANDARD", "MARIE DIALLO",
                "101", "CARTE VISA CLASSIC", "SERVICE PRODUCT", "00100002015", "00001", "XOF",
                "1", "CARD00100002015", "2020-02-01", null);

        addProduct("00100002", "DIOUF ABLAYE", "1", "PARTICULIER STANDARD", "MARIE DIALLO",
                "002", "LIVRET EPARGNE", "ACCOUNT PRODUCT", "00100002016", "00001", "XOF",
                "1", "0000195200100002016", "2021-06-10", null);

        // MBOUP ROKHAYA - 1 produit fermé
        addProduct("00100018", "MBOUP ROKHAYA", "1", "PART A REV INDETERMINE", "MAMADOU MBAYE",
                "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT", "00100018014", "00001", "XOF",
                "9", "0000195200100018014", "2019-02-05", "2019-03-27");

        // FALL MAMADOU - 2 produits
        addProduct("00100004", "FALL MAMADOU", "1", "PARTICULIER PREMIUM", "OUSMANE SALL",
                "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT", "00100004017", "00001", "XOF",
                "1", "0000195200100004017", "2019-03-20", null);

        addProduct("00100004", "FALL MAMADOU", "1", "PARTICULIER PREMIUM", "OUSMANE SALL",
                "201", "CREDIT IMMOBILIER", "FILE PRODUCT", "00100004017", "00001", "XOF",
                "1", "CREDIT00100004017", "2021-01-10", "2041-01-10");

        // TECHCORP SARL - 2 produits
        addProduct("00100003", "TECHCORP SARL", "3", "ENTREPRISE STANDARD", "AMINATA BA",
                "004", "COMPTE PROFESSIONNEL", "ACCOUNT PRODUCT", "00100003020", "00001", "XOF",
                "1", "0000195200100003020", "2018-05-10", null);

        addProduct("00100003", "TECHCORP SARL", "3", "ENTREPRISE STANDARD", "AMINATA BA",
                "301", "TERMINAL DE PAIEMENT", "SERVICE PRODUCT", "00100003020", "00001", "XOF",
                "1", "TPE00100003020", "2018-06-15", null);

        System.out.println("✅ " + mockProducts.size() + " produits initialisés");
        System.out.println("📊 Clients disponibles:");
        mockProducts.stream()
                .map(p -> p.customerNumber + " - " + p.customerName)
                .distinct()
                .forEach(customer -> System.out.println("  👤 " + customer));
    }

    private void addProduct(String customerNumber, String customerName, String customerType,
                            String profile, String officer, String productCode, String productName,
                            String productType, String accountNumber, String branchCode,
                            String currency, String status, String reference,
                            String startDate, String endDate) {

        MockProductData product = new MockProductData();
        product.customerNumber = customerNumber;
        product.customerName = customerName;
        product.customerType = customerType;
        product.profile = profile;
        product.officer = officer;
        product.productCode = productCode;
        product.productName = productName;
        product.productType = productType;
        product.accountNumber = accountNumber;
        product.branchCode = branchCode;
        product.currency = currency;
        product.status = status;
        product.reference = reference;
        product.startDate = startDate;
        product.endDate = endDate;

        mockProducts.add(product);
    }

    // ========================================
    // FILTRAGE ET CONSTRUCTION DES RÉPONSES
    // ========================================

    private String extractCustomerNumber(GetProductSubscriptionListRequest request) {
        if (request != null && request.getCustomer() != null &&
                request.getCustomer().getCustomer() != null) {
            return request.getCustomer().getCustomer().getCustomerNumber();
        }
        return "";
    }

    private List<MockProductData> filterProductsByCustomer(String customerNumber) {
        return mockProducts.stream()
                .filter(product -> product.customerNumber.equals(customerNumber))
                .collect(java.util.stream.Collectors.toList());
    }

    private GetProductSubscriptionResponse buildProductResponse(MockProductData data) throws Exception {
        GetProductSubscriptionResponse response = new GetProductSubscriptionResponse();

        // Client
        PopulationFile customer = new PopulationFile();
        RestrictedCustomer restrictedCustomer = new RestrictedCustomer();
        restrictedCustomer.setCustomerNumber(data.customerNumber);
        restrictedCustomer.setDisplayedName(data.customerName);
        customer.setCustomer(restrictedCustomer);
        // CustomerType n'existe pas, utiliser directement String
        // customer.setCustomerType(CustomerType.fromValue(data.customerType));

        CustomerProfile profile = new CustomerProfile();
        profile.setDesignation(data.profile);
        customer.setActiveProfile(profile);

        CustomerOfficer officer = new CustomerOfficer();
        officer.setName(data.officer);
        customer.setCustomerOfficer(officer);

        response.setCustomer(customer);

        // Produit
        Product product = new Product();
        product.setCode(data.productCode);
        product.setDesignation(data.productName);
        // ProductAttribute n'existe pas, utiliser directement String
        // product.setProductAttribute(ProductAttribute.fromValue(data.productType));
        response.setProduct(product);

        // Package et Module (vides)
        response.setPackage(new Package());
        response.setFileModule(new Module());

        // Compte
        AccountFile accountFile = new AccountFile();
        AccountIdentifierOurBranch accountIdentifier = new AccountIdentifierOurBranch();
        InternalFormatAccountOurBranch internalFormat = new InternalFormatAccountOurBranch();

        Branch branch = new Branch();
        branch.setCode(data.branchCode);
        branch.setDesignation("AGENCE TEST");
        internalFormat.setBranch(branch);

        SimpleCurrency currency = new SimpleCurrency();
        currency.setAlphaCode(data.currency);
        currency.setNumericCode("952");
        currency.setDesignation("FRANC CFA BCEAO");
        internalFormat.setCurrency(currency);

        internalFormat.setAccount(data.accountNumber);
        accountIdentifier.setInternalFormatAccountOurBranch(internalFormat);
        accountFile.setAccountNumber(accountIdentifier);
        accountFile.setCustomer(customer);

        response.setAccountFile(accountFile);

        // Détails de souscription
        // ProcessingCode n'existe pas, utiliser directement String
        // response.setProcessingCode(ProcessingCode.fromValue(data.status));
        response.setReferenceSubscription(data.reference);

        if (data.startDate != null) {
            response.setStartDateSubscription(parseDate(data.startDate));
        }
        if (data.endDate != null) {
            response.setEndDateSubscription(parseDate(data.endDate));
        }

        return response;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("MOCK_PROD_" + System.currentTimeMillis());
        header.setTimestamp(createXMLTimestamp(LocalDateTime.now()));
        header.setServiceVersion("V1.0");

        Language language = new Language();
        language.setCode("001");
        language.setDesignation("Francais");
        header.setLanguage(language);

        return header;
    }

    private ResponseStatus createSuccessStatus() {
        ResponseStatus status = new ResponseStatus();
        // StatusCodeType n'existe pas, utiliser directement String
        // status.setStatusCode(StatusCodeType.fromValue("0"));
        status.setStatusCode("0");
        return status;
    }

    private GetProductSubscriptionListResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetProductSubscriptionListResponseFlow errorResponse = new GetProductSubscriptionListResponseFlow();

            ResponseHeader header = createResponseHeader(requestId);
            errorResponse.setResponseHeader(header);

            ResponseStatus status = new ResponseStatus();
            // StatusCodeType n'existe pas, utiliser directement String
            // status.setStatusCode(StatusCodeType.fromValue("-1"));
            status.setStatusCode("-1");

            errorResponse.setResponseStatus(status);
            return errorResponse;

        } catch (Exception e) {
            System.err.println("Erreur création réponse d'erreur: " + e.getMessage());
            return new GetProductSubscriptionListResponseFlow();
        }
    }

    private XMLGregorianCalendar createXMLTimestamp(LocalDateTime dateTime) throws Exception {
        GregorianCalendar gcal = GregorianCalendar.from(dateTime.atZone(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    private XMLGregorianCalendar parseDate(String dateStr) throws Exception {
        String[] parts = dateStr.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        LocalDate localDate = LocalDate.of(year, month, day);
        GregorianCalendar gcal = GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    public int getProductCount() {
        return mockProducts.size();
    }

    public Set<String> getAvailableCustomers() {
        Set<String> customers = new HashSet<>();
        mockProducts.forEach(p -> customers.add(p.customerNumber + " - " + p.customerName));
        return customers;
    }

    // CLASSE INTERNE POUR DONNÉES MOCK
    private static class MockProductData {
        String customerNumber;
        String customerName;
        String customerType;
        String profile;
        String officer;
        String productCode;
        String productName;
        String productType;
        String accountNumber;
        String branchCode;
        String currency;
        String status;
        String reference;
        String startDate;
        String endDate;
    }
}