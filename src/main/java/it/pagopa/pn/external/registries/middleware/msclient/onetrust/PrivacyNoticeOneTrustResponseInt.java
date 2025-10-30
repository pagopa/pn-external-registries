package it.pagopa.pn.external.registries.middleware.msclient.onetrust;

import java.util.List;

public record PrivacyNoticeOneTrustResponseInt(String guid, String name, String description, OrgGroup orgGroup,
                                               List<PolicyUser> owners, List<PolicyUser> approvers, String effectiveDate,
                                               String expirationDate, int id, String defaultLanguageCode, List<PolicyVersion> versions) {

    public record OrgGroup(String id, String name) {}

    public record PolicyUser(String id, String name, String email) {}

    public record PolicyVersion(String id, String attachmentId, String descriptionOfChanges, String publishedDate,
                                String createdDate, String versionStatus, String policyContentType,
                                List<PolicySection> sections, int majorVersion, int minorVersion) {}

    public record PolicySection(String name, String description, String content, String sectionType, int order) {}

}
