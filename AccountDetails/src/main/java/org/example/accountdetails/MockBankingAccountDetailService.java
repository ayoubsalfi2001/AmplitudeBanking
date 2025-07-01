package org.example.accountdetails;

import org.example.accountdetails.Account.*;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service Mock Détail des Comptes - Compatible avec structure XML SOAP
 * Mode DIRECT - Serveur sécurisé
 */
@Service
public class MockBankingAccountDetailService implements GetAccountDetailService {

    private final Map<String, BankingAccountDetailData> mockAccountDetails = new HashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(1);

    public MockBankingAccountDetailService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 MOCK ACCOUNT DETAIL SERVICE ACTIVÉ DIRECTEMENT");
        System.out.println("🏦 Méthodes: getAccountDetail + getStatus");
        System.out.println("🏦 Mode: DIRECT (serveur sécurisé) - Structure XML Compatible");
        System.out.println("🏦 ==========================================");
        initializeAccountDetailMockData();
    }

    @Override
    public GetAccountDetailResponseFlow getAccountDetail(GetAccountDetailRequestFlow request) {
        System.out.println("🏦 MOCK: getAccountDetail() appelé");

        try {
            simulateDelay(400);

            GetAccountDetailResponseFlow responseFlow = new GetAccountDetailResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(
                    request.getRequestHeader().getRequestId()
            ));
            responseFlow.setResponseStatus(createSuccessStatus());

            // Extraire l'identifiant du compte de la requête
            String accountKey = extractAccountKey(request.getGetAccountDetailRequest());

            // Chercher les détails du compte
            BankingAccountDetailData accountDetail = mockAccountDetails.get(accountKey);

            if (accountDetail != null) {
                GetAccountDetailResponse detailResponse = buildAccountDetailResponse(accountDetail);
                responseFlow.setGetAccountDetailResponse(detailResponse);
                System.out.println("✅ Détails du compte " + accountKey + " retournés");
            } else {
                System.out.println("❌ Compte " + accountKey + " non trouvé");
                return buildErrorResponse(
                        request.getRequestHeader().getRequestId(),
                        "Compte non trouvé: " + accountKey
                );
            }

            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Account Detail: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(
                    request.getRequestHeader().getRequestId(),
                    e.getMessage()
            );
        }
    }

    @Override
    public GetStatusResponseFlow getStatus(GetStatusRequestFlow request) {
        System.out.println("🏦 MOCK: getStatus() appelé (AccountDetail)");

        simulateDelay(150);

        GetStatusResponseFlow responseFlow = new GetStatusResponseFlow();
        GetStatusResponse statusResponse = new GetStatusResponse();

        try {
            statusResponse.setTimeStamp(createXMLTimestamp(LocalDateTime.now()));
            statusResponse.setServiceName("Mock Amplitude Account Detail Service");
            responseFlow.setGetStatusResponse(statusResponse);

            System.out.println("✅ Status Mock AccountDetail retourné");

        } catch (Exception e) {
            System.err.println("❌ Erreur Status: " + e.getMessage());
        }

        return responseFlow;
    }

    // ========================================
    // INITIALISATION DES DONNÉES MOCK
    // ========================================

    private void initializeAccountDetailMockData() {
        System.out.println("📋 Initialisation des détails de comptes...");

        try {
            // Compte principal identique à l'exemple XML fourni
            addAccountDetail("00001-952-00100002014", "00001", "952", "00100002014", "",
                    "00100002", "DIOUF ABLAYE", "Compte ATLANTIC de M. DIOUF AB",
                    "26121110", "COMPTES ATLANTIC", "00", "0002", "COMPTABILITE",
                    "C", "0", "200", "Comptes clients N soumis arret",
                    "O", "D", "M", true, false, false, 0, 0, true,
                    "2019-02-04", "2019-08-26", 0, 0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT",
                    "REPRISE", "REPRISE", 9, "001", "Francais",
                    "M", "N", "M", "", 0.0, "", "",
                    "N", "", "", "N", false, "", "");

            // Comptes pour DIOUF ABLAYE actifs
            addAccountDetail("00001-952-00100002015", "00001", "952", "00100002015", "01",
                    "00100002", "DIOUF ABLAYE", "Compte ATLANTIC Principal de M. DIOUF A",
                    "26121110", "COMPTES ATLANTIC", "12", "0002", "COMPTABILITE",
                    "C", "0", "200", "Comptes clients N soumis arret",
                    "O", "D", "M", true, false, false, 0, 0, false,
                    "2020-01-15", null, 0, 0,
                    850000.0, 850000.0, 850000.0, 850000.0, 850000.0, 0.0, 0.0, 0.0, 0.0, 120000.0, 95000.0,
                    "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT",
                    "REPRISE", "REPRISE", 9, "001", "Francais",
                    "M", "N", "M", "", 0.0, "", "",
                    "N", "", "", "N", false, "", "");

            addAccountDetail("00001-952-00100002016", "00001", "952", "00100002016", "02",
                    "00100002", "DIOUF ABLAYE", "Livret EPARGNE de M. DIOUF A",
                    "26131120", "LIVRETS EPARGNE", "34", "0002", "COMPTABILITE",
                    "C", "0", "300", "Livrets epargne particuliers",
                    "O", "D", "M", true, false, false, 0, 0, false,
                    "2021-06-10", null, 0, 0,
                    250000.0, 250000.0, 250000.0, 250000.0, 250000.0, 0.0, 0.0, 0.0, 0.0, 45000.0, 12000.0,
                    "002", "LIVRET EPARGNE", "ACCOUNT PRODUCT",
                    "REPRISE", "REPRISE", 9, "001", "Francais",
                    "T", "N", "T", "", 0.0, "", "",
                    "N", "", "", "N", false, "", "");

            // Comptes pour FALL MAMADOU
            addAccountDetail("00001-952-00100004017", "00001", "952", "00100004017", "01",
                    "00100004", "FALL MAMADOU", "Compte ATLANTIC de M. FALL M",
                    "26121110", "COMPTES ATLANTIC", "56", "0002", "COMPTABILITE",
                    "C", "0", "200", "Comptes clients N soumis arret",
                    "O", "D", "M", true, false, false, 0, 0, false,
                    "2019-03-20", null, 0, 0,
                    450000.0, 450000.0, 450000.0, 450000.0, 450000.0, 0.0, 0.0, 0.0, 0.0, 75000.0, 65000.0,
                    "001", "COMPTE ATLANTIC", "ACCOUNT PRODUCT",
                    "REPRISE", "REPRISE", 9, "001", "Francais",
                    "M", "N", "M", "", 0.0, "", "",
                    "N", "", "", "N", false, "", "");

            // Comptes pour TECHCORP SARL (entreprise)
            addAccountDetail("00001-952-00100003020", "00001", "952", "00100003020", "01",
                    "00100003", "TECHCORP SARL", "Compte PROFESSIONNEL de TECHCORP SARL",
                    "26141130", "COMPTES PROFESSIONNELS", "42", "0003", "ENTREPRISES",
                    "C", "0", "400", "Comptes professionnels",
                    "O", "D", "M", true, false, false, 50000, 10, false,
                    "2018-05-10", null, 0, 0,
                    1200000.0, 1200000.0, 1200000.0, 1200000.0, 1150000.0, 0.0, 0.0, 0.0, 0.0, 320000.0, 280000.0,
                    "004", "COMPTE PROFESSIONNEL", "ACCOUNT PRODUCT",
                    "ENTERPRISE", "ENTERPRISE", 5, "001", "Francais",
                    "M", "N", "M", "", 50000.0, "", "",
                    "N", "", "", "N", false, "", "");

            System.out.println("✅ " + mockAccountDetails.size() + " comptes détaillés initialisés");
            mockAccountDetails.forEach((key, data) ->
                    System.out.println("  📝 " + key + ": " + data.accountDesignation + " - " + data.customerName)
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation détails: " + e.getMessage());
        }
    }

    private void addAccountDetail(String key, String branchCode, String currencyCode, String accountNumber, String suffix,
                                  String customerNumber, String customerName, String accountDesignation,
                                  String accountClassCode, String accountClassDesignation, String accountKey,
                                  String serviceCode, String serviceDesignation, String accountSide, String matchingCode,
                                  String accountTypeCode, String accountTypeDesignation, String subjectToInterest,
                                  String interestLadderCode, String statementCode, boolean taxable, boolean notToBePurged,
                                  boolean pendingClosure, int directCreditCeiling, int chequeThreshold, boolean closed,
                                  String openingDate, String closureDate, int modificationSheetNumber, int deductionAtSource,
                                  Double accountingBalance, Double valueDateBalance, Double historyBalance,
                                  Double interestCalculationBalance, Double indicativeBalance, Double unavailableNoDirectCredit,
                                  Double unavailableDirectCredit, Double dailyUnavailableNoDirectCredit,
                                  Double dailyUnavailableDirectCredit, Double debitTurnovers, Double creditTurnovers,
                                  String productCode, String productDesignation, String productAttribute,
                                  String userInitiatedCode, String userInitiatedName, int forcingLevel,
                                  String languageCode, String languageDesignation, String debitFreq, String transferDebtRecovery,
                                  String creditFreq, String lastMatchingPair, Double overdraftLimit, String realTimeTransfer,
                                  String checkDigitDeclared, String accountPledging, String chequeDeliveryMethod,
                                  String defaultChequeBookType, String temporaryOpening, boolean jointAccount,
                                  String responsibleCustomer, String jointAccountTitle) {

        BankingAccountDetailData data = new BankingAccountDetailData();

        // Informations de base
        data.key = key;
        data.branchCode = branchCode;
        data.branchDesignation = "AGENCE TEST";
        data.currencyAlphaCode = "XOF";
        data.currencyNumericCode = currencyCode;
        data.currencyDesignation = "FRANC CFA BCEAO";
        data.accountNumber = accountNumber;
        data.accountSuffix = suffix;
        data.accountClassCode = accountClassCode;
        data.accountClassDesignation = accountClassDesignation;
        data.accountKey = accountKey;
        data.customerNumber = customerNumber;
        data.customerName = customerName;
        data.accountDesignation = accountDesignation;
        data.serviceCode = serviceCode;
        data.serviceDesignation = serviceDesignation;
        data.accountSide = accountSide;
        data.matchingCode = matchingCode;
        data.accountTypeCode = accountTypeCode;
        data.accountTypeDesignation = accountTypeDesignation;
        data.subjectToInterest = subjectToInterest;
        data.interestLadderCode = interestLadderCode;
        data.statementCode = statementCode;
        data.taxable = taxable;
        data.notToBePurged = notToBePurged;
        data.pendingClosure = pendingClosure;
        data.directCreditCeiling = directCreditCeiling;
        data.chequeThreshold = chequeThreshold;
        data.closed = closed;
        data.openingDate = openingDate;
        data.closureDate = closureDate;
        data.modificationSheetNumber = modificationSheetNumber;
        data.deductionAtSource = deductionAtSource;

        // Soldes
        data.accountingBalance = accountingBalance;
        data.valueDateBalance = valueDateBalance;
        data.historyBalance = historyBalance;
        data.interestCalculationBalance = interestCalculationBalance;
        data.indicativeBalance = indicativeBalance;
        data.unavailableNoDirectCredit = unavailableNoDirectCredit;
        data.unavailableDirectCredit = unavailableDirectCredit;
        data.dailyUnavailableNoDirectCredit = dailyUnavailableNoDirectCredit;
        data.dailyUnavailableDirectCredit = dailyUnavailableDirectCredit;
        data.debitTurnovers = debitTurnovers;
        data.creditTurnovers = creditTurnovers;

        // Produit
        data.productCode = productCode;
        data.productDesignation = productDesignation;
        data.productAttribute = productAttribute;

        // Utilisateurs
        data.userInitiatedCode = userInitiatedCode;
        data.userInitiatedName = userInitiatedName;
        data.forcingLevel = forcingLevel;
        data.languageCode = languageCode;
        data.languageDesignation = languageDesignation;

        // Paramètres
        data.debitFreq = debitFreq;
        data.transferDebtRecovery = transferDebtRecovery;
        data.creditFreq = creditFreq;
        data.lastMatchingPair = lastMatchingPair;
        data.overdraftLimit = overdraftLimit;
        data.realTimeTransfer = realTimeTransfer;
        data.checkDigitDeclared = checkDigitDeclared;
        data.accountPledging = accountPledging;
        data.chequeDeliveryMethod = chequeDeliveryMethod;
        data.defaultChequeBookType = defaultChequeBookType;
        data.temporaryOpening = temporaryOpening;
        data.jointAccount = jointAccount;
        data.responsibleCustomer = responsibleCustomer;
        data.jointAccountTitle = jointAccountTitle;

        mockAccountDetails.put(key, data);
    }

    // ========================================
    // CONSTRUCTION DES RÉPONSES
    // ========================================

    private GetAccountDetailResponse buildAccountDetailResponse(BankingAccountDetailData data) throws Exception {
        GetAccountDetailResponse response = new GetAccountDetailResponse();

        // Agence
        Branch branch = new Branch();
        branch.setCode(data.branchCode);
        branch.setDesignation(data.branchDesignation);
        response.setBranch(branch);

        // Devise
        SimpleCurrency currency = new SimpleCurrency();
        currency.setAlphaCode(data.currencyAlphaCode);
        currency.setNumericCode(data.currencyNumericCode);
        currency.setDesignation(data.currencyDesignation);
        response.setCurrency(currency);

        // Informations de base
        response.setAccountNumber(data.accountNumber);
        response.setAccountSuffix(data.accountSuffix);

        // Classe de compte
        AccountClass accountClass = new AccountClass();
        accountClass.setCode(data.accountClassCode);
        accountClass.setDesignation(data.accountClassDesignation);
        response.setAccountClass(accountClass);

        response.setAccountKey(data.accountKey);

        // Client
        RestrictedCustomer customer = new RestrictedCustomer();
        customer.setCustomerNumber(data.customerNumber);
        customer.setDisplayedName(data.customerName);
        response.setCustomer(customer);

        response.setAccountDesignation(data.accountDesignation);

        // Service - CORRECTED: Créer une instance simple sans référence à la classe abstraite
        // Remplacer par des setters directs sur la response si disponibles
        // ou ignorer si Service ne peut pas être instancié

        // CORRECTED: Supprimer complètement les enums qui n'existent pas
        // Les champs AccountSide et MatchingCode seront ignorés car les enums n'existent pas
        // response.setAccountSide(...) et response.setMatchingCode(...) supprimés

        // Type de compte
        AccountType accountType = new AccountType();
        accountType.setCode(data.accountTypeCode);
        accountType.setDesignation(data.accountTypeDesignation);
        response.setAccountType(accountType);

        // CORRECTED: Supprimer complètement les enums qui n'existent pas
        // AccountSubjectToInterestCalculation et CodeForInterestLadderPrinting supprimés
        // car les enums ne sont pas disponibles dans votre modèle généré
        response.setAccountStatementCode(data.statementCode);
        response.setTaxableAccount(data.taxable);
        response.setAccountNotToBePurged(data.notToBePurged);
        response.setPendingClosure(data.pendingClosure);
        response.setDirectCreditCeiling(BigDecimal.valueOf(data.directCreditCeiling));
        response.setThresholdForReorderingCheques(BigDecimal.valueOf(data.chequeThreshold));
        response.setClosedAccount(data.closed);

        // Dates
        if (data.openingDate != null) {
            response.setOpeningDate(parseDate(data.openingDate));
        }
        if (data.closureDate != null) {
            response.setClosureDate(parseDate(data.closureDate));
        }

        response.setModificationSheetNumber(BigDecimal.valueOf(data.modificationSheetNumber));
        response.setAccountSubjectToDeductionAtSource(BigDecimal.valueOf(data.deductionAtSource));

        // Soldes
        response.setAccountingBalance(BigDecimal.valueOf(data.accountingBalance));
        response.setValueDateBalance(BigDecimal.valueOf(data.valueDateBalance));
        response.setHistoryBalance(BigDecimal.valueOf(data.historyBalance));
        response.setInterestCalculationBalance(BigDecimal.valueOf(data.interestCalculationBalance));
        response.setIndicativeBalance(BigDecimal.valueOf(data.indicativeBalance));
        response.setUnavailableFundsWithoutDirectCredit(BigDecimal.valueOf(data.unavailableNoDirectCredit));
        response.setUnavailableDirectCreditFunds(BigDecimal.valueOf(data.unavailableDirectCredit));
        response.setDailyUnavailableFundsWithoutDirectCredit(BigDecimal.valueOf(data.dailyUnavailableNoDirectCredit));
        response.setDailyUnavailableDirectCreditFunds(BigDecimal.valueOf(data.dailyUnavailableDirectCredit));
        response.setDebitTurnovers(BigDecimal.valueOf(data.debitTurnovers));
        response.setCreditTurnovers(BigDecimal.valueOf(data.creditTurnovers));
        response.setAvailableBalance(BigDecimal.valueOf(data.indicativeBalance));

        // Utilisateur initiateur
        User userInitiated = new User();
        userInitiated.setCode(data.userInitiatedCode);
        userInitiated.setName(data.userInitiatedName);
        userInitiated.setForcingLevel(String.valueOf(data.forcingLevel));

        Language language = new Language();
        language.setCode(data.languageCode);
        language.setDesignation(data.languageDesignation);
        userInitiated.setLanguage(language);

        response.setUserWhoInitiated(userInitiated);

        // Paramètres de fréquence
        response.setFrequencyOfDebitInterestCalculation(data.debitFreq);
        response.setTransferToDebtRecoveryProcedure(data.transferDebtRecovery);
        response.setFrequencyOfCreditInterestCalculation(data.creditFreq);
        response.setLastMatchingPairAllocated(data.lastMatchingPair);
        response.setOverdraftLimit1(BigDecimal.valueOf(data.overdraftLimit));
        response.setRealTimeTransferCode(data.realTimeTransfer);
        response.setCheckDigitDeclared(data.checkDigitDeclared);

        // Produit - CORRECTED: Supprimer ProductAttribute qui n'existe pas
        Product product = new Product();
        product.setCode(data.productCode);
        product.setDesignation(data.productDesignation);
        // product.setProductAttribute(...) supprimé car enum non disponible
        response.setProduct(product);

        response.setAccountPledging(data.accountPledging);
        response.setDefaultChequeBookType(data.defaultChequeBookType);

        // Agences (répéter pour les champs d'historique)
        response.setUserWhoCreated(userInitiated);
        response.setBranchWhereTheAccountWasCreated(branch);
        response.setBranchFromAccountInformationForm(branch);
        response.setLastBranchThatHeldTheAccount(branch);

        response.setTemporaryOpening(data.temporaryOpening);
        response.setJointAccount(data.jointAccount);
        response.setResponsibleCustomer(data.responsibleCustomer);

        return response;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private String extractAccountKey(GetAccountDetailRequest request) {
        if (request != null && request.getAccountIdentifier() != null) {
            InternalFormatSimpleAccountOurBranch identifier = request.getAccountIdentifier();
            return identifier.getBranch() + "-" + identifier.getCurrency() + "-" + identifier.getAccount();
        }
        return "";
    }

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("MOCK_DETAIL_" + requestCounter.getAndIncrement());
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
        // CORRECTED: Utiliser String directement car StatusCodeType peut ne pas exister
        status.setStatusCode("0");
        return status;
    }

    private GetAccountDetailResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetAccountDetailResponseFlow errorResponse = new GetAccountDetailResponseFlow();

            ResponseHeader header = createResponseHeader(requestId);
            errorResponse.setResponseHeader(header);

            ResponseStatus status = new ResponseStatus();
            // CORRECTED: Utiliser String directement
            status.setStatusCode("-1");

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
            return new GetAccountDetailResponseFlow();
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
    public int getMockAccountDetailsCount() {
        return mockAccountDetails.size();
    }

    public Set<String> getAvailableAccountKeys() {
        return new HashSet<>(mockAccountDetails.keySet());
    }

    public void printMockStatus() {
        System.out.println("🏦 ACCOUNT DETAIL MOCK STATUS: " + mockAccountDetails.size() + " comptes détaillés");
        mockAccountDetails.forEach((key, data) ->
                System.out.println("  - " + key + ": " + data.accountDesignation + " (" + data.customerName + ")")
        );
    }

    // CLASSE INTERNE POUR DONNÉES DÉTAILLÉES
    private static class BankingAccountDetailData {
        // Identifiants
        String key;
        String branchCode;
        String branchDesignation;
        String currencyAlphaCode;
        String currencyNumericCode;
        String currencyDesignation;
        String accountNumber;
        String accountSuffix;
        String accountClassCode;
        String accountClassDesignation;
        String accountKey;
        String customerNumber;
        String customerName;
        String accountDesignation;
        String serviceCode;
        String serviceDesignation;
        String accountSide;
        String matchingCode;
        String accountTypeCode;
        String accountTypeDesignation;
        String subjectToInterest;
        String interestLadderCode;
        String statementCode;
        boolean taxable;
        boolean notToBePurged;
        boolean pendingClosure;
        int directCreditCeiling;
        int chequeThreshold;
        boolean closed;
        String openingDate;
        String closureDate;
        int modificationSheetNumber;
        int deductionAtSource;

        // Soldes
        Double accountingBalance;
        Double valueDateBalance;
        Double historyBalance;
        Double interestCalculationBalance;
        Double indicativeBalance;
        Double unavailableNoDirectCredit;
        Double unavailableDirectCredit;
        Double dailyUnavailableNoDirectCredit;
        Double dailyUnavailableDirectCredit;
        Double debitTurnovers;
        Double creditTurnovers;

        // Produit
        String productCode;
        String productDesignation;
        String productAttribute;

        // Utilisateurs
        String userInitiatedCode;
        String userInitiatedName;
        int forcingLevel;
        String languageCode;
        String languageDesignation;

        // Paramètres
        String debitFreq;
        String transferDebtRecovery;
        String creditFreq;
        String lastMatchingPair;
        Double overdraftLimit;
        String realTimeTransfer;
        String checkDigitDeclared;
        String accountPledging;
        String chequeDeliveryMethod;
        String defaultChequeBookType;
        String temporaryOpening;
        boolean jointAccount;
        String responsibleCustomer;
        String jointAccountTitle;
    }
}