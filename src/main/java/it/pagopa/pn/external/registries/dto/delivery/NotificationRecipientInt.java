package it.pagopa.pn.external.registries.dto.delivery;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationRecipientInt {
    private String internalId;
}
