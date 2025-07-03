package org.example.productsubscription;

import org.example.productsubscription.product.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Interface SIMPLE pour le service de souscriptions de produits
 * Seulement les 2 méthodes essentielles du WSDL
 */
@WebService(targetNamespace = "http://soprabanking.com/amplitude")
public interface GetProductSubscriptionListPortType {

    /**
     * Récupérer la liste des souscriptions de produits
     */
    @WebMethod
    GetProductSubscriptionListResponseFlow getProductSubscriptionList(
            @WebParam(name = "parameters") GetProductSubscriptionListRequestFlow request
    );

    /**
     * Vérifier le statut du service
     */
    @WebMethod
    GetStatusResponseFlow getStatus(
            @WebParam(name = "parameters") GetStatusRequestFlow request
    );
}