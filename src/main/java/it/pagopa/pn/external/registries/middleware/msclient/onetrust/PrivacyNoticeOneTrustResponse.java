package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

public record PrivacyNoticeOneTrustResponse(String createdDate, String id, String lastPublishedDate, String organizationId,
                                            String responsibleUserId, Version version) {


    public record Version(String id, String name, String publishedDate, String status, Integer version) {}
}
