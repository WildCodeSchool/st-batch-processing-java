package dev.wcs.tutoring.batchprocessing.processor;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.wcs.tutoring.batchprocessing.model.Person;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class CovidStatisticsEnricher implements ItemProcessor<Person, Person> {

    private String covidAPI = "https://api.covid19api.com/dayone/country/";

    @Override
    public Person process(Person item) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest
            .get(covidAPI + "{country}")
            .routeParam("country", item.getCountry()).asJson();
        String jsonString = jsonResponse.getBody().toString();
        DocumentContext jsonContext = JsonPath.parse(jsonString);
        List<Object> jsonpathCreatorName = jsonContext.read("$[*]['Confirmed']");
        Integer[] casesArray = jsonpathCreatorName.toArray(new Integer[0]);
        item.setActiveCovidCases(casesArray[casesArray.length-1]);
        return item;
    }
}
