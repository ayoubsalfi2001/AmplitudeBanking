package org.example.accountdetails;

import org.example.accountdetails.Account.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Interface pour le service de détail des comptes Sopra Amplitude
 * Seulement les 2 méthodes du WSDL getAccountDetail
 */
@WebService(targetNamespace = "http://soprabanking.com/amplitude")
public interface GetAccountDetailService {

    /**
     * Récupérer le détail complet d'un compte
     */
    @WebMethod
    GetAccountDetailResponseFlow getAccountDetail(
            @WebParam(name = "parameters") GetAccountDetailRequestFlow request
    );

    /**
     * Vérifier le statut du service
     */
    @WebMethod
    GetStatusResponseFlow getStatus(
            @WebParam(name = "parameters") GetStatusRequestFlow request
    );
}