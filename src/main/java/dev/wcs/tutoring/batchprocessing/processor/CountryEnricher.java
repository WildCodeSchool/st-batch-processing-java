package dev.wcs.tutoring.batchprocessing.processor;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.wcs.tutoring.batchprocessing.model.Person;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import net.minidev.json.JSONArray;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class CountryEnricher implements ItemProcessor<Person, Person> {

    @Value("${country.enricher.url}")
    private String countryAPI;

    @Override
    public Person process(Person item) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest
            .get(countryAPI + "{country}")
            .routeParam("country", item.getCountry()).asJson();
        String jsonString = jsonResponse.getBody().toString();
        DocumentContext jsonContext = JsonPath.parse(jsonString);
        List<Object> jsonpathCreatorName = jsonContext.read("$[*]['population']");
        Integer[] populationArray = jsonpathCreatorName.toArray(new Integer[0]);
        item.setPopulation(populationArray[0]);
        jsonpathCreatorName = jsonContext.read("$[*]['capital']");
        Object[] capArray = jsonpathCreatorName.toArray(new Object[0]);
        item.setCapital(((JSONArray)capArray[0]).toJSONString());
        return item;
    }
}
