package controllers;

import org.json.JSONObject;
import views.MainView;
import views.ResultsView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SearchController {
    private static final String SEARCH_URL = "https://cs.csub.edu/~paul/3390/lab08/search.php";
    private final HttpClient http = HttpClient.newHttpClient();

    public SearchController(MainView view) {

        view.attachSearchListener(e -> {
            String searchTerm = view.getSearchTerm().trim();
            String searchText = view.getSearchText().trim();

            if (searchTerm.isEmpty() || searchText.isEmpty()) {
                view.displayError("Please enter both a search term and search text.");
                return;
            }

            try {
                String html = postSearch(searchTerm, searchText);

                ResultsView resultsView = new ResultsView("Search", html);
                new ResultsController(resultsView);
                resultsView.setVisible(true);
            } catch (RuntimeException ex) {
                view.displayError("Unable to perform search!");
            }
        });
    }

    private String postSearch(String term, String text) {
        try {
            JSONObject postBody = new JSONObject();
            postBody.put("search", term);
            postBody.put("text", text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SEARCH_URL))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "text/html")
                    .POST(HttpRequest.BodyPublishers.ofString(postBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Invalid server request");
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}