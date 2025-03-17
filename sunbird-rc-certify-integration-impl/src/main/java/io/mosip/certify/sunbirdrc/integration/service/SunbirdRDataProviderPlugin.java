package io.mosip.certify.sunbirdrc.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.sunbirdrc.integration.dto.RegistrySearchRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "SunbirdRDataProviderPlugin")
@Component
@Slf4j
public class SunbirdRDataProviderPlugin implements DataProviderPlugin {

    private final String FILTER_EQUALS_OPERATOR = "eq";

    @Value("${mosip.data-provider.url}")
    private String dataProviderUrl;

    @Value("${mosip.data-provider.entity-id-field}")
    private String entityIdField;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        try {
            String entityId = (String) identityDetails.get("sub");
            log.debug("Fetching data for entityId: {}", entityId);

            if (entityId != null) {
                RegistrySearchRequestDto registrySearchRequestDto = new RegistrySearchRequestDto();
                registrySearchRequestDto.setOffset(0);
                registrySearchRequestDto.setLimit(1);

                Map<String, Map<String, String>> filter = new HashMap<>();
                Map<String, String> idFilter = new HashMap<>();
                idFilter.put(FILTER_EQUALS_OPERATOR, entityId);
                filter.put(entityIdField, idFilter);

                registrySearchRequestDto.setFilters(filter);

                RequestEntity<RegistrySearchRequestDto> requestEntity = RequestEntity.post(
                        UriComponentsBuilder.fromUriString(dataProviderUrl).build().toUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(registrySearchRequestDto);

                ResponseEntity<List<Map<String, Object>>> responseEntity = restTemplate.exchange(
                        requestEntity, new ParameterizedTypeReference<List<Map<String, Object>>>() {});

                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    List<Map<String, Object>> responseList = responseEntity.getBody();

                    if (!responseList.isEmpty()) {
                        return new JSONObject(responseList.get(0));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch json data from data provider plugin", e);
            throw new DataProviderExchangeException("ERROR_FETCHING_IDENTITY_DATA");
        }

        throw new DataProviderExchangeException("No Data Found");
    }
}
