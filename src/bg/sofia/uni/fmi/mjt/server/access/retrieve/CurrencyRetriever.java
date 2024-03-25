package bg.sofia.uni.fmi.mjt.server.access.retrieve;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.CurrencyRetrievalException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CurrencyRetriever {
    private static final Gson GSON = new Gson();
    private static final int SUCCESS = 200;

    public static List<Currency> retrieveResources(HttpClient httpClient) {

        HttpResponse<String> response;

        UriBuilder uriBuilder = new UriBuilder();
        URI requestUri = uriBuilder.buildUri();
        try {
            HttpRequest httpRequest =
                HttpRequest.newBuilder().uri(requestUri).build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new CurrencyRetrievalException("Could not retrieve currencies.", e);
        }

        TypeToken<List<Currency>> list = new TypeToken<>() { };

        if (response.statusCode() == SUCCESS) {
            return GSON.fromJson(response.body(), list);

        } else {
            throw new CurrencyRetrievalException("statusCode: " + response.statusCode());
        }

    }
}
