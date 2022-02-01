package dev.wcs.tutoring.batchprocessing.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Person {

	private @NonNull String firstName;
	private @NonNull String lastName;
	private @NonNull String country;
	private String capital;
	private Integer population;
	private Integer activeCovidCases;

}
