package com.steatoda.nar.internal;

import com.steatoda.nar.NarGraph;

import java.text.ParseException;
import java.util.LinkedHashMap;

/**
 * <p>Temporary storage for {@link NarGraph}s parsed from strings.</p>
 *
 * <p><b>INTERNAL</b></p>
 */
public class RecursiveStringMap extends LinkedHashMap<RecursiveStringMap.Key, RecursiveStringMap> {

	/** Holds fields names paired with their offset in original string (for better error reporting) */
	public static class Key {
		public Key(String name, int offset) {
			this.name = name;
			this.offset = offset;
		}
		public final String name;
		public final int offset;
	}
	
	public static RecursiveStringMap of(String value) throws ParseException {

		RecursiveStringMap map = new RecursiveStringMap();

		if (value == null || value.trim().isEmpty())
			return map;
		
		// recursively parse to string hierarchy
		parse(map, value, 0, false);

		return map;

	}

	private static int parse(RecursiveStringMap map, String value, int start, boolean subfields) throws ParseException {

		int end;
		
		while (true) {
		
			// parse field name
			end = start;
			char c = 0;
			while (true) {
				if (end == value.length())
					break;
				c = value.charAt(end);
				if (start == end && !isFieldIdentifierStart(c) || !isFieldIdentifierPart(c))
					break;
				++end;
			}

			if (start == end)
				throw new ParseException("Zero-length field name", start);
			
			String field = value.substring(start, end);
			
			// parse (optional) subset
			if (c == '{') {
				RecursiveStringMap subset = new RecursiveStringMap();
				end = parse(subset, value, end + 1, true);
				if (end == value.length())
					throw new ParseException("Reached end-of-line before subfields declaration ended (start was at " + start + ")", end);
				else if (value.charAt(end) != '}')
					throw new ParseException("Unterminated subfields declaration (start was at " + start + ")", end);
				++end;
				if (end < value.length())
					c = value.charAt(end);
				map.put(new Key(field, start), subset);
			} else {
				map.put(new Key(field, start), null);
			}

			if (end == value.length())
				break;	// reached end of value
			else if (c == ',')
				start = end + 1;	// reached end of field (WITHOUT subfields)
			else if (subfields && c == '}')
				break;	// reached end of subfields
			else
				throw new ParseException("Illegal character: '" + c + "'", end);

		}
		
		return end;

	}

	private static boolean isFieldIdentifierStart(char c) {
		return Character.isLetter(c) || c == '$' || c == '_';
	}
	
	private static boolean isFieldIdentifierPart(char c) {
		return isFieldIdentifierStart(c) || Character.isDigit(c);
	}
	
	private static final long serialVersionUID = 1L;

}
