package org.example.amortizable;

import org.example.amortizable.credit.*;
import org.example.amortizable.credit.Comparator;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service Mock pour les prêts amortissables - VERSION FINALE QUI FONCTIONNE
 */
@Service
public class MockAmortizableLoanService implements GetAmortizableLoanListService {

    private final Map<String, List<AmortizableLoanData>> mockLoans = new HashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(1);

    public MockAmortizableLoanService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 MOCK AMORTIZABLE LOANS SERVICE ACTIVÉ");
        System.out.println("🏦 Méthodes: getAmortizableLoanList + getStatus");
        System.out.println("🏦 ==========================================");
        initializeMockLoans();
    }

    @Override
    public GetAmortizableLoanListResponseFlow getAmortizableLoanList(GetAmortizableLoanListRequestFlow request) {
        System.out.println("🏦 MOCK: getAmortizableLoanList() appelé");

        try {
            String customerNumber = null;
            String loanTypeCode = null;
            String processingCode = null;
            String state = null;

            // Extraction des critères de recherche
            GetAmortizableLoanListRequest loanRequest = request.getGetAmortizableLoanListRequest();

            if (loanRequest.getCustomer() != null && loanRequest.getCustomer().getCustomer() != null) {
                customerNumber = loanRequest.getCustomer().getCustomer().getCustomerNumber();
                System.out.println("🏦 Client demandé: " + customerNumber);
            }

            if (loanRequest.getLoanType() != null) {
                loanTypeCode = loanRequest.getLoanType().getCode();
                System.out.println("🏦 Type de prêt: " + loanTypeCode);
            }

            if (loanRequest.getProcessingCode() != null) {
                processingCode = loanRequest.getProcessingCode().toString();
                System.out.println("🏦 Code de traitement: " + processingCode);
            }

            if (loanRequest.getState() != null) {
                state = loanRequest.getState().toString();
                System.out.println("🏦 État: " + state);
            }

            simulateDelay(500);

            GetAmortizableLoanListResponseFlow responseFlow = new GetAmortizableLoanListResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(
                    request.getRequestHeader().getRequestId()
            ));
            responseFlow.setResponseStatus(createSuccessStatus());

            // Filtrage des prêts selon les critères
            List<AmortizableLoanData> filteredLoans = filterLoans(customerNumber, loanTypeCode, processingCode, state);

            if (!filteredLoans.isEmpty()) {
                responseFlow.setGetAmortizableLoanListResponse(
                        buildLoansResponse(filteredLoans)
                );
                System.out.println("✅ " + filteredLoans.size() + " prêt(s) trouvé(s)");
            } else {
                responseFlow.setGetAmortizableLoanListResponse(new GetAmortizableLoanListResponse());
                System.out.println("⚠️ Aucun prêt trouvé pour les critères donnés");
            }

            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Loans: " + e.getMessage());
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
            statusResponse.setServiceName("Mock Amortizable Loans Service");
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

    private void initializeMockLoans() {
        System.out.println("📋 Initialisation des prêts amortissables...");

        try {
            // Prêts pour client 0001989 (comme dans votre exemple XML)
            addLoan("0001989", "000002", "00001", "CASA KOERA", "104", "SALAF ATTAAOUNIA",
                    "6043.8600", "2024-11-28", "2025-05-28", "VA", "1", 6, "15.6000000", "38.7760000");

            // Prêts pour client 0001990
            addLoan("0001990", "000003", "00001", "CASA KOERA", "105", "CREDIT IMMOBILIER",
                    "150000.0000", "2023-06-15", "2028-06-15", "VA", "1", 48, "5.5000000", "6.2000000");

            // Prêts pour client 0001991
            addLoan("0001991", "000004", "00002", "AGENCE RABAT", "106", "CREDIT AUTO",
                    "80000.0000", "2024-01-10", "2027-01-10", "VA", "1", 30, "8.5000000", "9.8000000");

            // Prêts pour client 0001992
            addLoan("0001992", "000005", "00001", "CASA KOERA", "104", "SALAF ATTAAOUNIA",
                    "12000.0000", "2024-09-01", "2025-09-01", "VA", "1", 12, "16.2000000", "39.5000000");

            // Prêts pour client 0001993 (prêt terminé)
            addLoan("0001993", "000006", "00003", "AGENCE FES", "105", "CREDIT IMMOBILIER",
                    "200000.0000", "2020-03-20", "2023-12-20", "TR", "0", 0, "6.0000000", "6.8000000");

            System.out.println("✅ " + getTotalLoansCount() + " prêts amortissables initialisés");
            mockLoans.forEach((customer, loans) ->
                    System.out.println("  📝 Client " + customer + ": " + loans.size() + " prêt(s)")
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation prêts: " + e.getMessage());
        }
    }

    private void addLoan(String customerNumber, String fileNumber, String branchCode, String branchName,
                         String loanTypeCode, String loanTypeLabel, String amount, String establishmentDate,
                         String lastInstallmentDate, String state, String processingCode, int remainingInstallments,
                         String interestRate, String teg) {

        AmortizableLoanData loan = new AmortizableLoanData();
        loan.customerNumber = customerNumber;
        loan.fileNumber = fileNumber;
        loan.branchCode = branchCode;
        loan.branchName = branchName;
        loan.loanTypeCode = loanTypeCode;
        loan.loanTypeLabel = loanTypeLabel;
        loan.amount = new BigDecimal(amount);
        loan.establishmentDate = establishmentDate;
        loan.lastInstallmentDate = lastInstallmentDate;
        loan.state = state;
        loan.processingCode = processingCode;
        loan.remainingInstallments = remainingInstallments;
        loan.interestRate = new BigDecimal(interestRate);
        loan.teg = new BigDecimal(teg);

        // Calculer le capital restant dû (simulation)
        if ("VA".equals(state)) {
            loan.outstandingCapital = loan.amount; // Même montant que l'exemple XML
        } else {
            loan.outstandingCapital = BigDecimal.ZERO;
        }

        mockLoans.computeIfAbsent(customerNumber, k -> new ArrayList<>()).add(loan);
    }

    // ========================================
    // FILTRAGE ET RECHERCHE
    // ========================================

    private List<AmortizableLoanData> filterLoans(String customerNumber, String loanTypeCode,
                                                  String processingCode, String state) {
        List<AmortizableLoanData> filtered = new ArrayList<>();

        for (List<AmortizableLoanData> customerLoans : mockLoans.values()) {
            for (AmortizableLoanData loan : customerLoans) {
                boolean matches = true;

                // Filtre par client
                if (customerNumber != null && !customerNumber.equals(loan.customerNumber)) {
                    matches = false;
                }

                // Filtre par type de prêt
                if (loanTypeCode != null && !loanTypeCode.equals(loan.loanTypeCode)) {
                    matches = false;
                }

                // Filtre par code de traitement
                if (processingCode != null && !processingCode.equals(loan.processingCode)) {
                    matches = false;
                }

                // Filtre par état
                if (state != null && !state.equals(loan.state)) {
                    matches = false;
                }

                if (matches) {
                    filtered.add(loan);
                }
            }
        }

        return filtered;
    }

    // ========================================
    // CONSTRUCTION DES RÉPONSES
    // ========================================

    private GetAmortizableLoanListResponse buildLoansResponse(List<AmortizableLoanData> loans) throws Exception {
        GetAmortizableLoanListResponse response = new GetAmortizableLoanListResponse();

        for (AmortizableLoanData loanData : loans) {
            GetAmortizableLoanResponse loan = new GetAmortizableLoanResponse();

            // Identifiant crédit
            AmortizableLoanIdentifier creditId = new AmortizableLoanIdentifier();
            creditId.setFileNumber(loanData.fileNumber);
            creditId.setOrderNumber(""); // Vide comme dans l'exemple XML
            creditId.setAmendmentNumber(BigDecimal.ONE);

            Branch branch = new Branch();
            branch.setCode(loanData.branchCode);
            branch.setDesignation(loanData.branchName);
            creditId.setBranch(branch);

            loan.setCreditIdentifier(creditId);

            // Devise
            SimpleCurrency currency = new SimpleCurrency();
            currency.setAlphaCode("MAD");
            currency.setNumericCode("504");
            currency.setDesignation("DIRHAM MAROCAIN");
            loan.setCurrency(currency);

            // Client
            PopulationFile customer = new PopulationFile();
            RestrictedCustomer restrictedCustomer = new RestrictedCustomer();
            restrictedCustomer.setCustomerNumber(loanData.customerNumber);
            restrictedCustomer.setDisplayedName("DDD G"); // Comme dans l'exemple XML
            customer.setCustomer(restrictedCustomer);
            customer.setCustomerType("1"); // Type particulier

            CustomerProfile activeProfile = new CustomerProfile();
            activeProfile.setCode("100");
            activeProfile.setDesignation("AGR");
            customer.setActiveProfile(activeProfile);

            CustomerOfficer officer = new CustomerOfficer();
            officer.setCode("001");
            officer.setName("BM RETAIL GESTIONNAIRE");
            customer.setCustomerOfficer(officer);

            loan.setCustomer(customer);

            // Type de prêt
            AmortizableLoanType loanType = new AmortizableLoanType();
            loanType.setCode(loanData.loanTypeCode);
            loanType.setLabel(loanData.loanTypeLabel);
            loan.setLoanType(loanType);

            // Dates
            ComparisonDate lastInstallmentDate = new ComparisonDate();
            lastInstallmentDate.setDate1(parseDate(loanData.lastInstallmentDate));
            lastInstallmentDate.setComparator(Comparator.EQUALS);
            loan.setLastInstallmentDate(lastInstallmentDate);

            ComparisonDate establishmentDate = new ComparisonDate();
            establishmentDate.setDate1(parseDate(loanData.establishmentDate));
            establishmentDate.setComparator(Comparator.EQUALS);
            loan.setEstablishmentDate(establishmentDate);

            // État
            loan.setState(AmortizableLoanFileStatus.fromValue(loanData.state));

            // CORRECTION FINALE: Ne pas définir processingCode car l'enum n'existe pas
            // Laissez cette ligne commentée si GetAmortizableLoanResponse ne supporte pas String
            // loan.setProcessingCode(loanData.processingCode);

            // Durée
            ComparisonNumber duration = new ComparisonNumber();
            duration.setNumber1(calculateMonthsDuration(loanData.establishmentDate, loanData.lastInstallmentDate));
            duration.setComparator(Comparator.EQUALS);
            loan.setMonthsDuration(duration);

            // Montant
            ComparisonAmount amount = new ComparisonAmount();
            amount.setAmount1(loanData.amount);
            amount.setComparator(Comparator.EQUALS);
            AmountCurrency amountCurrency = new AmountCurrency();
            amountCurrency.setCurrency(currency);
            amountCurrency.setNumberOfDecimals(BigDecimal.valueOf(2));
            amount.setCurrency(amountCurrency);
            loan.setAmount(amount);

            // Capital restant dû
            Amount outstandingCapital = new Amount();
            outstandingCapital.setAmount(loanData.outstandingCapital);
            outstandingCapital.setCurrency(amountCurrency);
            loan.setOutstandingCapital(outstandingCapital);

            // Taux de crédit
            SimpleLoanRate creditRate = new SimpleLoanRate();
            creditRate.setInterestRate(loanData.interestRate);
            creditRate.setTeg(loanData.teg);
            loan.setCreditRate(creditRate);

            // Autres informations
            loan.setInstallmentPeriods(AmortizableLoanInstallmentPeriods.M);
            loan.setNumberRemainingInstallments(loanData.remainingInstallments);

            // Montant impayé
            Amount unpaidAmount = new Amount();
            unpaidAmount.setAmount(BigDecimal.ZERO);
            unpaidAmount.setCurrency(amountCurrency);
            loan.setUnpaidAmount(unpaidAmount);

            response.getAmortizableLoan().add(loan);
        }

        return response;
    }

    private int calculateMonthsDuration(String startDate, String endDate) {
        try {
            String[] startParts = startDate.split("-");
            String[] endParts = endDate.split("-");

            int startYear = Integer.parseInt(startParts[0]);
            int startMonth = Integer.parseInt(startParts[1]);
            int endYear = Integer.parseInt(endParts[0]);
            int endMonth = Integer.parseInt(endParts[1]);

            return (endYear - startYear) * 12 + (endMonth - startMonth);
        } catch (Exception e) {
            return 6; // Valeur par défaut basée sur l'exemple
        }
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("1216"); // Comme dans l'exemple XML
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
        status.setStatusCode("0");
        return status;
    }

    private GetAmortizableLoanListResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetAmortizableLoanListResponseFlow errorResponse = new GetAmortizableLoanListResponseFlow();

            ResponseHeader header = createResponseHeader(requestId);
            errorResponse.setResponseHeader(header);

            ResponseStatus status = new ResponseStatus();
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
            return new GetAmortizableLoanListResponseFlow();
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

    // ========================================
    // MÉTHODES PUBLIQUES POUR MONITORING
    // ========================================

    public int getMockLoansCount() {
        return getTotalLoansCount();
    }

    public Set<String> getAvailableCustomers() {
        return mockLoans.keySet();
    }

    public List<Map<String, Object>> getAllMockLoans() {
        List<Map<String, Object>> allLoans = new ArrayList<>();

        for (List<AmortizableLoanData> customerLoans : mockLoans.values()) {
            for (AmortizableLoanData loan : customerLoans) {
                Map<String, Object> loanMap = new HashMap<>();
                loanMap.put("customerNumber", loan.customerNumber);
                loanMap.put("fileNumber", loan.fileNumber);
                loanMap.put("branchCode", loan.branchCode);
                loanMap.put("branchName", loan.branchName);
                loanMap.put("loanTypeCode", loan.loanTypeCode);
                loanMap.put("loanTypeLabel", loan.loanTypeLabel);
                loanMap.put("amount", loan.amount);
                loanMap.put("outstandingCapital", loan.outstandingCapital);
                loanMap.put("establishmentDate", loan.establishmentDate);
                loanMap.put("lastInstallmentDate", loan.lastInstallmentDate);
                loanMap.put("state", loan.state);
                loanMap.put("processingCode", loan.processingCode);
                loanMap.put("remainingInstallments", loan.remainingInstallments);
                loanMap.put("interestRate", loan.interestRate);
                loanMap.put("teg", loan.teg);
                allLoans.add(loanMap);
            }
        }

        return allLoans;
    }

    private int getTotalLoansCount() {
        return mockLoans.values().stream().mapToInt(List::size).sum();
    }

    public void printMockStatus() {
        System.out.println("🏦 LOANS MOCK STATUS: " + getTotalLoansCount() + " prêts");
        mockLoans.forEach((customer, loans) ->
                System.out.println("  - Client " + customer + ": " + loans.size() + " prêt(s)")
        );
    }

    // ========================================
    // CLASSE INTERNE POUR DONNÉES
    // ========================================

    private static class AmortizableLoanData {
        String customerNumber;
        String fileNumber;
        String branchCode;
        String branchName;
        String loanTypeCode;
        String loanTypeLabel;
        BigDecimal amount;
        BigDecimal outstandingCapital;
        String establishmentDate;
        String lastInstallmentDate;
        String state;
        String processingCode;
        int remainingInstallments;
        BigDecimal interestRate;
        BigDecimal teg;
    }
}