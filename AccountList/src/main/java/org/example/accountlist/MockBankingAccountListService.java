package org.example.accountList;

import org.example.accountList.Account.Comparator;
import org.example.accountlist.GetAccountListService;
import org.example.accountList.Account.*;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.example.accountList.Account.Comparator.GREATER_EQUALS;
import static org.example.accountList.Account.Comparator.LOWER_THAN;

/**
 * Service Mock Liste des Comptes - SEULEMENT 2 méthodes du WSDL
 * Mode DIRECT - Serveur sécurisé
 */
@Service
public class MockBankingAccountListService implements GetAccountListService {

    private final Map<String, BankingAccountData> mockAccounts = new HashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(1);

    public MockBankingAccountListService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 MOCK ACCOUNT LIST SERVICE ACTIVÉ DIRECTEMENT");
        System.out.println("🏦 Méthodes: getAccountList + getStatus");
        System.out.println("🏦 Mode: DIRECT (serveur sécurisé)");
        System.out.println("🏦 ==========================================");
        initializeMockData();
    }

    @Override
    public GetAccountListResponseFlow getAccountList(GetAccountListRequestFlow request) {
        System.out.println("🏦 MOCK: getAccountList() appelé");

        try {
            simulateDelay(300);

            GetAccountListResponseFlow responseFlow = new GetAccountListResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(
                    request.getRequestHeader().getRequestId()
            ));
            responseFlow.setResponseStatus(createSuccessStatus());

            // Appliquer les filtres et retourner les comptes
            List<BankingAccountData> filteredAccounts = applyFilters(
                    request.getGetAccountListRequest()
            );

            GetAccountListResponse accountListResponse = buildAccountListResponse(filteredAccounts);
            responseFlow.setGetAccountListResponse(accountListResponse);

            System.out.println("✅ " + filteredAccounts.size() + " comptes trouvés");
            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Account List: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(
                    request.getRequestHeader().getRequestId(),
                    e.getMessage()
            );
        }
    }

    @Override
    public GetStatusResponseFlow getStatus(GetStatusRequestFlow request) {
        System.out.println("🏦 MOCK: getStatus() appelé");

        simulateDelay(150);

        GetStatusResponseFlow responseFlow = new GetStatusResponseFlow();
        GetStatusResponse statusResponse = new GetStatusResponse();

        try {
            statusResponse.setTimeStamp(createXMLTimestamp(LocalDateTime.now()));
            statusResponse.setServiceName("Mock Amplitude Account List Service");
            responseFlow.setGetStatusResponse(statusResponse);

            System.out.println("✅ Status Mock retourné");

        } catch (Exception e) {
            System.err.println("❌ Erreur Status: " + e.getMessage());
        }

        return responseFlow;
    }

    // ========================================
    // INITIALISATION DES DONNÉES
    // ========================================

    private void initializeMockData() {
        System.out.println("📋 Initialisation des comptes bancaires...");

        try {
            // Comptes pour DIOUF ABLAYE (00100002)
            addAccount("SN08SN0100010012345678901", "12345678901", "01", "00100002", "DIOUF ABLAYE",
                    "CCL", "COMPTE COURANT LIBRE", "O", 850000.0, "2020-01-15", "COMPTE PRINCIPAL DIOUF");

            addAccount("SN08SN0100010098765432109", "98765432109", "02", "00100002", "DIOUF ABLAYE",
                    "EPG", "LIVRET EPARGNE", "O", 250000.0, "2021-06-10", "LIVRET EPARGNE DIOUF");

            // Comptes pour FALL MAMADOU (00100004)
            addAccount("SN08SN0100010011111111111", "11111111111", "01", "00100004", "FALL MAMADOU",
                    "CCL", "COMPTE COURANT LIBRE", "O", 450000.0, "2019-03-20", "COMPTE COURANT FALL");

            // Comptes pour DIALLO AISSATOU (00100005)
            addAccount("SN08SN0100010022222222222", "22222222222", "01", "00100005", "DIALLO AISSATOU",
                    "CCL", "COMPTE COURANT LIBRE", "O", 320000.0, "2020-08-15", "COMPTE PRINCIPAL DIALLO");

            addAccount("SN08SN0100010033333333333", "33333333333", "02", "00100005", "DIALLO AISSATOU",
                    "EPG", "LIVRET EPARGNE", "O", 180000.0, "2021-12-01", "EPARGNE DIALLO");

            addAccount("SN08SN0100010044444444444", "44444444444", "03", "00100005", "DIALLO AISSATOU",
                    "PLA", "PLACEMENT TERME", "O", 500000.0, "2022-02-14", "PLACEMENT DIALLO");

            // Comptes pour TECHCORP SARL (00100003)
            addAccount("SN08SN0100010055555555555", "55555555555", "01", "00100003", "TECHCORP SARL",
                    "PRO", "COMPTE PROFESSIONNEL", "O", 1200000.0, "2018-05-10", "COMPTE PRINCIPAL TECHCORP");

            addAccount("SN08SN0100010066666666666", "66666666666", "02", "00100003", "TECHCORP SARL",
                    "DEV", "COMPTE DEVISES", "O", 75000.0, "2019-11-22", "COMPTE USD TECHCORP");

            // Comptes pour GLOBAL SOLUTIONS (00100006)
            addAccount("SN08SN0100010077777777777", "77777777777", "01", "00100006", "GLOBAL SOLUTIONS",
                    "PRO", "COMPTE PROFESSIONNEL", "O", 2500000.0, "2017-01-30", "COMPTE PRINCIPAL GLOBAL");

            addAccount("SN08SN0100010088888888888", "88888888888", "02", "00100006", "GLOBAL SOLUTIONS",
                    "INV", "COMPTE INVESTISSEMENT", "O", 800000.0, "2020-04-05", "INVESTISSEMENTS GLOBAL");

            // Quelques comptes fermés pour la diversité
            addAccount("SN08SN0100010099999999999", "99999999999", "04", "00100002", "DIOUF ABLAYE",
                    "CCL", "COMPTE COURANT LIBRE", "F", 0.0, "2018-01-10", "ANCIEN COMPTE DIOUF (FERMÉ)");

            System.out.println("✅ " + mockAccounts.size() + " comptes bancaires initialisés");
            mockAccounts.forEach((iban, data) ->
                    System.out.println("  📝 " + iban + ": " + data.accountTitle)
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation: " + e.getMessage());
        }
    }

    private void addAccount(String iban, String accountNumber, String suffix, String customerCode,
                            String customerName, String accountTypeCode, String accountTypeDesc,
                            String status, Double balance, String openingDate, String title) {
        BankingAccountData account = new BankingAccountData();
        account.iban = iban;
        account.accountNumber = accountNumber;
        account.suffix = suffix;
        account.customerCode = customerCode;
        account.customerName = customerName;
        account.accountTypeCode = accountTypeCode;
        account.accountTypeDesc = accountTypeDesc;
        account.status = status;
        account.balance = balance;
        account.openingDate = openingDate;
        account.accountTitle = title;

        mockAccounts.put(iban, account);
    }

    // ========================================
    // FILTRAGE DES COMPTES
    // ========================================

    private List<BankingAccountData> applyFilters(GetAccountListRequest request) {
        List<BankingAccountData> result = new ArrayList<>();

        for (BankingAccountData account : mockAccounts.values()) {
            boolean matches = true;

            // Filtre par client
            if (request.getAccount() != null &&
                    request.getAccount().getCustomer() != null &&
                    request.getAccount().getCustomer().getCustomer() != null &&
                    request.getAccount().getCustomer().getCustomer().getCustomerNumber() != null) {

                String filterCustomer = request.getAccount().getCustomer().getCustomer().getCustomerNumber();
                if (!account.customerCode.equals(filterCustomer)) {
                    matches = false;
                }
            }

            // Filtre par statut
            if (request.getAccountStatus() != null) {
                AccountStatus filterStatus = request.getAccountStatus();
                if (!account.status.equals(filterStatus.value())) {
                    matches = false;
                }
            }

            // Filtre par type de compte
            if (request.getAccountType() != null && request.getAccountType().getCode() != null) {
                String filterType = request.getAccountType().getCode();
                if (!account.accountTypeCode.equals(filterType)) {
                    matches = false;
                }
            }

            // Filtre par solde
            if (request.getBalance() != null) {
                ComparisonAmount balanceFilter = request.getBalance();
                if (!matchesBalanceFilter(account.balance, balanceFilter)) {
                    matches = false;
                }
            }

            if (matches) {
                result.add(account);
            }
        }

        return result;
    }

    private boolean matchesBalanceFilter(Double accountBalance, ComparisonAmount filter) {
        if (filter.getAmount1() == null) return true;

        double filterAmount1 = filter.getAmount1().doubleValue();
        Comparator comparator = filter.getComparator();

        switch (comparator) {
            case EQUALS:
                return accountBalance.equals(filterAmount1);
            case GREATER_THAN:
                return accountBalance > filterAmount1;
            case GREATER_EQUALS:
                return accountBalance >= filterAmount1;
            case LOWER_THAN:
                return accountBalance < filterAmount1;
            case LOWER_EQUALS:
                return accountBalance <= filterAmount1;
            case BETWEEN:
                if (filter.getAmount2() != null) {
                    double filterAmount2 = filter.getAmount2().doubleValue();
                    return accountBalance > filterAmount1 && accountBalance < filterAmount2;
                }
                return true;
            case BETWEEN_EQUALS:
                if (filter.getAmount2() != null) {
                    double filterAmount2 = filter.getAmount2().doubleValue();
                    return accountBalance >= filterAmount1 && accountBalance <= filterAmount2;
                }
                return true;
            default:
                return true;
        }
    }

    // ========================================
    // CONSTRUCTION DES RÉPONSES
    // ========================================

    private GetAccountListResponse buildAccountListResponse(List<BankingAccountData> accounts) throws Exception {
        GetAccountListResponse response = new GetAccountListResponse();

        for (BankingAccountData accountData : accounts) {
            GetAccountResponse accountResponse = buildAccountResponse(accountData);
            response.getAccount().add(accountResponse);
        }

        return response;
    }

    private GetAccountResponse buildAccountResponse(BankingAccountData data) throws Exception {
        GetAccountResponse response = new GetAccountResponse();

        // Catégorie - CORRECTION: Utiliser l'enum AccountCategory
        response.setAccountCategory(AccountCategory.C); // C = Compte

        // Agence
        Branch branch = new Branch();
        branch.setCode("00001");
        branch.setDesignation("AGENCE PRINCIPALE");
        response.setBranch(branch);

        // Informations du compte
        AccountFile accountFile = new AccountFile();

        // Numéro de compte
        AccountIdentifierOurBranch accountId = new AccountIdentifierOurBranch();
        InternalFormatAccountOurBranch internalFormat = new InternalFormatAccountOurBranch();

        internalFormat.setBranch(branch);

        SimpleCurrency currency = new SimpleCurrency();
        currency.setAlphaCode("XOF");
        currency.setNumericCode("952");
        currency.setDesignation("FRANC CFA");
        internalFormat.setCurrency(currency);

        internalFormat.setAccount(data.accountNumber);
        internalFormat.setSuffix(data.suffix);
        accountId.setInternalFormatAccountOurBranch(internalFormat);

        IbanFormatAccount ibanFormat = new IbanFormatAccount();
        ibanFormat.setValue(data.iban);
        accountId.setIbanFormatAccount(ibanFormat);

        accountFile.setAccountNumber(accountId);

        // Client
        PopulationFile popFile = new PopulationFile();
        RestrictedCustomer customer = new RestrictedCustomer();
        customer.setCustomerNumber(data.customerCode);
        customer.setDisplayedName(data.customerName);
        popFile.setCustomer(customer);

        // Type client - CORRECTION: Utiliser les valeurs string directement
        if (data.customerCode.equals("00100003") || data.customerCode.equals("00100006")) {
            popFile.setCustomerType("2"); // Entreprise
        } else {
            popFile.setCustomerType("1"); // Particulier
        }

        // Profil client
        CustomerProfile profile = new CustomerProfile();
        profile.setCode("101");
        profile.setDesignation("PART A REV INDETERMINE");
        popFile.setActiveProfile(profile);

        // Chargé de clientèle
        CustomerOfficer officer = new CustomerOfficer();
        officer.setCode("002");
        officer.setName("FAYE MARIANE");
        popFile.setCustomerOfficer(officer);

        accountFile.setCustomer(popFile);
        response.setAccount(accountFile);

        // Type de compte
        AccountType accountType = new AccountType();
        accountType.setCode(data.accountTypeCode);
        accountType.setDesignation(data.accountTypeDesc);
        response.setAccountType(accountType);

        // Produit
        Product product = new Product();
        product.setCode(data.accountTypeCode);
        product.setDesignation(data.accountTypeDesc);
        product.setProductAttribute(ProductAttribute.ACCOUNT_PRODUCT);
        response.setProduct(product);

        // Statut
        response.setAccountStatus(AccountStatus.fromValue(data.status));

        // Titre du compte
        Designation accountTitle = new Designation();
        accountTitle.setValue(data.accountTitle);
        accountTitle.setComparisonOperator(ComparisonOperator.EQUALS);
        response.setAccountTitle(accountTitle);

        // Solde indicatif
        response.setIndicativeBalance(BigDecimal.valueOf(data.balance));

        // Dates
        response.setOpeningDate(parseDate(data.openingDate));
        response.setDateLocation(parseDate("2025-07-01"));

        // Clé de contrôle
        response.setCheckKey(String.format("%02d", (int)(Math.random() * 100)));
        response.setCheckDigitDeclared(response.getCheckKey());

        return response;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("MOCK_ACC_" + requestCounter.getAndIncrement());
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
        // CORRECTION: Utiliser String directement car StatusCodeType n'existe pas
        status.setStatusCode("0"); // 0 = Succès
        return status;
    }

    private GetAccountListResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetAccountListResponseFlow errorResponse = new GetAccountListResponseFlow();

            ResponseHeader header = createResponseHeader(requestId);
            errorResponse.setResponseHeader(header);

            ResponseStatus status = new ResponseStatus();
            // CORRECTION: Utiliser String directement
            status.setStatusCode("-1"); // -1 = Erreur

            ResponseStatusMessages messages = new ResponseStatusMessages();
            ResponseStatusMessage message = new ResponseStatusMessage();
            message.setNature(ResponseMessageNature.ERROR);
            message.setCode("ERR001");
            message.getLine().add(errorMessage);
            messages.getMessage().add(message);
            status.setMessages(messages);

            errorResponse.setResponseStatus(status);

            return errorResponse;

        } catch (Exception e) {
            System.err.println("Erreur création réponse d'erreur: " + e.getMessage());
            return new GetAccountListResponseFlow();
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

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, 0, 0);
        return createXMLTimestamp(dateTime);
    }

    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // MÉTHODES PUBLIQUES POUR MONITORING
    public int getMockAccountsCount() {
        return mockAccounts.size();
    }

    public Set<String> getAvailableAccountNumbers() {
        return new HashSet<>(mockAccounts.keySet());
    }

    public void printMockStatus() {
        System.out.println("🏦 ACCOUNT LIST MOCK STATUS: " + mockAccounts.size() + " comptes");
        mockAccounts.forEach((iban, data) ->
                System.out.println("  - " + iban + ": " + data.accountTitle + " (" + data.customerName + ")")
        );
    }

    // CLASSE INTERNE POUR DONNÉES
    private static class BankingAccountData {
        String iban;
        String accountNumber;
        String suffix;
        String customerCode;
        String customerName;
        String accountTypeCode;
        String accountTypeDesc;
        String status;
        Double balance;
        String openingDate;
        String accountTitle;
    }
}