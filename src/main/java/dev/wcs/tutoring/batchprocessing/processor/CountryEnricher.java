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
    public Person process(Person item) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get(countryAPI + item.getCountry()).asJson();
        String jsonString = jsonResponse.getBody().toString();
        DocumentContext jsonContext = JsonPath.parse(jsonString);
        // read population
        List<Object> population = jsonContext.read("$[*]['population']");
        Integer[] populationArray = population.toArray(new Integer[0]);
        item.setPopulation(populationArray[0]);
        // read capital
        List<Object> capital = jsonContext.read("$[*]['capital']");
        JSONArray[] capArray = capital.toArray(new JSONArray[0]);
        item.setCapital(capArray[0].get(0).toString());
        return item;
    }
}
