package it.pagopa.pn.external.registries.dto.delivery;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class NotificationInt {
    private String iun;
    private String subject;
    private String senderDenomination;
    private List<NotificationRecipientInt> recipients ;
}
