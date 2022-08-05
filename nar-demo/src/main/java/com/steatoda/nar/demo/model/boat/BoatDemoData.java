package com.steatoda.nar.demo.model.boat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.steatoda.nar.demo.model.marina.MarinaDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoData;

public abstract class BoatDemoData {

	public static final String WandererId = "wanderer";
	public static final String QueenAnnesRevengeId = "queen-anne-s-revenge";
	public static final String AlphaId = "alpha";
	public static final List<String> KornatiFlotillaIds = IntStream.range(0, 56).mapToObj(i -> String.format("kornati-%d", i)).collect(Collectors.toList());

	static class Record {
		public Record(String id, String name, String type, String homeportId, String skipperId, List<String> crewIds) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.homeportId = homeportId;
			this.skipperId = skipperId;
			this.crewIds = crewIds;
		}
		String id;
		String name;
		String type;
		String homeportId;
		String skipperId;
		List<String> crewIds;
	}

	public static List<Record> $() {
		
		if ($ == null) {
			
			$ = new ArrayList<>();
			
			$.add(new Record(
				WandererId,
				"Wanderer",
				"Motor yacht",
				MarinaDemoData.StPommeDeTerreId,
				PersonDemoData.RonId,
				new ArrayList<>(PersonDemoData.WandererCrewIds)
			));

			$.add(new Record(
				QueenAnnesRevengeId,
				"Queen Anne's Revenge",
				"Sailing yacht",
				MarinaDemoData.StPommeDeTerreId,
				PersonDemoData.PirateId,
				new ArrayList<>(PersonDemoData.QueenAnnesRevengeCrewIds)
			));

			$.add(new Record(
				AlphaId,
				"Alpha",
				"Sailing yacht",
				MarinaDemoData.KornatiId,
				PersonDemoData.JohnId,
				new ArrayList<>(PersonDemoData.AlphaCrewIds)
			));

			for (int i = 0; i < KornatiFlotillaIds.size(); ++i)
				$.add(new Record(
					KornatiFlotillaIds.get(i),
					String.format("Kornati %02d", i),
					"Motor boat",
					MarinaDemoData.KornatiId,
					null,
					Collections.emptyList()
				));

		}

		return $;
		
	}

	private static List<Record> $ = null;
	
}
