package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedResponseDto;
import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.paging.model.PageModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class OnboardInstitutionEntityToPaSummaryExtendedDtoMapperTest {
    private OnboardInstitutionEntity entity;


    @BeforeEach
    void setUp() {
        entity = new OnboardInstitutionEntity();
        entity.setPk("PA123");
        entity.setDescription("Ministero della Salute");
    }


    @Test
    void testToDto() {
        // When
        PaSummaryExtendedDto dto = OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toDto(entity);

        // Then
        Assertions.assertNotNull(dto);
        assertEquals(entity.getInstitutionId(), dto.getId());
        assertEquals(entity.getDescription(), dto.getName());
        Assertions.assertNotNull(dto.getChildrenList());
        assertTrue(dto.getChildrenList().isEmpty());
    }

    @Test
    void toPageableResponseExtended() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<PaSummaryExtendedDto> content = new ArrayList<>();
        PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
        dto.setId("PA456");
        dto.setName("Agenzia delle Entrate");
        content.add(dto);

        PageModel<PaSummaryExtendedDto> pageModel = PageModel.builder(content, pageable);

        // When
        PaSummaryExtendedResponseDto responseDto = OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toPageableResponseExtended(pageModel);

        // Then
        Assertions.assertNotNull(responseDto);
        assertEquals(1, responseDto.getNumberOfElements());
        Assertions.assertFalse(responseDto.getEmpty());
        assertEquals("PA456", responseDto.getContent().get(0).getId());
        assertEquals("Agenzia delle Entrate", responseDto.getContent().get(0).getName());
    }

    @Test
    void toPageableResponseExtendedWithEmptyId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<PaSummaryExtendedDto> content = new ArrayList<>();
        PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
        dto.setChildrenList(new ArrayList<>());
        content.add(dto);

        PageModel<PaSummaryExtendedDto> pageModel = PageModel.builder(content, pageable);

        // When
        PaSummaryExtendedResponseDto responseDto = OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toPageableResponseExtended(pageModel);

        // Then
        Assertions.assertNotNull(responseDto);
        Assertions.assertNotNull(responseDto.getContent());
        Assertions.assertEquals(1, responseDto.getContent().size());
        Assertions.assertNotNull(responseDto.getContent().get(0).getChildrenList());
        Assertions.assertTrue(responseDto.getContent().get(0).getChildrenList().isEmpty());
    }

    @Test
    void toPaginationPaSummaryExtended() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<PaSummaryExtendedDto> list = new ArrayList<>();

        PaSummaryExtendedDto dto1 = new PaSummaryExtendedDto();
        dto1.setId("PA123");
        dto1.setName("Ministero della Salute");
        list.add(dto1);

        PaSummaryExtendedDto dto2 = new PaSummaryExtendedDto();
        dto2.setId("PA124");
        dto2.setName("Agenzia delle Entrate");
        list.add(dto2);

        // Debug prima della conversione
        list.forEach(dto -> System.out.println("ID: " + dto.getId() + ", Name: " + dto.getName()));

        // When
        PageModel<PaSummaryExtendedDto> pageModel = OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toPaginationPaSummaryExtended(pageable, list);

        // Then
        Assertions.assertNotNull(pageModel);
        Assertions.assertEquals(2, pageModel.getNumberOfElements(), "Il numero di elementi non corrisponde a quello atteso!");
        Assertions.assertEquals("PA123", pageModel.getContent().get(0).getId());
        Assertions.assertEquals("Ministero della Salute", pageModel.getContent().get(0).getName());
        Assertions.assertEquals("PA124", pageModel.getContent().get(1).getId());
        Assertions.assertEquals("Agenzia delle Entrate", pageModel.getContent().get(1).getName());
    }
}