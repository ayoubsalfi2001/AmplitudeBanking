package org.example.amortizabledetail;

import org.example.amortizabledetail.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Interface pour le service de détails des prêts amortissables Sopra Amplitude
 * Les 2 méthodes du WSDL getAmortizableLoanDetail
 */
@WebService(targetNamespace = "http://soprabanking.com/amplitude")
public interface GetAmortizableLoanDetailService {

    /**
     * Récupérer les détails complets d'un prêt amortissable
     */
    @WebMethod
    GetAmortizableLoanDetailResponseFlow getAmortizableLoanDetail(
            @WebParam(name = "parameters") GetAmortizableLoanDetailRequestFlow request
    );

    /**
     * Vérifier le statut du service de détails
     */
    @WebMethod
    GetStatusResponseFlow getStatus(
            @WebParam(name = "parameters") GetStatusRequestFlow request
    );
}