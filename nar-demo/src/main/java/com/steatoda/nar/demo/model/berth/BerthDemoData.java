package com.steatoda.nar.demo.model.berth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.steatoda.nar.demo.model.boat.BoatDemoData;

public abstract class BerthDemoData {

	public static final List<String> KornatiBerthIds = IntStream.range(0, 100).mapToObj(i -> String.format("kb-%d", i)).collect(Collectors.toList());
	public static final List<String> PommeBerthIds = IntStream.range(0, 34).mapToObj(i -> String.format("pm-%d", i)).collect(Collectors.toList());

	static class Record {
		public Record(String id, String boatId) {
			this.id = id;
			this.boatId = boatId;
		}
		String id;
		String boatId;
	}

	public static List<Record> $() {
		
		if ($ == null) {
			
			$ = new ArrayList<>();
			
			// fill every other berth
			for (int i = 0; i < KornatiBerthIds.size(); ++i)
				$.add(new Record(
					KornatiBerthIds.get(i),
					i % 2 == 0 && i / 2 < BoatDemoData.KornatiFlotillaIds.size() ? BoatDemoData.KornatiFlotillaIds.get(i / 2) : null
				));
			
			for (int i = 0; i < PommeBerthIds.size(); ++i)
				$.add(new Record(
					PommeBerthIds.get(i),
					null
				));

		}

		return $;
		
	}
	
	private static List<Record> $ = null;

}
