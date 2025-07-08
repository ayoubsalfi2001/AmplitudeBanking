package org.example.amortizabledetail;

import org.example.amortizabledetail.*;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service Mock pour les détails des prêts amortissables
 * Compatible avec les données du service AmortizableList
 */
@Service
public class MockAmortizableDetailService implements GetAmortizableLoanDetailService {

    private final Map<String, AmortizableLoanDetailData> mockLoanDetails = new HashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(1);

    public MockAmortizableDetailService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 MOCK AMORTIZABLE DETAIL SERVICE ACTIVÉ");
        System.out.println("🏦 Méthodes: getAmortizableLoanDetail + getStatus");
        System.out.println("🏦 Mode: MOCK DÉTAILLÉ COMPLET");
        System.out.println("🏦 ==========================================");
        initializeMockDetailData();
    }

    @Override
    public GetAmortizableLoanDetailResponseFlow getAmortizableLoanDetail(GetAmortizableLoanDetailRequestFlow request) {
        System.out.println("🏦 MOCK: getAmortizableLoanDetail() appelé");

        try {
            simulateDelay(400);

            // Extraire les paramètres de la requête
            AmortizableLoanDetailIdentifier identifier = request.getGetAmortizableLoanDetailRequest().getAmortizableLoanIdentifier();
            String key = generateKey(
                    identifier.getBranchCode(),
                    identifier.getFileNumber(),
                    identifier.getAmendmentNumber().toString()
            );

            System.out.println("🔍 Recherche détails pour: " + key);

            // Chercher les données
            AmortizableLoanDetailData detailData = mockLoanDetails.get(key);
            if (detailData == null) {
                System.out.println("❌ Prêt non trouvé: " + key);
                return buildErrorResponse(request.getRequestHeader().getRequestId(), "Prêt non trouvé");
            }

            // Construire la réponse de succès
            GetAmortizableLoanDetailResponseFlow responseFlow = new GetAmortizableLoanDetailResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(request.getRequestHeader().getRequestId()));
            responseFlow.setResponseStatus(createSuccessStatus());

            GetAmortizableLoanDetailResponse detailResponse = buildCompleteDetailResponse(detailData);
            responseFlow.setGetAmortizableLoanDetailResponse(detailResponse);

            System.out.println("✅ Détails prêt complets retournés pour: " + key);
            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Detail: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(
                    request.getRequestHeader().getRequestId(),
                    e.getMessage()
            );
        }
    }

    @Override
    public GetStatusResponseFlow getStatus(GetStatusRequestFlow request) {
        System.out.println("🏦 MOCK: getStatus() appelé pour détails");

        simulateDelay(150);

        GetStatusResponseFlow responseFlow = new GetStatusResponseFlow();
        GetStatusResponse statusResponse = new GetStatusResponse();

        try {
            statusResponse.setTimeStamp(createXMLTimestamp(LocalDateTime.now()));
            statusResponse.setServiceName("Mock Amplitude Amortizable Detail Service COMPLET");
            responseFlow.setGetStatusResponse(statusResponse);

            System.out.println("✅ Status Mock détails retourné");

        } catch (Exception e) {
            System.err.println("❌ Erreur Status: " + e.getMessage());
        }

        return responseFlow;
    }

    // ========================================
    // INITIALISATION DES DONNÉES MOCK DÉTAILLÉES
    // ========================================

    private void initializeMockDetailData() {
        System.out.println("📋 Initialisation des détails COMPLETS des prêts amortissables...");

        try {
            // Prêt 1: SALAF ATTAAOUNIA - Client DDD G (Amendment 0 - version originale)
            addLoanDetail("00001", "000002", "0", createCompleteSalafDetail0());

            // Prêt 2: SALAF ATTAAOUNIA - Client DDD G (Amendment 1 - version modifiée)
            addLoanDetail("00001", "000002", "1", createCompleteSalafDetail1());

            // Prêt 3: CREDIT IMMOBILIER - Client DDD G
            addLoanDetail("00001", "000003", "1", createCompleteCreditImmobilierDetail());

            // Prêt 4: CREDIT AUTO - Client DDD G
            addLoanDetail("00002", "000004", "1", createCompleteCreditAutoDetail());

            // Prêt 5: SALAF ATTAAOUNIA - Autre client
            addLoanDetail("00001", "000005", "1", createCompleteSalafDetail2());

            // Prêt 6: CREDIT IMMOBILIER - Terminé
            addLoanDetail("00003", "000006", "1", createCompleteCreditImmobilierFinished());

            System.out.println("✅ " + mockLoanDetails.size() + " détails COMPLETS de prêts initialisés");
            mockLoanDetails.forEach((key, data) ->
                    System.out.println("  📝 " + key + ": " + data.loanTypeLabel + " - " + data.customerName)
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation détails: " + e.getMessage());
        }
    }

    private AmortizableLoanDetailData createCompleteSalafDetail0() {
        AmortizableLoanDetailData data = new AmortizableLoanDetailData();

        // Toutes les données détaillées
        data.branchCode = "00001";
        data.branchName = "CASA KOERA";
        data.fileNumber = "000002";
        data.orderNumber = "";
        data.amendmentNumber = 0;
        data.customerNumber = "0001989";
        data.customerName = "DDD G";
        data.loanTypeCode = "104";
        data.loanTypeLabel = "SALAF ATTAAOUNIA";
        data.operationCode = "L04";
        data.operationName = "SALAF ATTAAOUNIA";
        data.fileCurrencyCode = "MAD";
        data.fileCurrencyName = "DIRHAM MAROCAIN";
        data.accountsCurrencyCode = "MAD";
        data.accountsCurrencyName = "DIRHAM MAROCAIN";
        data.establishmentDate = "2024-09-25";
        data.firstInstallmentDate = "2024-10-25";
        data.lastInstallmentDate = "2026-03-25";
        data.loanAmount = new BigDecimal("7000.0000");
        data.constantInstallmentAmount = new BigDecimal("438.5600");
        data.originOrFixRate = new BigDecimal("15.600000");
        data.annualPercentageRate = new BigDecimal("23.832000");
        data.interestRate = new BigDecimal("15.600000");
        data.cumulativeInterest = new BigDecimal("0.0000");
        data.feesAmount = new BigDecimal("200.0000");
        data.fileStatus = "VA";
        data.processingCode = "9";
        data.currentGradeCode = "0";
        data.currentGradeName = "Saint";
        data.userCode = "HAOUZAL";
        data.userName = "HATIM AOUZAL";

        return data;
    }

    private AmortizableLoanDetailData createCompleteSalafDetail1() {
        AmortizableLoanDetailData data = createCompleteSalafDetail0();
        data.amendmentNumber = 1;
        data.establishmentDate = "2024-11-28";
        data.lastInstallmentDate = "2025-05-28";
        data.installmentsNumber = 6;
        data.totalInstallmentsNumber = 6;
        data.lastTriggeredInstallment = 3;
        data.cumulativeInterest = new BigDecimal("185.4500");
        data.cumulativeRepayment = new BigDecimal("956.1400");
        data.earlyRepaymentAmount = new BigDecimal("264.4700");
        return data;
    }

    private AmortizableLoanDetailData createCompleteCreditImmobilierDetail() {
        AmortizableLoanDetailData data = new AmortizableLoanDetailData();
        data.branchCode = "00001";
        data.branchName = "CASA KOERA";
        data.fileNumber = "000003";
        data.amendmentNumber = 1;
        data.customerNumber = "0001990";
        data.customerName = "DDD G";
        data.loanTypeCode = "105";
        data.loanTypeLabel = "CREDIT IMMOBILIER";
        data.loanAmount = new BigDecimal("150000.0000");
        data.originOrFixRate = new BigDecimal("5.500000");
        data.annualPercentageRate = new BigDecimal("6.200000");
        data.fileStatus = "VA";
        data.processingCode = "1";
        return data;
    }

    private AmortizableLoanDetailData createCompleteCreditAutoDetail() {
        AmortizableLoanDetailData data = new AmortizableLoanDetailData();
        data.branchCode = "00002";
        data.branchName = "AGENCE RABAT";
        data.fileNumber = "000004";
        data.amendmentNumber = 1;
        data.customerNumber = "0001991";
        data.customerName = "DDD G";
        data.loanTypeCode = "106";
        data.loanTypeLabel = "CREDIT AUTO";
        data.loanAmount = new BigDecimal("80000.0000");
        data.originOrFixRate = new BigDecimal("8.500000");
        data.fileStatus = "VA";
        data.processingCode = "3";
        return data;
    }

    private AmortizableLoanDetailData createCompleteSalafDetail2() {
        AmortizableLoanDetailData data = new AmortizableLoanDetailData();
        data.branchCode = "00001";
        data.branchName = "CASA KOERA";
        data.fileNumber = "000005";
        data.amendmentNumber = 1;
        data.customerNumber = "0001992";
        data.customerName = "DDD G";
        data.loanTypeCode = "104";
        data.loanTypeLabel = "SALAF ATTAAOUNIA";
        data.loanAmount = new BigDecimal("12000.0000");
        data.originOrFixRate = new BigDecimal("16.200000");
        data.fileStatus = "VA";
        data.processingCode = "1";
        return data;
    }

    private AmortizableLoanDetailData createCompleteCreditImmobilierFinished() {
        AmortizableLoanDetailData data = new AmortizableLoanDetailData();
        data.branchCode = "00003";
        data.branchName = "AGENCE FES";
        data.fileNumber = "000006";
        data.amendmentNumber = 1;
        data.customerNumber = "0001993";
        data.customerName = "DDD G";
        data.loanTypeCode = "105";
        data.loanTypeLabel = "CREDIT IMMOBILIER";
        data.loanAmount = new BigDecimal("200000.0000");
        data.originOrFixRate = new BigDecimal("6.000000");
        data.fileStatus = "TR";
        data.processingCode = "0";
        return data;
    }

    private GetAmortizableLoanDetailResponse buildCompleteDetailResponse(AmortizableLoanDetailData data) throws Exception {
        GetAmortizableLoanDetailResponse response = new GetAmortizableLoanDetailResponse();

        AmortizableLoanFile loanFile = new AmortizableLoanFile();

        // Construction COMPLÈTE de tous les objets du prêt
        // Identifier
        AmortizableLoanIdentifier identifier = new AmortizableLoanIdentifier();
        identifier.setFileNumber(data.fileNumber);
        identifier.setOrderNumber(data.orderNumber);
        identifier.setAmendmentNumber(BigDecimal.valueOf(data.amendmentNumber));

        Branch branch = new Branch();
        branch.setCode(data.branchCode);
        branch.setDesignation(data.branchName);
        identifier.setBranch(branch);

        loanFile.setAmortizableLoanIdentifier(identifier);

        // Client
        RestrictedCustomer customer = new RestrictedCustomer();
        customer.setCustomerNumber(data.customerNumber);
        customer.setDisplayedName(data.customerName);
        loanFile.setCustomer(customer);

        // Type de prêt
        AmortizableLoanType loanType = new AmortizableLoanType();
        loanType.setCode(data.loanTypeCode);
        loanType.setLabel(data.loanTypeLabel);
        loanFile.setLoanType(loanType);

        // Opération
        Operation operation = new Operation();
        operation.setCode(data.operationCode);
        operation.setDesignation(data.operationName);
        loanFile.setOperation(operation);

        // Devises
        SimpleCurrency fileCurrency = new SimpleCurrency();
        fileCurrency.setAlphaCode(data.fileCurrencyCode);
        fileCurrency.setNumericCode("504");
        fileCurrency.setDesignation(data.fileCurrencyName);
        loanFile.setFileCurrency(fileCurrency);

        SimpleCurrency accountsCurrency = new SimpleCurrency();
        accountsCurrency.setAlphaCode(data.accountsCurrencyCode);
        accountsCurrency.setNumericCode("504");
        accountsCurrency.setDesignation(data.accountsCurrencyName);
        loanFile.setAccountsCurrency(accountsCurrency);

        // Dates
        if (data.establishmentDate != null) {
            loanFile.setEstablishmentDate(parseDate(data.establishmentDate));
        }
        if (data.firstInstallmentDate != null) {
            loanFile.setFirstInstallmentDate(parseDate(data.firstInstallmentDate));
        }
        if (data.lastInstallmentDate != null) {
            loanFile.setLastInstallmentDate(parseDate(data.lastInstallmentDate));
        }

        // Montants
        loanFile.setLoanAmount(data.loanAmount);
        loanFile.setConstantInstallmentAmount(data.constantInstallmentAmount);

        // Taux
        AmortizableLoanFileRates fileRates = new AmortizableLoanFileRates();
        fileRates.setOriginOrFixRate(data.originOrFixRate);
        loanFile.setFileRates(fileRates);

        // TEG
        DosprtDetailAnnualPercentageRate apr = new DosprtDetailAnnualPercentageRate();
        apr.setAnnualPercentageRate(data.annualPercentageRate);
        loanFile.setAnnualPercentageRate(apr);

        // Intérêts
        DosprtDetailInterest interest = new DosprtDetailInterest();
        interest.setInterestRate(data.interestRate);
        interest.setCumulativeInterest(data.cumulativeInterest);
        loanFile.setInterest(interest);

        // Frais
        DosprtDetailFees fees = new DosprtDetailFees();
        fees.setFeesAmount(data.feesAmount);
        loanFile.setFees(fees);

        // Capital
        DosprtDetailCapital capital = new DosprtDetailCapital();
        if (data.cumulativeRepayment != null) {
            capital.setCumulativeRepayment(data.cumulativeRepayment);
        }
        loanFile.setCapital(capital);

        // Statut
        loanFile.setFileStatus(AmortizableLoanFileStatus.fromValue(data.fileStatus));

        // Note
        DosprtDetailFileGrade fileGrade = new DosprtDetailFileGrade();
        AmortizableLoanFileGrade currentGrade = new AmortizableLoanFileGrade();
        currentGrade.setCode(data.currentGradeCode);
        currentGrade.setDesignation(data.currentGradeName);
        fileGrade.setCurrentFileGrade(currentGrade);
        loanFile.setFileGrade(fileGrade);

        // Utilisateur
        User user = new User();
        user.setCode(data.userCode);
        user.setName(data.userName);
        loanFile.setUser(user);

        response.setAmortizableLoanFile(loanFile);
        return response;
    }

    // Méthodes utilitaires
    private void addLoanDetail(String branchCode, String fileNumber, String amendmentNumber, AmortizableLoanDetailData data) {
        String key = generateKey(branchCode, fileNumber, amendmentNumber);
        mockLoanDetails.put(key, data);
    }

    private String generateKey(String branchCode, String fileNumber, String amendmentNumber) {
        return branchCode + "_" + fileNumber + "_" + amendmentNumber;
    }

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("DETAIL_" + requestCounter.getAndIncrement());
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

    private GetAmortizableLoanDetailResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetAmortizableLoanDetailResponseFlow errorResponse = new GetAmortizableLoanDetailResponseFlow();
            ResponseHeader header = createResponseHeader(requestId);
            errorResponse.setResponseHeader(header);
            ResponseStatus status = new ResponseStatus();
            status.setStatusCode("-1");
            errorResponse.setResponseStatus(status);
            return errorResponse;
        } catch (Exception e) {
            return new GetAmortizableLoanDetailResponseFlow();
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

    // CLASSE INTERNE POUR TOUTES LES DONNÉES DE DÉTAIL
    private static class AmortizableLoanDetailData {
        // Identification
        String branchCode, branchName, fileNumber, orderNumber;
        Integer amendmentNumber;

        // Client
        String customerNumber, customerName;

        // Type de prêt
        String loanTypeCode, loanTypeLabel, operationCode, operationName;

        // Devises
        String fileCurrencyCode, fileCurrencyName, accountsCurrencyCode, accountsCurrencyName;

        // Dates
        String establishmentDate, firstInstallmentDate, lastInstallmentDate;
        String grantingDate, fileOpeningDate, fileModificationDate, offerExpiryDate;
        String creditRepaymentDate;

        // Échéancement
        String periodicityUnit, scheduleType, interestType;
        Integer installmentsNumber, totalInstallmentsNumber, lastTriggeredInstallment;

        // Montants
        BigDecimal loanAmount, constantInstallmentAmount, originOrFixRate;
        BigDecimal annualPercentageRate, interestRate, cumulativeInterest;
        BigDecimal feesAmount, cumulativeFeesAmount;
        BigDecimal commission1Amount, commission2Amount, commission2Rate;
        BigDecimal cumulativeRepayment, earlyRepaymentAmount;

        // Statut
        String fileStatus, processingCode, currentGradeCode, currentGradeName;

        // Utilisateur
        String userCode, userName, forcingLevel;
    }
}