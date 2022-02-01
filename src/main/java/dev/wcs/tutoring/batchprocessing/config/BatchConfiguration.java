package dev.wcs.tutoring.batchprocessing.config;

import dev.wcs.tutoring.batchprocessing.JobCompletionNotificationListener;
import dev.wcs.tutoring.batchprocessing.model.Person;
import dev.wcs.tutoring.batchprocessing.processor.PersonItemProcessor;
import dev.wcs.tutoring.batchprocessing.processor.CountryEnricher;
import dev.wcs.tutoring.batchprocessing.processor.CovidStatisticsEnricher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

// tag::setup[]
@Configuration
@EnableBatchProcessing
@EnableTask
public class BatchConfiguration {

	private final JdbcTemplate jdbcTemplate;

	public BatchConfiguration(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	private void init() {
		jdbcTemplate.update("DROP TABLE people IF EXISTS");
		jdbcTemplate.update(
				"CREATE TABLE people  (" +
				"person_id BIGINT IDENTITY NOT NULL PRIMARY KEY," +
				"first_name VARCHAR(20)," +
				"last_name VARCHAR(20)," +
				"country VARCHAR(2)," +
				"capital VARCHAR(20)," +
				"population INTEGER, " +
				"active_covid_cases INTEGER" +
				")");
	}

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	// end::setup[]

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> csvReader() {
		return new FlatFileItemReaderBuilder<Person>()
				.name("personItemReader")
				.resource(new ClassPathResource("people.csv"))
				.delimited()
				.names(new String[]{"firstName", "lastName", "country"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
					setTargetType(Person.class);
				}})
				.build();
	}

	@Bean
	public JdbcCursorItemReader<Person> dbReader(DataSource dataSource) {
		JdbcCursorItemReader<Person> cursorItemReader = new JdbcCursorItemReader<>();

		cursorItemReader.setSql("SELECT first_name, last_name, country, capital, population, active_covid_cases FROM people");
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setRowMapper(
			(rs, rowNum) -> new Person(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getInt(6))
		);
		return cursorItemReader;
	}

	@Bean
	public PersonItemProcessor personItemProcessor() {
		return new PersonItemProcessor();
	}

	@Bean
	public CountryEnricher countryEnricher() {
		return new CountryEnricher();
	}

	@Bean
	public CovidStatisticsEnricher covidStatisticsEnricher() {
		return new CovidStatisticsEnricher();
	}

	@Bean
	public JdbcBatchItemWriter<Person> inserter(DataSource dataSource) {
		return
				new JdbcBatchItemWriterBuilder<Person>()
						.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
						.sql("INSERT INTO people (first_name, last_name, country, capital, population, active_covid_cases) VALUES (:firstName, :lastName, :country, :capital, :population, :activeCovidCases)")
						.dataSource(dataSource)
						.build();
	}

	@Bean
	public JdbcBatchItemWriter<Person> updater(DataSource dataSource) {
		return
				new JdbcBatchItemWriterBuilder<Person>()
						.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
						.sql("UPDATE people SET first_name=:firstName, last_name=:lastName, country=:country, capital=:capital, population=:population, active_covid_cases=:activeCovidCases WHERE first_name=:firstName AND last_name=:lastName")
						.dataSource(dataSource)
						.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step importCsv, Step enrichCountryInformation, Step enrichCovidStatistics) {
		return jobBuilderFactory.get("processPersonJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.start(importCsv)
			.next(enrichCountryInformation)
			.next(enrichCovidStatistics)
			.build();
	}

	@Bean
	public Step importCsv(JdbcBatchItemWriter<Person> inserter) {
		return stepBuilderFactory.get("importCsv")
				.<Person, Person> chunk(10)
				.reader(csvReader())
				.processor(personItemProcessor())
				.writer(inserter)
				.build();
	}

	@Bean
	public Step enrichCountryInformation(JdbcCursorItemReader<Person> reader, JdbcBatchItemWriter<Person> updater) {
		return stepBuilderFactory.get("enrichCountryInformation")
				.<Person, Person> chunk(10)
				.reader(reader)
				.processor(countryEnricher())
				.writer(updater)
				.build();
	}

	@Bean
	public Step enrichCovidStatistics(JdbcCursorItemReader<Person> reader, JdbcBatchItemWriter<Person> updater) {
		return stepBuilderFactory.get("enrichCovidStatistics")
				.<Person, Person> chunk(10)
				.reader(reader)
				.processor(covidStatisticsEnricher())
				.writer(updater)
				.build();
	}
	// end::jobstep[]
}
