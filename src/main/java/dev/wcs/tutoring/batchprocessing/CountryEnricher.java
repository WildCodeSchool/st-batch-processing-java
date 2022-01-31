package dev.wcs.tutoring.batchprocessing;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import net.minidev.json.JSONArray;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class CountryEnricher implements ItemProcessor<Person, Person> {

    private String countryAPI = "https://restcountries.com/v3.1/alpha/";

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
