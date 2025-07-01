package org.example.accountdetails;

import org.example.accountdetails.Account.*;
import org.example.accountdetails.Account.RequestHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Contrôleur REST pour service de détail des comptes
 */
@RestController
@RequestMapping("/api/banking/account-detail")
@CrossOrigin(origins = "*")
public class BankingAccountDetailController {

    @Autowired
    private MockBankingAccountDetailService accountDetailService;

    /**
     * GET /api/banking/account-detail/{branch}/{currency}/{accountNumber} - Détail d'un compte
     */
    @GetMapping("/{branch}/{currency}/{accountNumber}")
    public ResponseEntity<?> getAccountDetail(@PathVariable String branch,
                                              @PathVariable String currency,
                                              @PathVariable String accountNumber,
                                              @RequestParam(required = false) String suffix,
                                              @RequestParam(defaultValue = "false") boolean fullResponse,
                                              @RequestParam(defaultValue = "false") boolean includeAddresses,
                                              @RequestParam(defaultValue = "false") boolean includePhones,
                                              @RequestParam(defaultValue = "false") boolean includeEmails,
                                              @RequestParam(defaultValue = "false") boolean includeContacts) {
        System.out.println("🏦 === GET /api/banking/account-detail/" + branch + "/" + currency + "/" + accountNumber + " ===");

        try {
            GetAccountDetailRequestFlow soapRequest = buildAccountDetailRequest(
                    branch, currency, accountNumber, suffix,
                    includeAddresses, includePhones, includeEmails, includeContacts
            );

            GetAccountDetailResponseFlow soapResponse = accountDetailService.getAccountDetail(soapRequest);

            if (!"0".equals(soapResponse.getResponseStatus().getStatusCode().toString())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Erreur récupération détail: " +
                                soapResponse.getResponseStatus().getStatusCode().toString()));
            }

            if (fullResponse) {
                return ResponseEntity.ok(soapResponse);
            } else {
                Map<String, Object> restResponse = convertToSimpleDetailResponse(soapResponse);
                return ResponseEntity.ok(restResponse);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération détail compte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur récupération détail: " + e.getMessage()));
        }
    }

    /**
     * POST /api/banking/account-detail/search - Recherche avec critères étendus
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchAccountDetail(@RequestBody Map<String, Object> searchCriteria) {
        System.out.println("🏦 === POST /api/banking/account-detail/search ===");
        System.out.println("Critères reçus: " + searchCriteria);

        try {
            String branch = (String) searchCriteria.get("branch");
            String currency = (String) searchCriteria.get("currency");
            String accountNumber = (String) searchCriteria.get("accountNumber");
            String suffix = (String) searchCriteria.get("suffix");

            boolean includeAddresses = Boolean.TRUE.equals(searchCriteria.get("includeAddresses"));
            boolean includePhones = Boolean.TRUE.equals(searchCriteria.get("includePhones"));
            boolean includeEmails = Boolean.TRUE.equals(searchCriteria.get("includeEmails"));
            boolean includeContacts = Boolean.TRUE.equals(searchCriteria.get("includeContacts"));
            boolean fullResponse = Boolean.TRUE.equals(searchCriteria.get("fullResponse"));

            GetAccountDetailRequestFlow soapRequest = buildAccountDetailRequest(
                    branch, currency, accountNumber, suffix,
                    includeAddresses, includePhones, includeEmails, includeContacts
            );

            GetAccountDetailResponseFlow soapResponse = accountDetailService.getAccountDetail(soapRequest);

            if (fullResponse) {
                return ResponseEntity.ok(soapResponse);
            } else {
                Map<String, Object> restResponse = convertToSimpleDetailResponse(soapResponse);
                return ResponseEntity.ok(restResponse);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur recherche détail: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur recherche détail: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/account-detail/status - Statut du service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAccountDetailStatus() {
        System.out.println("🏦 === GET /api/banking/account-detail/status ===");

        try {
            GetStatusRequestFlow statusRequest = new GetStatusRequestFlow();
            statusRequest.setGetStatusRequest("detail_status_check");

            GetStatusResponseFlow statusResponse = accountDetailService.getStatus(statusRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("serviceName", statusResponse.getGetStatusResponse().getServiceName());
            response.put("timestamp", statusResponse.getGetStatusResponse().getTimeStamp());
            response.put("mode", "MOCK_DETAIL_DIRECT");
            response.put("description", "Service de détail des comptes Mock Sopra");
            response.put("totalMockAccountDetails", accountDetailService.getMockAccountDetailsCount());
            response.put("available", true);

            System.out.println("✅ Statut service détail comptes retourné");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur statut service détail: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur statut détail: " + e.getMessage()));
        }
    }

    /**
     * GET /api/banking/account-detail/keys - Liste des clés de comptes disponibles
     */
    @GetMapping("/keys")
    public ResponseEntity<?> getAvailableAccountKeys() {
        System.out.println("🏦 === GET /api/banking/account-detail/keys ===");

        try {
            Set<String> availableKeys = accountDetailService.getAvailableAccountKeys();

            Map<String, Object> response = new HashMap<>();
            response.put("totalKeys", availableKeys.size());
            response.put("accountKeys", availableKeys);
            response.put("timestamp", System.currentTimeMillis());
            response.put("description", "Clés des comptes disponibles pour consultation détaillée");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération clés: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur récupération clés: " + e.getMessage()));
        }
    }

    // ========================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================

    private GetAccountDetailRequestFlow buildAccountDetailRequest(String branch, String currency,
                                                                  String accountNumber, String suffix,
                                                                  boolean includeAddresses, boolean includePhones,
                                                                  boolean includeEmails, boolean includeContacts) throws Exception {
        GetAccountDetailRequestFlow request = new GetAccountDetailRequestFlow();

        // Header de requête
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setRequestId("REST_DETAIL_" + System.currentTimeMillis());
        requestHeader.setServiceName("getAccountDetail");
        requestHeader.setTimestamp(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        LocalDateTime.now().atZone(ZoneId.systemDefault()))));
        requestHeader.setUserCode("REPRISE");
        request.setRequestHeader(requestHeader);

        // Requête de détail
        GetAccountDetailRequest detailRequest = new GetAccountDetailRequest();

        // Identifiant du compte
        InternalFormatSimpleAccountOurBranch accountIdentifier = new InternalFormatSimpleAccountOurBranch();
        accountIdentifier.setBranch(branch);
        accountIdentifier.setCurrency(currency);
        accountIdentifier.setAccount(accountNumber);
        if (suffix != null && !suffix.isEmpty()) {
            accountIdentifier.setSuffix(suffix);
        }
        detailRequest.setAccountIdentifier(accountIdentifier);

        // Données additionnelles
        if (includeAddresses || includePhones || includeEmails || includeContacts) {
            AccountAdditionalData additionalData = new AccountAdditionalData();
            additionalData.setAddressDetailData(includeAddresses);
            additionalData.setPhoneNumberlistData(includePhones);
            additionalData.setEmailAddressListData(includeEmails);
            additionalData.setContactListData(includeContacts);
            detailRequest.setAccountAdditionalData(additionalData);
        }

        request.setGetAccountDetailRequest(detailRequest);
        return request;
    }

    private Map<String, Object> convertToSimpleDetailResponse(GetAccountDetailResponseFlow soapResponse) {
        Map<String, Object> response = new HashMap<>();

        try {
            GetAccountDetailResponse detailResponse = soapResponse.getGetAccountDetailResponse();

            if (detailResponse != null) {
                Map<String, Object> accountDetail = new HashMap<>();

                // Informations de base
                accountDetail.put("accountNumber", detailResponse.getAccountNumber());
                accountDetail.put("accountSuffix", detailResponse.getAccountSuffix());
                accountDetail.put("accountKey", detailResponse.getAccountKey());
                accountDetail.put("accountDesignation", detailResponse.getAccountDesignation());

                // Agence
                if (detailResponse.getBranch() != null) {
                    Map<String, String> branch = new HashMap<>();
                    branch.put("code", detailResponse.getBranch().getCode());
                    branch.put("designation", detailResponse.getBranch().getDesignation());
                    accountDetail.put("branch", branch);
                }

                // Devise
                if (detailResponse.getCurrency() != null) {
                    Map<String, String> currency = new HashMap<>();
                    currency.put("alphaCode", detailResponse.getCurrency().getAlphaCode());
                    currency.put("numericCode", detailResponse.getCurrency().getNumericCode());
                    currency.put("designation", detailResponse.getCurrency().getDesignation());
                    accountDetail.put("currency", currency);
                }

                // Client
                if (detailResponse.getCustomer() != null) {
                    Map<String, String> customer = new HashMap<>();
                    customer.put("customerNumber", detailResponse.getCustomer().getCustomerNumber());
                    customer.put("displayedName", detailResponse.getCustomer().getDisplayedName());
                    accountDetail.put("customer", customer);
                }

                // Classe de compte
                if (detailResponse.getAccountClass() != null) {
                    Map<String, String> accountClass = new HashMap<>();
                    accountClass.put("code", detailResponse.getAccountClass().getCode());
                    accountClass.put("designation", detailResponse.getAccountClass().getDesignation());
                    accountDetail.put("accountClass", accountClass);
                }

                // Service
                if (detailResponse.getService() != null) {
                    Map<String, String> service = new HashMap<>();
                    service.put("code", detailResponse.getService().getCode());
                    service.put("designation", detailResponse.getService().getDesignation());
                    accountDetail.put("service", service);
                }

                // Type de compte
                if (detailResponse.getAccountType() != null) {
                    Map<String, String> accountType = new HashMap<>();
                    accountType.put("code", detailResponse.getAccountType().getCode());
                    accountType.put("designation", detailResponse.getAccountType().getDesignation());
                    accountDetail.put("accountType", accountType);
                }

                // Produit
                if (detailResponse.getProduct() != null) {
                    Map<String, String> product = new HashMap<>();
                    product.put("code", detailResponse.getProduct().getCode());
                    product.put("designation", detailResponse.getProduct().getDesignation());
                    product.put("attribute", detailResponse.getProduct().getProductAttribute().toString());
                    accountDetail.put("product", product);
                }

                // Informations de statut
                accountDetail.put("accountSide", detailResponse.getAccountSide() != null ?
                        detailResponse.getAccountSide().toString() : null);
                accountDetail.put("matchingCode", detailResponse.getMatchingCode() != null ?
                        detailResponse.getMatchingCode().toString() : null);
                accountDetail.put("subjectToInterestCalculation", detailResponse.getAccountSubjectToInterestCalculation() != null ?
                        detailResponse.getAccountSubjectToInterestCalculation().toString() : null);
                accountDetail.put("interestLadderPrinting", detailResponse.getCodeForInterestLadderPrinting() != null ?
                        detailResponse.getCodeForInterestLadderPrinting().toString() : null);
                accountDetail.put("statementCode", detailResponse.getAccountStatementCode());
                accountDetail.put("taxableAccount", detailResponse.isTaxableAccount());
                accountDetail.put("notToBePurged", detailResponse.isAccountNotToBePurged());
                accountDetail.put("pendingClosure", detailResponse.isPendingClosure());
                accountDetail.put("closedAccount", detailResponse.isClosedAccount());

                // Plafonds et seuils
                accountDetail.put("directCreditCeiling", detailResponse.getDirectCreditCeiling() != null ?
                        detailResponse.getDirectCreditCeiling().doubleValue() : 0);
                accountDetail.put("thresholdForReorderingCheques", detailResponse.getThresholdForReorderingCheques() != null ?
                        detailResponse.getThresholdForReorderingCheques().doubleValue() : 0);

                // Dates
                accountDetail.put("openingDate", detailResponse.getOpeningDate() != null ?
                        detailResponse.getOpeningDate().toString() : null);
                accountDetail.put("closureDate", detailResponse.getClosureDate() != null ?
                        detailResponse.getClosureDate().toString() : null);
                accountDetail.put("lastModificationDate", detailResponse.getLastModificationDate() != null ?
                        detailResponse.getLastModificationDate().toString() : null);

                // Numéros de modification et déduction
                accountDetail.put("modificationSheetNumber", detailResponse.getModificationSheetNumber() != null ?
                        detailResponse.getModificationSheetNumber().doubleValue() : 0);
                accountDetail.put("deductionAtSource", detailResponse.getAccountSubjectToDeductionAtSource() != null ?
                        detailResponse.getAccountSubjectToDeductionAtSource().doubleValue() : 0);

                // Soldes (section importante)
                Map<String, Object> balances = new HashMap<>();
                balances.put("accountingBalance", detailResponse.getAccountingBalance() != null ?
                        detailResponse.getAccountingBalance().doubleValue() : 0.0);
                balances.put("valueDateBalance", detailResponse.getValueDateBalance() != null ?
                        detailResponse.getValueDateBalance().doubleValue() : 0.0);
                balances.put("historyBalance", detailResponse.getHistoryBalance() != null ?
                        detailResponse.getHistoryBalance().doubleValue() : 0.0);
                balances.put("interestCalculationBalance", detailResponse.getInterestCalculationBalance() != null ?
                        detailResponse.getInterestCalculationBalance().doubleValue() : 0.0);
                balances.put("indicativeBalance", detailResponse.getIndicativeBalance() != null ?
                        detailResponse.getIndicativeBalance().doubleValue() : 0.0);
                balances.put("availableBalance", detailResponse.getAvailableBalance() != null ?
                        detailResponse.getAvailableBalance().doubleValue() : 0.0);
                balances.put("unavailableFundsWithoutDirectCredit", detailResponse.getUnavailableFundsWithoutDirectCredit() != null ?
                        detailResponse.getUnavailableFundsWithoutDirectCredit().doubleValue() : 0.0);
                balances.put("unavailableDirectCreditFunds", detailResponse.getUnavailableDirectCreditFunds() != null ?
                        detailResponse.getUnavailableDirectCreditFunds().doubleValue() : 0.0);
                accountDetail.put("balances", balances);

                // Mouvements
                Map<String, Object> movements = new HashMap<>();
                movements.put("debitTurnovers", detailResponse.getDebitTurnovers() != null ?
                        detailResponse.getDebitTurnovers().doubleValue() : 0.0);
                movements.put("creditTurnovers", detailResponse.getCreditTurnovers() != null ?
                        detailResponse.getCreditTurnovers().doubleValue() : 0.0);
                accountDetail.put("movements", movements);

                // Utilisateur qui a initié
                if (detailResponse.getUserWhoInitiated() != null) {
                    Map<String, Object> userInitiated = new HashMap<>();
                    userInitiated.put("code", detailResponse.getUserWhoInitiated().getCode());
                    userInitiated.put("name", detailResponse.getUserWhoInitiated().getName());
                    userInitiated.put("forcingLevel", detailResponse.getUserWhoInitiated().getForcingLevel());
                    if (detailResponse.getUserWhoInitiated().getLanguage() != null) {
                        Map<String, String> language = new HashMap<>();
                        language.put("code", detailResponse.getUserWhoInitiated().getLanguage().getCode());
                        language.put("designation", detailResponse.getUserWhoInitiated().getLanguage().getDesignation());
                        userInitiated.put("language", language);
                    }
                    accountDetail.put("userWhoInitiated", userInitiated);
                }

                // Paramètres de gestion
                Map<String, Object> managementParams = new HashMap<>();
                managementParams.put("frequencyOfDebitInterestCalculation", detailResponse.getFrequencyOfDebitInterestCalculation());
                managementParams.put("transferToDebtRecoveryProcedure", detailResponse.getTransferToDebtRecoveryProcedure());
                managementParams.put("frequencyOfCreditInterestCalculation", detailResponse.getFrequencyOfCreditInterestCalculation());
                managementParams.put("lastMatchingPairAllocated", detailResponse.getLastMatchingPairAllocated());
                managementParams.put("overdraftLimit1", detailResponse.getOverdraftLimit1() != null ?
                        detailResponse.getOverdraftLimit1().doubleValue() : 0.0);
                managementParams.put("realTimeTransferCode", detailResponse.getRealTimeTransferCode());
                managementParams.put("accountPledging", detailResponse.getAccountPledging());
                managementParams.put("temporaryOpening", detailResponse.getTemporaryOpening());
                managementParams.put("jointAccount", detailResponse.isJointAccount());
                accountDetail.put("managementParameters", managementParams);

                // Informations chèques
                Map<String, Object> chequeInfo = new HashMap<>();
                chequeInfo.put("deliveryMethod", detailResponse.getChequeDeliveryMethod() != null ?
                        detailResponse.getChequeDeliveryMethod().toString() : null);
                chequeInfo.put("defaultBookType", detailResponse.getDefaultChequeBookType());
                chequeInfo.put("checkDigitDeclared", detailResponse.getCheckDigitDeclared());
                accountDetail.put("chequeInformation", chequeInfo);

                response.put("accountDetail", accountDetail);
            }

            // Métadonnées de la réponse
            response.put("responseId", soapResponse.getResponseHeader().getResponseId());
            response.put("timestamp", soapResponse.getResponseHeader().getTimestamp());
            response.put("serviceVersion", soapResponse.getResponseHeader().getServiceVersion());
            response.put("statusCode", soapResponse.getResponseStatus().getStatusCode().toString());

        } catch (Exception e) {
            System.err.println("Erreur conversion réponse détail: " + e.getMessage());
            response.put("error", "Erreur lors de la conversion de la réponse détaillée");
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