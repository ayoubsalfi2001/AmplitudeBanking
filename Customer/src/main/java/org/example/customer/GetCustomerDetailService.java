package org.example.customer;

import org.example.customer.client.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Interface pour le service bancaire Sopra Amplitude
 * Seulement les 2 méthodes du WSDL
 */
@WebService(targetNamespace = "http://soprabanking.com/amplitude")
public interface GetCustomerDetailService {

    /**
     * Récupérer les détails d'un client par son code
     */
    @WebMethod
    GetCustomerDetailResponseFlow getCustomerDetail(
            @WebParam(name = "parameters") GetCustomerDetailRequestFlow request
    );

    /**
     * Vérifier le statut du service
     */
    @WebMethod
    GetStatusResponseFlow getStatus(
            @WebParam(name = "parameters") GetStatusRequestFlow request
    );
}