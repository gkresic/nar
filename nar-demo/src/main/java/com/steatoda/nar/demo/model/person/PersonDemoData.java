package com.steatoda.nar.demo.model.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Sets;

import com.steatoda.nar.demo.model.boat.BoatDemoData;

/** Sample persons */
public abstract class PersonDemoData {

	public static final String CapoId = "capo";
	public static final String RonId = "ron";
	public static final String PirateId = "pirate";
	public static final String JohnId = "john";

	public static final List<String> WandererCrewIds = IntStream.range(0, 7).mapToObj(i -> String.format("wanderer-sailor-%d", i)).collect(Collectors.toList());
	public static final List<String> QueenAnnesRevengeCrewIds = IntStream.range(0, 300).mapToObj(i -> String.format("revenger-%d", i)).collect(Collectors.toList());
	public static final List<String> AlphaCrewIds = IntStream.range(0, 4).mapToObj(i -> String.format("alpha-sailor-%d", i)).collect(Collectors.toList());

	static class Record {
		public Record(String id, String name, String email, Set<String> permissions, String boatId) {
			this.id = id;
			this.name = name;
			this.email = email;
			this.permissions = permissions;
			this.boatId = boatId;
		}
		public String getId() { return id; }
		String id;
		String name;
		String email;
		Set<String> permissions;
		String boatId;
	}
	
	public static List<Record> $() {
		
		if ($ == null) {
			
			$ = new ArrayList<>();
			
			$.add(new Record(
				CapoId,
				"Capo Di Tutti Capi",
				"capo@cosa-nostra.org",
				Sets.newHashSet("admin"),
				null
			));
			
			$.add(new Record(
				RonId,
				"Captain Ron",
				"ron@pirates.org",
				Sets.newHashSet("admin"),
				BoatDemoData.WandererId
			));
			
			$.add(new Record(
				PirateId,
				"Blackbeard",
				"blackbeard@caribbean.com",
				Sets.newHashSet("read", "write", "steal"),
				BoatDemoData.QueenAnnesRevengeId
			));
			
			$.add(new Record(
				JohnId,
				"John Doe",
				"john.doe@acme.com",
				Sets.newHashSet("read", "write"),
				BoatDemoData.AlphaId
			));
			
			for (int i = 0; i < WandererCrewIds.size(); ++i)
				$.add(new Record(
					WandererCrewIds.get(i),
					String.format("Wanderer %02d", i),
					String.format("wanderer.%d@wanderer.com", i),
					Sets.newHashSet("read"),
					BoatDemoData.WandererId
				));

			for (int i = 0; i < QueenAnnesRevengeCrewIds.size(); ++i)
				$.add(new Record(
					QueenAnnesRevengeCrewIds.get(i),
					String.format("Revenger %02d", i),
					String.format("revenger.%d@caribbean.com", i),
					Sets.newHashSet("read"),
					BoatDemoData.QueenAnnesRevengeId
				));

			for (int i = 0; i < AlphaCrewIds.size(); ++i)
				$.add(new Record(
					AlphaCrewIds.get(i),
					String.format("Alpha Crewman %02d", i),
					String.format("alpha.%d@alpha.com", i),
					Sets.newHashSet("read"),
					BoatDemoData.AlphaId
				));

		}

		return $;
		
	}

	private static List<Record> $ = null;

}
