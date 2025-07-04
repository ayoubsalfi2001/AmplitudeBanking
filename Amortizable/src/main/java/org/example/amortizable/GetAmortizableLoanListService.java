


import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import org.example.amortizable.credit.GetAmortizableLoanListRequestFlow;
import org.example.amortizable.credit.GetAmortizableLoanListResponseFlow;
import org.example.amortizable.credit.GetStatusRequestFlow;
import org.example.amortizable.credit.GetStatusResponseFlow;

/**
 * Interface pour le service de prêts amortissables Sopra Amplitude
 */
@WebService(targetNamespace = "http://soprabanking.com/amplitude")
public interface GetAmortizableLoanListService {

    /**
     * Récupérer la liste des prêts amortissables selon des critères
     */
    @WebMethod
    GetAmortizableLoanListResponseFlow getAmortizableLoanList(
            @WebParam(name = "parameters") GetAmortizableLoanListRequestFlow request
    );

    /**
     * Vérifier le statut du service
     */
    @WebMethod
    GetStatusResponseFlow getStatus(
            @WebParam(name = "parameters") GetStatusRequestFlow request
    );
}