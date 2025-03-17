package io.mosip.certify.mock.integration.service;
import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.util.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
	@@ -34,6 +39,8 @@ public class MockCSVDataProviderPlugin implements DataProviderPlugin {
    private CSVReader csvReader;
    @Value("${mosip.certify.mock.data-provider.csv-registry-uri}")
    private String csvRegistryURI;
    @Value("${mosip.certify.mock.data-provider.csv.identifier-column}")
    private String identifierColumn;
    @Value("#{'${mosip.certify.mock.data-provider.csv.data-columns}'.split(',')}")
	@@ -79,8 +86,31 @@ public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProv
        try {
            String individualId = (String) identityDetails.get("sub");
            if (individualId != null) {
                JSONObject jsonRes = csvReader.getJsonObjectByIdentifier(individualId);
                return jsonRes;
            }
        } catch (Exception e) {
            log.error("Failed to fetch json data for from data provider plugin", e);
            throw new DataProviderExchangeException("ERROR_FETCHING_IDENTITY_DATA");
        }
        throw new DataProviderExchangeException("No Data Found");
    }
}
