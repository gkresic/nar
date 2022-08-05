package com.steatoda.nar.model.carrier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.steatoda.nar.NarGraph;
import com.steatoda.nar.demo.model.person.Person;
import com.steatoda.nar.demo.model.person.PersonDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoService;
import com.steatoda.nar.demo.model.person.PersonService;

public class CarrierDemoService implements CarrierService {

	@Override
	public Carrier get(String id, NarGraph<Carrier.Field> graph) {

		if (!"enterprise".equals(id))
			return null;
		
		if (graph.isEmpty())
			return Carrier.ref(id);
		
		Carrier carrier = new Carrier();

		carrier.setId(id);
		
		for (Carrier.Field field : graph) {

			NarGraph<Person.Field> personGraph = graph.getGraph(field, Person.Field.class);

			switch (field) {
			
				case name:
					carrier.setName("Enterprise");
					break;
					
				case entity:
					carrier.setEntity(personService.get(PersonDemoData.RonId, personGraph));
					break;
					
				case list:
					carrier.setList(buildList(personGraph));
					break;
					
				case set:
					carrier.setSet(buildSet(personGraph));
					break;
					
				case map:
					carrier.setMap(buildMap(personGraph));
					break;
					
				case listlist:
					List<List<Person>> listlist = new ArrayList<>();
					listlist.add(buildList(personGraph));
					listlist.add(buildList(personGraph));
					listlist.add(buildList(personGraph));
					carrier.setListList(listlist);
					break;
					
				case setset:
					Set<Set<Person>> setset = new HashSet<>();
					setset.add(buildSet(personGraph));
					setset.add(buildSet(personGraph));
					setset.add(buildSet(personGraph));
					carrier.setSetSet(setset);
					break;
					
				case maplist:
					Map<String, List<Person>> maplist = new HashMap<>();
					maplist.put("first", buildList(personGraph));
					maplist.put("second", buildList(personGraph));
					maplist.put("third", buildList(personGraph));
					carrier.setMapList(maplist);
					break;
					
				case mapset:
					Map<String, Set<Person>> mapset = new HashMap<>();
					mapset.put("first", buildSet(personGraph));
					mapset.put("second", buildSet(personGraph));
					mapset.put("third", buildSet(personGraph));
					carrier.setMapSet(mapset);
					break;
					
				case mapmap:
					Map<String, Map<String, Person>> mapmap = new HashMap<>();
					mapmap.put("first", buildMap(personGraph));
					mapmap.put("second", buildMap(personGraph));
					mapmap.put("third", buildMap(personGraph));
					carrier.setMapMap(mapmap);
					break;

				case mapentitylist:
					Map<Person, List<Person>> mapentitylist = new HashMap<>();
					mapentitylist.put(personService.get(PersonDemoData.RonId, personGraph), buildList(personGraph));
					mapentitylist.put(personService.get(PersonDemoData.PirateId, personGraph), buildList(personGraph));
					mapentitylist.put(personService.get(PersonDemoData.JohnId, personGraph), buildList(personGraph));
					carrier.setMapEntityList(mapentitylist);
					break;

			}
			
		}

		return carrier;
		
	}

	private List<Person> buildList(NarGraph<Person.Field> fields) {
		List<Person> persons = new ArrayList<>();
		persons.add(personService.get(PersonDemoData.RonId, fields));
		persons.add(personService.get(PersonDemoData.PirateId, fields));
		persons.add(personService.get(PersonDemoData.JohnId, fields));
		return persons;
	}
	
	private Set<Person> buildSet(NarGraph<Person.Field> fields) {
		Set<Person> persons = new HashSet<>();
		persons.add(personService.get(PersonDemoData.RonId, fields));
		persons.add(personService.get(PersonDemoData.PirateId, fields));
		persons.add(personService.get(PersonDemoData.JohnId, fields));
		return persons;
	}
	
	private Map<String, Person> buildMap(NarGraph<Person.Field> fields) {
		Map<String, Person> persons = new HashMap<>();
		persons.put("ron", personService.get(PersonDemoData.RonId, fields));
		persons.put("pirate", personService.get(PersonDemoData.PirateId, fields));
		persons.put("john", personService.get(PersonDemoData.JohnId, fields));
		return persons;
	}
	
	@SuppressWarnings("unused")
	private static final Logger Log = LoggerFactory.getLogger(CarrierDemoService.class);

	private final PersonService personService = new PersonDemoService();

}
