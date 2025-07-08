package org.example.amortizabledetail;

import org.example.amortizabledetail.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST pour les détails des prêts amortissables
 * API simple : branchCode/fileNumber/amendmentNumber
 */
@RestController
@RequestMapping("/api/banking/amortizable-details")
@CrossOrigin(origins = "*")
public class AmortizableDetailsController {

    @Autowired
    private MockAmortizableDetailService detailService;

    /**
     * GET /api/banking/amortizable-details/{branchCode}/{fileNumber}/{amendmentNumber}
     * Récupérer les détails complets d'un prêt amortissable
     */
    @GetMapping("/{branchCode}/{fileNumber}/{amendmentNumber}")
    public ResponseEntity<?> getAmortizableLoanDetail(
            @PathVariable String branchCode,
            @PathVariable String fileNumber,
            @PathVariable String amendmentNumber) {

        System.out.println("🏦 === GET Détails Prêt: " + branchCode + "/" + fileNumber + "/" + amendmentNumber + " ===");

        try {
            // Construire la requête SOAP
            GetAmortizableLoanDetailRequestFlow soapRequest = buildDetailRequest(
                    branchCode, fileNumber, amendmentNumber);

            // Appeler le service Mock
            GetAmortizableLoanDetailResponseFlow soapResponse = detailService.getAmortizableLoanDetail(soapRequest);

            // Vérifier le statut de la réponse
            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode().toString())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Prêt non trouvé: " + branchCode + "/" + fileNumber + "/" + amendmentNumber));
            }

            // Convertir en réponse détaillée COMPLÈTE
            Map<String, Object> restResponse = convertToCompleteDetailedResponse(soapResponse);
            return ResponseEntity.ok(restResponse);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération détails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/amortizable-details/status
     * Statut du service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        System.out.println("🏦 === GET Status Service Détails ===");

        try {
            GetStatusRequestFlow statusRequest = new GetStatusRequestFlow();
            statusRequest.setGetStatusRequest("detail_status_check");

            GetStatusResponseFlow statusResponse = detailService.getStatus(statusRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", "AmortizableDetailService");
            response.put("timestamp", System.currentTimeMillis());
            response.put("mode", "MOCK_DETAIL_SERVICE");
            response.put("description", "Service de détails des prêts amortissables");
            response.put("available", true);

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

    /**
     * Construire la requête SOAP pour les détails
     */
    private GetAmortizableLoanDetailRequestFlow buildDetailRequest(
            String branchCode, String fileNumber, String amendmentNumber) throws Exception {

        GetAmortizableLoanDetailRequestFlow request = new GetAmortizableLoanDetailRequestFlow();

        // Header de requête
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("DETAIL_" + System.currentTimeMillis());
        requestHeader.setServiceName("getAmortizableLoanDetail");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("DETAIL_API");
        request.setRequestHeader(requestHeader);

        // Requête de détails
        GetAmortizableLoanDetailRequest detailRequest = new GetAmortizableLoanDetailRequest();
        AmortizableLoanDetailIdentifier identifier = new AmortizableLoanDetailIdentifier();

        identifier.setBranchCode(branchCode);
        identifier.setFileNumber(fileNumber);
        identifier.setOrderNumber(""); // Toujours vide selon votre exemple
        identifier.setAmendmentNumber(new BigDecimal(amendmentNumber));

        detailRequest.setAmortizableLoanIdentifier(identifier);
        request.setGetAmortizableLoanDetailRequest(detailRequest);

        return request;
    }

    /**
     * Convertir la réponse SOAP en format JSON COMPLET avec TOUS les champs
     */
    private Map<String, Object> convertToCompleteDetailedResponse(GetAmortizableLoanDetailResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            AmortizableLoanFile loanFile = soapResponse.getGetAmortizableLoanDetailResponse().getAmortizableLoanFile();

            // ========================================
            // 1. IDENTIFICATION DU PRÊT
            // ========================================
            Map<String, Object> identification = new HashMap<>();
            if (loanFile.getAmortizableLoanIdentifier() != null) {
                identification.put("fileNumber", loanFile.getAmortizableLoanIdentifier().getFileNumber());
                identification.put("orderNumber", loanFile.getAmortizableLoanIdentifier().getOrderNumber());
                identification.put("amendmentNumber", loanFile.getAmortizableLoanIdentifier().getAmendmentNumber());

                if (loanFile.getAmortizableLoanIdentifier().getBranch() != null) {
                    Map<String, String> branch = new HashMap<>();
                    branch.put("code", loanFile.getAmortizableLoanIdentifier().getBranch().getCode());
                    branch.put("designation", loanFile.getAmortizableLoanIdentifier().getBranch().getDesignation());
                    identification.put("branch", branch);
                }
            }
            response.put("identification", identification);

            // ========================================
            // 2. INFORMATIONS CLIENT
            // ========================================
            Map<String, Object> customer = new HashMap<>();
            if (loanFile.getCustomer() != null) {
                customer.put("customerNumber", loanFile.getCustomer().getCustomerNumber());
                customer.put("displayedName", loanFile.getCustomer().getDisplayedName());
            }
            response.put("customer", customer);

            // ========================================
            // 3. TYPE DE PRÊT ET OPÉRATION
            // ========================================
            Map<String, String> loanType = new HashMap<>();
            if (loanFile.getLoanType() != null) {
                loanType.put("code", loanFile.getLoanType().getCode());
                loanType.put("label", loanFile.getLoanType().getLabel());
            }
            response.put("loanType", loanType);

            Map<String, String> operation = new HashMap<>();
            if (loanFile.getOperation() != null) {
                operation.put("code", loanFile.getOperation().getCode());
                operation.put("designation", loanFile.getOperation().getDesignation());
            }
            response.put("operation", operation);

            // ========================================
            // 4. DEVISES
            // ========================================
            Map<String, String> fileCurrency = new HashMap<>();
            if (loanFile.getFileCurrency() != null) {
                fileCurrency.put("alphaCode", loanFile.getFileCurrency().getAlphaCode());
                fileCurrency.put("numericCode", loanFile.getFileCurrency().getNumericCode());
                fileCurrency.put("designation", loanFile.getFileCurrency().getDesignation());
            }
            response.put("fileCurrency", fileCurrency);

            Map<String, String> accountsCurrency = new HashMap<>();
            if (loanFile.getAccountsCurrency() != null) {
                accountsCurrency.put("alphaCode", loanFile.getAccountsCurrency().getAlphaCode());
                accountsCurrency.put("numericCode", loanFile.getAccountsCurrency().getNumericCode());
                accountsCurrency.put("designation", loanFile.getAccountsCurrency().getDesignation());
            }
            response.put("accountsCurrency", accountsCurrency);

            // ========================================
            // 5. DATES IMPORTANTES
            // ========================================
            Map<String, Object> dates = new HashMap<>();
            dates.put("establishmentDate", loanFile.getEstablishmentDate() != null ? loanFile.getEstablishmentDate().toString() : null);
            dates.put("firstInstallmentDate", loanFile.getFirstInstallmentDate() != null ? loanFile.getFirstInstallmentDate().toString() : null);
            dates.put("lastInstallmentDate", loanFile.getLastInstallmentDate() != null ? loanFile.getLastInstallmentDate().toString() : null);
            dates.put("grantingDate", loanFile.getGrantingDate() != null ? loanFile.getGrantingDate().toString() : null);
            dates.put("fileOpeningDate", loanFile.getFileOpeningDate() != null ? loanFile.getFileOpeningDate().toString() : null);
            dates.put("fileModificationDate", loanFile.getFileModificationDate() != null ? loanFile.getFileModificationDate().toString() : null);
            dates.put("offerExpiryDate", loanFile.getOfferExpiryDate() != null ? loanFile.getOfferExpiryDate().toString() : null);
            response.put("dates", dates);

            // ========================================
            // 6. PARAMÈTRES D'ÉCHÉANCEMENT
            // ========================================
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("periodicityUnit", loanFile.getPeriodicityUnit());
            schedule.put("installmentsNumber", loanFile.getInstallmentsNumber());
            schedule.put("totalInstallmentsNumber", loanFile.getTotalInstallmentsNumber());
            schedule.put("lastTriggeredInstallment", loanFile.getLastTriggeredInstallment());
            schedule.put("endOfMonthInstallment", loanFile.isEndOfMonthInstallment());
            schedule.put("calculationBasedOnRealNumber", loanFile.isCalculationBasedOnRealNumber());
            schedule.put("deferralPeriodManagement", loanFile.getDeferralPeriodManagement());
            schedule.put("scheduleType", loanFile.getScheduleType());
            schedule.put("interestType", loanFile.getInterestType());
            response.put("schedule", schedule);

            // ========================================
            // 7. MONTANTS ET TAUX
            // ========================================
            Map<String, Object> amounts = new HashMap<>();
            amounts.put("loanAmount", loanFile.getLoanAmount());
            amounts.put("constantInstallmentAmount", loanFile.getConstantInstallmentAmount());
            amounts.put("personalInvestmentAmount", loanFile.getPersonalInvestmentAmount());
            amounts.put("usedPersonalInvestmentAmount", loanFile.getUsedPersonalInvestmentAmount());
            amounts.put("baseRateVariation", loanFile.getBaseRateVariation());

            if (loanFile.getFileRates() != null) {
                Map<String, Object> rates = new HashMap<>();
                rates.put("originOrFixRate", loanFile.getFileRates().getOriginOrFixRate());
                amounts.put("fileRates", rates);
            }
            response.put("amounts", amounts);

            // ========================================
            // 8. DÉBLOCAGES DE FONDS
            // ========================================
            Map<String, Object> fundsReleases = new HashMap<>();
            if (loanFile.getFundsReleases() != null) {
                fundsReleases.put("cumulativeFundsReleases", loanFile.getFundsReleases().getCumulativeFundsReleases());
                fundsReleases.put("lastFundsReleaseDate", loanFile.getFundsReleases().getLastFundsReleaseDate() != null ?
                        loanFile.getFundsReleases().getLastFundsReleaseDate().toString() : null);
                fundsReleases.put("fundsReleaseMethod", loanFile.getFundsReleases().getFundsReleaseMethod());
            }
            response.put("fundsReleases", fundsReleases);

            // ========================================
            // 9. TAUX EFFECTIF GLOBAL (TEG)
            // ========================================
            Map<String, Object> apr = new HashMap<>();
            if (loanFile.getAnnualPercentageRate() != null) {
                apr.put("annualPercentageRate", loanFile.getAnnualPercentageRate().getAnnualPercentageRate());
                apr.put("aprSmoothing", loanFile.getAnnualPercentageRate().getAPRSmoothing());
                apr.put("proRataAPR", loanFile.getAnnualPercentageRate().getProRataAPR());
            }
            response.put("annualPercentageRate", apr);

            // ========================================
            // 10. INTÉRÊTS
            // ========================================
            Map<String, Object> interest = new HashMap<>();
            if (loanFile.getInterest() != null) {
                interest.put("interestRate", loanFile.getInterest().getInterestRate());
                interest.put("interestTaxRate", loanFile.getInterest().getInterestTaxRate());
                interest.put("cumulativeInterest", loanFile.getInterest().getCumulativeInterest());
                interest.put("cumulativeInterestTax", loanFile.getInterest().getCumulativeInterestTax());
                interest.put("interestMaturity", loanFile.getInterest().getInterestMaturity());
                interest.put("interestPeriodicity", loanFile.getInterest().getInterestPeriodicity());
                interest.put("lastInterestCalculationBalance", loanFile.getInterest().getLastInterestCalculationBalance());
            }
            response.put("interest", interest);

            // ========================================
            // 11. FRAIS
            // ========================================
            Map<String, Object> fees = new HashMap<>();
            if (loanFile.getFees() != null) {
                fees.put("feesRate", loanFile.getFees().getFeesRate());
                fees.put("feesTaxRate", loanFile.getFees().getFeesTaxRate());
                fees.put("feesAmount", loanFile.getFees().getFeesAmount());
                fees.put("cumulativeFeesAmount", loanFile.getFees().getCumulativeFeesAmount());
                fees.put("cumulativeFeesTax", loanFile.getFees().getCumulativeFeesTax());
                fees.put("feesCollectionMethod", loanFile.getFees().getFeesCollectionMethod());
                fees.put("feesRepaymentMethod", loanFile.getFees().getFeesRepaymentMethod());
            }
            response.put("fees", fees);

            // ========================================
            // 12. COMMISSIONS
            // ========================================
            Map<String, Object> commission1 = new HashMap<>();
            if (loanFile.getCommission1() != null) {
                commission1.put("commissionRate", loanFile.getCommission1().getCommissionRate());
                commission1.put("commissionAmount", loanFile.getCommission1().getCommissionAmount());
                commission1.put("cumulativeCommissionAmount", loanFile.getCommission1().getCumulativeCommissionAmount());
                commission1.put("commissionCalculationMethod", loanFile.getCommission1().getCommissionCalculationMethod());
            }
            response.put("commission1", commission1);

            Map<String, Object> commission2 = new HashMap<>();
            if (loanFile.getCommission2() != null) {
                commission2.put("commissionRate", loanFile.getCommission2().getCommissionRate());
                commission2.put("commissionAmount", loanFile.getCommission2().getCommissionAmount());
                commission2.put("cumulativeCommissionAmount", loanFile.getCommission2().getCumulativeCommissionAmount());
                commission2.put("commissionCalculationMethod", loanFile.getCommission2().getCommissionCalculationMethod());
            }
            response.put("commission2", commission2);

            // ========================================
            // 13. CAPITAL
            // ========================================
            Map<String, Object> capital = new HashMap<>();
            if (loanFile.getCapital() != null) {
                capital.put("capitalTaxRate", loanFile.getCapital().getCapitalTaxRate());
                capital.put("cumulativeCapitalTax", loanFile.getCapital().getCumulativeCapitalTax());
                capital.put("cumulativeRepayment", loanFile.getCapital().getCumulativeRepayment());
                capital.put("capitalMaturity", loanFile.getCapital().getCapitalMaturity());
                capital.put("capitalPeriodicity", loanFile.getCapital().getCapitalPeriodicity());
            }
            response.put("capital", capital);

            // ========================================
            // 14. STATUT ET SUIVI
            // ========================================
            Map<String, Object> status = new HashMap<>();
            status.put("fileStatus", loanFile.getFileStatus());
            status.put("processingCode", loanFile.getProcessingCode());

            if (loanFile.getFileGrade() != null) {
                Map<String, Object> fileGrade = new HashMap<>();
                if (loanFile.getFileGrade().getCurrentFileGrade() != null) {
                    Map<String, String> currentGrade = new HashMap<>();
                    currentGrade.put("code", loanFile.getFileGrade().getCurrentFileGrade().getCode());
                    currentGrade.put("designation", loanFile.getFileGrade().getCurrentFileGrade().getDesignation());
                    fileGrade.put("currentFileGrade", currentGrade);
                }
                status.put("fileGrade", fileGrade);
            }
            response.put("status", status);

            // ========================================
            // 15. UTILISATEUR
            // ========================================
            Map<String, Object> user = new HashMap<>();
            if (loanFile.getUser() != null) {
                user.put("code", loanFile.getUser().getCode());
                user.put("name", loanFile.getUser().getName());
                user.put("forcingLevel", loanFile.getUser().getForcingLevel());
            }
            response.put("user", user);

            // ========================================
            // 16. MÉTADONNÉES DE RÉPONSE
            // ========================================
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("responseId", soapResponse.getResponseHeader().getResponseId());
            metadata.put("timestamp", soapResponse.getResponseHeader().getTimestamp().toString());
            metadata.put("serviceVersion", soapResponse.getResponseHeader().getServiceVersion());
            metadata.put("statusCode", soapResponse.getResponseStatus().getStatusCode().toString());
            response.put("metadata", metadata);

        } catch (Exception e) {
            System.err.println("❌ Erreur conversion réponse complète: " + e.getMessage());
            response.put("error", "Erreur lors de la conversion de la réponse");
        }

        return response;
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