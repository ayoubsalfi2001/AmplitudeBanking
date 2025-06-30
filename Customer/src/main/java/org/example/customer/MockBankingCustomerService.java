package org.example.customer;

import org.example.customer.client.*;
import org.example.customer.GetCustomerDetailService;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service Mock Bancaire - SEULEMENT 2 méthodes du WSDL
 * Mode DIRECT - Serveur sécurisé
 */
@Service
public class MockBankingCustomerService implements GetCustomerDetailService {

    private final Map<String, BankingCustomerData> mockCustomers = new HashMap<>();
    private final AtomicLong requestCounter = new AtomicLong(1);

    public MockBankingCustomerService() {
        System.out.println("🏦 ==========================================");
        System.out.println("🏦 MOCK BANKING SERVICE ACTIVÉ DIRECTEMENT");
        System.out.println("🏦 Méthodes: getCustomerDetail + getStatus");
        System.out.println("🏦 Mode: DIRECT (serveur sécurisé)");
        System.out.println("🏦 ==========================================");
        initializeMockData();
    }

    @Override
    public GetCustomerDetailResponseFlow getCustomerDetail(GetCustomerDetailRequestFlow request) {
        System.out.println("🏦 MOCK: getCustomerDetail() appelé");

        try {
            String customerCode = request.getGetCustomerDetailRequest()
                    .getCustomerIdentifier()
                    .getCustomerCode();

            System.out.println("🏦 Code client demandé: " + customerCode);
            simulateDelay(400);

            GetCustomerDetailResponseFlow responseFlow = new GetCustomerDetailResponseFlow();
            responseFlow.setResponseHeader(createResponseHeader(
                    request.getRequestHeader().getRequestId()
            ));
            responseFlow.setResponseStatus(createSuccessStatus());

            BankingCustomerData customerData = mockCustomers.get(customerCode);
            if (customerData != null) {
                responseFlow.setGetCustomerDetailResponse(
                        buildCustomerResponse(customerData)
                );
                System.out.println("✅ Client Mock trouvé: " + customerData.displayName);
            } else {
                responseFlow.setGetCustomerDetailResponse(
                        buildDefaultCustomer(customerCode)
                );
                System.out.println("⚠️ Client Mock par défaut créé pour: " + customerCode);
            }

            return responseFlow;

        } catch (Exception e) {
            System.err.println("❌ Erreur Mock Banking: " + e.getMessage());
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
            statusResponse.setServiceName("Mock Amplitude Banking Service");
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
        System.out.println("📋 Initialisation des données bancaires...");

        try {
            addCustomer("00100002", "DIOUF", "ABLAYE", "1", "M", "1983-06-20", "SENEGALAIS", "+221778665273");
            addCustomer("00100003", "TECHCORP SARL", "DAHMAN", "2", "C", "2015-03-15", "SENEGALAIS", "+221773334455");
            addCustomer("00100004", "FALL", "MAMADOU", "1", "M", "1990-12-10", "SENEGALAIS", "+221776667788");
            addCustomer("00100005", "DIALLO", "AISSATOU", "1", "F", "1985-08-25", "MALIEN", "+221779990011");
            addCustomer("00100006", "GLOBAL SOLUTIONS", "DA3DO3", "2", "C", "2010-01-01", "FRANCAIS", "+221772223344");

            System.out.println("✅ " + mockCustomers.size() + " clients bancaires initialisés");
            mockCustomers.forEach((code, data) ->
                    System.out.println("  📝 " + code + ": " + data.displayName)
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation: " + e.getMessage());
        }
    }

    private void addCustomer(String code, String lastname, String firstname,
                             String customerType, String sex, String birthDate,
                             String nationality, String phone) {
        BankingCustomerData customer = new BankingCustomerData();
        customer.customerCode = code;
        customer.lastname = lastname;
        customer.firstname = firstname;
        customer.customerType = customerType;
        customer.displayName = lastname + (firstname.isEmpty() ? "" : " " + firstname);
        customer.sex = sex;
        customer.birthDate = birthDate;
        customer.nationality = nationality;
        customer.phoneNumber = phone;

        mockCustomers.put(code, customer);
    }

    // ========================================
    // CONSTRUCTION DES RÉPONSES
    // ========================================

    private GetCustomerDetailResponse buildCustomerResponse(BankingCustomerData data) throws Exception {
        GetCustomerDetailResponse response = new GetCustomerDetailResponse();

        response.setCustomerCode(data.customerCode);
        response.setCustomerType(data.customerType);
        response.setLastname(data.lastname);
        response.setNameToReturn(data.displayName);

        // Langue
        Language language = new Language();
        language.setCode("001");
        language.setDesignation("Francais");
        response.setLanguage(language);

        // Titre
        TitleCode titleCode = new TitleCode();
        switch (data.sex) {
            case "M":
                titleCode.setCode("01");
                titleCode.setDesignation("MONSIEUR");
                break;
            case "F":
                titleCode.setCode("02");
                titleCode.setDesignation("MADAME");
                break;
            default:
                titleCode.setCode("03");
                titleCode.setDesignation("SOCIETE");
        }
        response.setTitleCode(titleCode);

        // Situation
        CustomerSituation situation = new CustomerSituation();
        Nationality nationality = new Nationality();
        nationality.setCode("686");
        nationality.setDesignation(data.nationality);
        situation.setNationalityCode(nationality);

        Country country = new Country();
        country.setCode("686");
        country.setDesignation("SENEGAL");
        situation.setCountryOfResidence(country);
        response.setSituation(situation);

        // FATCA
        CustomerFatca fatca = new CustomerFatca();
        FatcaStatus fatcaStatus = new FatcaStatus();
        fatcaStatus.setFatcaStatusCode("");
        fatca.setFatcaStatus(fatcaStatus);
        CrsStatus crsStatus = new CrsStatus();
        crsStatus.setCrsStatusCode("");
        fatca.setCrsStatus(crsStatus);
        response.setFatca(fatca);

        // Informations spécifiques
        CustomerSpecInfo specInfo = new CustomerSpecInfo();
        if ("1".equals(data.customerType)) {
            specInfo.setIndividualSpecInfo(buildIndividualInfo(data));
        } else {
            specInfo.setCorporateSpecInfo(buildCorporateInfo(data));
        }
        response.setSpecificInformation(specInfo);

        response.setGeneralAttributes(buildGeneralAttributes());
        response.setReportingAttributes(buildReportingAttributes());
        response.setPaymentMethods(buildPaymentMethods());
        response.setAdditionnalInformation(buildAdditionalInfo());
        response.setAddressesDetail(buildAddressDetail(data.customerCode));
        response.setPhoneNumbers(buildPhoneNumbers(data.customerCode, data.displayName, data.phoneNumber));
        response.setActiveProfile(buildActiveProfile());

        return response;
    }

    private CustomerIndividualSpecInfo buildIndividualInfo(BankingCustomerData data) throws Exception {
        CustomerIndividualSpecInfo individualInfo = new CustomerIndividualSpecInfo();

        CustomerIndividualGeneralInfo generalInfo = new CustomerIndividualGeneralInfo();
        generalInfo.setFirstname(data.firstname);

        FamilyStatus familyStatus = new FamilyStatus();
        familyStatus.setCode("M");
        familyStatus.setDesignation("MARIE(E)");
        generalInfo.setFamilyStatusCode(familyStatus);

        individualInfo.setIndividualGeneralInfo(generalInfo);

        CustomerBirth birth = new CustomerBirth();
        birth.setHolderSex(data.sex);
        birth.setBirthDate(parseDate(data.birthDate));
        birth.setBirthCity("MBADJI NDIOUFENE");
        birth.setBirthCounty("001");

        Country birthCountry = new Country();
        birthCountry.setCode("686");
        birthCountry.setDesignation("SENEGAL");
        birth.setBirthCountry(birthCountry);

        individualInfo.setBirth(birth);

        CustomerIdPaper idPaper = new CustomerIdPaper();
        IdPaperType idType = new IdPaperType();
        idType.setCode("00001");
        idType.setDesignation("CARTE NATIONALE D'IDENTITE");
        idPaper.setType(idType);
        idPaper.setIdPaperNumber("1210200101280");
        idPaper.setIdPaperDeliveryDate(parseDate("2017-02-25"));
        idPaper.setOrganisationWhichDeliver("MINT");
        idPaper.setIdPaperValidityDate(parseDate("2027-02-24"));

        individualInfo.setIdPaper(idPaper);

        CustomerTerritoriality territoriality = new CustomerTerritoriality();
        TerritorialityCode territorialityCode = new TerritorialityCode();
        territorialityCode.setCode("0");
        territorialityCode.setDesignation("SENEGAL");
        territoriality.setTerritorialityCode(territorialityCode);
        individualInfo.setTerritoriality(territoriality);

        CustomerFamily family = new CustomerFamily();
        family.setNumberOfChildren(0);
        individualInfo.setFamily(family);

        CustomerOtherAttributes otherAttributes = new CustomerOtherAttributes();
        otherAttributes.setHolderMotherName(".............");
        individualInfo.setOtherAttributes(otherAttributes);

        return individualInfo;
    }

    private CustomerCorporateSpecInfo buildCorporateInfo(BankingCustomerData data) throws Exception {
        CustomerCorporateSpecInfo corporateInfo = new CustomerCorporateSpecInfo();

        CustomerCorporateGeneralInfo generalInfo = new CustomerCorporateGeneralInfo();
        generalInfo.setTradeNameToDeclare(data.lastname);
        generalInfo.setCompanyCreationDate(parseDate(data.birthDate));

        LegalForm legalForm = new LegalForm();
        legalForm.setCode("SA");
        legalForm.setDesignation("SOCIETE ANONYME");
        generalInfo.setLegalFormCode(legalForm);

        corporateInfo.setCorporateGeneralInfo(generalInfo);
        return corporateInfo;
    }

    private CustomerGeneralAttributes buildGeneralAttributes() {
        CustomerGeneralAttributes attributes = new CustomerGeneralAttributes();

        Branch branch = new Branch();
        branch.setCode("00001");
        branch.setDesignation("AGENCE TEST");
        attributes.setBranchCode(branch);

        CustomerOfficer officer = new CustomerOfficer();
        officer.setCode("002");
        officer.setName("FAYE MARIANE");
        attributes.setCustomerOfficer(officer);

        attributes.setTaxableCustomer(true);
        return attributes;
    }

    private CustomerReportingAttributes buildReportingAttributes() {
        CustomerReportingAttributes reporting = new CustomerReportingAttributes();

        DeclaredHome home = new DeclaredHome();
        home.setCode("007");
        home.setDesignation("RESIDENTS UMOA");
        reporting.setDeclaredHome(home);

        CustomerActivityField activity = new CustomerActivityField();
        activity.setCode("60115");
        activity.setDesignation("Commerce, restaurant, hotel");
        reporting.setActivityFieldCode(activity);

        reporting.setGradingAgreement("N");
        reporting.setGradingAgreementAmount(new BigDecimal("0.0000"));
        reporting.setSecurityIssuer("N");
        reporting.setInternationalOperationsIndicator("N");

        CustomerCreditInfoCentre creditInfo = new CustomerCreditInfoCentre();
        reporting.setCreditInfoCentre(creditInfo);

        return reporting;
    }

    private CustomerPaymentMethods buildPaymentMethods() {
        CustomerPaymentMethods payment = new CustomerPaymentMethods();
        payment.setChequeBookFacilitySuspension("N");
        payment.setWithdrawalOfCreditCard("N");
        return payment;
    }

    private CustomerAdditionalInformation buildAdditionalInfo() throws Exception {
        CustomerAdditionalInformation additional = new CustomerAdditionalInformation();
        additional.setDeletionCode("N");
        additional.setUserWhoCreated("REPRISE");
        additional.setCreationDate(parseDate("2019-02-02"));
        additional.setModificationSheetNumber(new BigDecimal("0"));
        additional.setRealTimeTransferCode("N");
        return additional;
    }

    private GetCustomerAddressDetailResponse buildAddressDetail(String customerCode) {
        GetCustomerAddressDetailResponse addressResponse = new GetCustomerAddressDetailResponse();

        CustomerAddressDetail address = new CustomerAddressDetail();

        CustomerAddressDetailIdentifier identifier = new CustomerAddressDetailIdentifier();
        identifier.setCustomerCode(customerCode);

        AddressType addressType = new AddressType();
        addressType.setCode("D");
        addressType.setDesignation("Adresse declarative client");
        identifier.setAddressType(addressType);
        address.setIdentifier(identifier);

        Language language = new Language();
        language.setCode("001");
        language.setDesignation("Francais");
        address.setLanguage(language);

        AddressFormat format = new AddressFormat();
        format.setCode("GE");
        format.setDesignation("Geographique");
        address.setAddressFormat(format);

        address.setAddressLine1("Grand Dakar, Dakar");
        address.setAddressLine2("GRAND DAKAR 2 PARCELLE N 200");
        address.setCity("DAKAR");
        address.setPostalCode("0000");
        address.setPoBox("0000");

        Country country = new Country();
        country.setCode("686");
        country.setDesignation("SENEGAL");
        address.setCountryCode(country);

        Branch branch = new Branch();
        branch.setCode("00001");
        branch.setDesignation("AGENCE TEST");
        address.setCounterCode(branch);

        address.setNumberOfReturnMailsForWrongAddress(0);
        address.setCounty("Grand Dakar");
        address.setRegion("DAKAR");

        addressResponse.getCustomerAddressDetail().add(address);
        return addressResponse;
    }

    private GetCustomerPhoneNumberListResponse buildPhoneNumbers(String customerCode, String displayName, String phoneNumber) {
        GetCustomerPhoneNumberListResponse phoneResponse = new GetCustomerPhoneNumberListResponse();

        CustomerPhoneNumber phone = new CustomerPhoneNumber();

        RestrictedCustomer customer = new RestrictedCustomer();
        customer.setCustomerNumber(customerCode);
        customer.setDisplayedName(displayName);
        phone.setCustomer(customer);

        PhoneType phoneType = new PhoneType();
        phoneType.setCode("001");
        phoneType.setDesignation("TELEPHONE MOBILE 1");
        phone.setPhoneType(phoneType);

        phone.setPhoneNumber(phoneNumber);

        phoneResponse.getCustomerPhoneNumber().add(phone);
        return phoneResponse;
    }

    private GetCustomerActiveProfileResponse buildActiveProfile() {
        GetCustomerActiveProfileResponse profileResponse = new GetCustomerActiveProfileResponse();

        CustomerProfile profile = new CustomerProfile();
        profile.setCode("101");
        profile.setDesignation("PART A REV INDETERMINE");

        profileResponse.setActiveProfile(profile);
        return profileResponse;
    }

    private GetCustomerDetailResponse buildDefaultCustomer(String customerCode) throws Exception {
        GetCustomerDetailResponse defaultCustomer = new GetCustomerDetailResponse();

        defaultCustomer.setCustomerCode(customerCode);
        defaultCustomer.setCustomerType("1");
        defaultCustomer.setLastname("CLIENT_INCONNU");
        defaultCustomer.setNameToReturn("CLIENT INCONNU " + customerCode);

        Language language = new Language();
        language.setCode("001");
        language.setDesignation("Francais");
        defaultCustomer.setLanguage(language);

        return defaultCustomer;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    private ResponseHeader createResponseHeader(String requestId) throws Exception {
        ResponseHeader header = new ResponseHeader();
        header.setRequestId(requestId);
        header.setResponseId("MOCK_" + requestCounter.getAndIncrement());
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

    private GetCustomerDetailResponseFlow buildErrorResponse(String requestId, String errorMessage) {
        try {
            GetCustomerDetailResponseFlow errorResponse = new GetCustomerDetailResponseFlow();

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
            return new GetCustomerDetailResponseFlow();
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
    public int getMockCustomersCount() {
        return mockCustomers.size();
    }

    public Set<String> getAvailableCustomerCodes() {
        return mockCustomers.keySet();
    }

    public void printMockStatus() {
        System.out.println("🏦 BANKING MOCK STATUS: " + mockCustomers.size() + " clients");
        mockCustomers.forEach((code, data) ->
                System.out.println("  - " + code + ": " + data.displayName)
        );
    }

    // CLASSE INTERNE POUR DONNÉES
    private static class BankingCustomerData {
        String customerCode;
        String lastname;
        String firstname;
        String customerType;
        String displayName;
        String sex;
        String birthDate;
        String nationality;
        String phoneNumber;
    }
}
