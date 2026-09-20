package org.greenstone.backend.publiccontent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GoogleReviewsService {

    private static final String FIELD_MASK = "rating,userRatingCount,reviews,googleMapsUri";

    private final RestClient restClient;
    private final String apiKey;
    private final String placeId;

    public GoogleReviewsService(
            @Value("${app.google-places.api-key:}") String apiKey,
            @Value("${app.google-places.place-id:}") String placeId
    ) {
        this.restClient = RestClient.create("https://places.googleapis.com/v1");
        this.apiKey = apiKey;
        this.placeId = placeId;
    }

    public PublicReviewsResponse reviews() {
        if (apiKey.isBlank() || placeId.isBlank()) {
            throw new GoogleReviewsUnavailableException("Google reviews are not configured.");
        }

        try {
            var place = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/places/{placeId}")
                                .queryParam("languageCode", "en")
                                .queryParam("regionCode", "NZ")
                                .build(placeId))
                        .header("X-Goog-Api-Key", apiKey)
                        .header("X-Goog-FieldMask", FIELD_MASK)
                        .header(HttpHeaders.ACCEPT, "application/json")
                        .retrieve()
                        .body(GooglePlaceResponse.class);

            if (place == null || place.rating() == null || place.userRatingCount() == null) {
                throw new GoogleReviewsUnavailableException("Google returned incomplete review information.");
            }

            return new PublicReviewsResponse(
                    place.rating(),
                    place.userRatingCount(),
                    place.googleMapsUri(),
                    place.reviews() == null ? List.of() : place.reviews().stream()
                            .filter(review -> review.text() != null && !review.text().text().isBlank())
                            .map(review -> new PublicReviewResponse(
                                    review.authorAttribution() == null ? "Google customer" : review.authorAttribution().displayName(),
                                    review.authorAttribution() == null ? null : review.authorAttribution().uri(),
                                    review.authorAttribution() == null ? null : review.authorAttribution().photoUri(),
                                    review.rating(),
                                    review.text().text(),
                                    review.relativePublishTimeDescription()
                            ))
                            .toList()
            );
        } catch (GoogleReviewsUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new GoogleReviewsUnavailableException("Google reviews are temporarily unavailable.", exception);
        }
    }

    private record GooglePlaceResponse(
            Double rating,
            Integer userRatingCount,
            String googleMapsUri,
            List<GoogleReview> reviews
    ) {
    }

    private record GoogleReview(
            GoogleAuthorAttribution authorAttribution,
            Double rating,
            GoogleLocalizedText text,
            String relativePublishTimeDescription
    ) {
    }

    private record GoogleAuthorAttribution(String displayName, String uri, String photoUri) {
    }

    private record GoogleLocalizedText(String text, String languageCode) {
    }
}
