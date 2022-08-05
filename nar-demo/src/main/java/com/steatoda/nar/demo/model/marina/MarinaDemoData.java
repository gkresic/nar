package com.steatoda.nar.demo.model.marina;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.steatoda.nar.demo.model.berth.BerthDemoData;
import com.steatoda.nar.demo.model.person.PersonDemoData;

public abstract class MarinaDemoData {

	public static final String KornatiId = "kornati";
	public static final String StPommeDeTerreId = "pomme";

	static class Record {
		public Record(String id, String name, String managerId, Double latitute, Double longitude, List<String> berthIds, Integer[][] depths) {
			this.id = id;
			this.name = name;
			this.managerId = managerId;
			this.latitute = latitute;
			this.longitude = longitude;
			this.berthIds = berthIds;
			this.depths = depths;
		}
		String id;
		String name;
		String managerId;
		Double latitute;
		Double longitude;
		List<String> berthIds;
		Integer[][] depths;
	}

	public static List<Record> $() {
		
		if ($ == null) {
			
			$ = new ArrayList<>();
			
			$.add(new Record(
				KornatiId,
				"Kornati",
				PersonDemoData.CapoId,
				43.9409,
				15.4430,
				new ArrayList<>(BerthDemoData.KornatiBerthIds),
				randomDepths(500, 750)
			));
			
			$.add(new Record(
				StPommeDeTerreId,
				"St. Pomme de Terre",
				PersonDemoData.CapoId,
				10.6396,
				-61.4998,
				new ArrayList<>(BerthDemoData.PommeBerthIds),
				randomDepths(123, 456)
			));
			
		}

		return $;
		
	}

	private static List<Record> $ = null;

	private static Integer[][] randomDepths(int width, int height) {
		Integer[][] depths = new Integer[width][height];
		for (int x = 0; x < width; ++x)
			for (int y = 0; y < height; ++y)
				depths[x][y] = Optional.of(Math.random() * 100).map(depth -> depth > 90 ? null : depth.intValue()).orElse(null);
		return depths;
	}

}
