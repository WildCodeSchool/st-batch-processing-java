package dev.wcs.tutoring.batchprocessing.processor;

import dev.wcs.tutoring.batchprocessing.model.Person;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getFirstName().toUpperCase();
		final String lastName = person.getLastName().toUpperCase();
		final String country = person.getCountry().toUpperCase();

		final Person transformedPerson = new Person(firstName, lastName, country);
		log.info("Processed (" + person + ") into (" + transformedPerson + ")");

		return transformedPerson;
	}

}
