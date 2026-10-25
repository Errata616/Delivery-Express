package vn.uteexpress.routing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Undirected weighted graph of delivery districts, loaded from two CSV files.
 * Plain Java (no Spring) so it can be unit-tested without starting the app.
 */
public final class DistrictGraph {

	public record District(String code, String name) {
	}

	/** hops = number of links travelled, totalWeight = sum of link weights (km if you fill them in). */
	public record Route(List<District> path, int hops, double totalWeight) {
	}

	// "Quan 1", "Q 1", "District 1" -> "1"
	private static final String[] PREFIXES = { "quan ", "huyen ", "district ", "q " };

	private final Map<String, District> byCode = new LinkedHashMap<>();
	private final Map<String, String> aliasToCode = new HashMap<>();
	private final Map<String, Map<String, Double>> adjacency = new HashMap<>();

	private DistrictGraph() {
	}

	public static DistrictGraph load(InputStream districtsCsv, InputStream linksCsv) throws IOException {
		DistrictGraph graph = new DistrictGraph();
		graph.readDistricts(districtsCsv);
		graph.readLinks(linksCsv);
		return graph;
	}

	/** Lowercase, strip accents, turn punctuation into single spaces. */
	public static String normalize(String text) {
		if (text == null) {
			return "";
		}
		String lower = text.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
		String plain = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return plain.replaceAll("[^a-z0-9]+", " ").trim();
	}

	public List<District> districts() {
		return List.copyOf(byCode.values());
	}

	public Optional<District> resolve(String text) {
		String key = normalize(text);
		if (key.isEmpty()) {
			return Optional.empty();
		}

		String code = aliasToCode.get(key);
		if (code == null) {
			for (String prefix : PREFIXES) {
				if (key.startsWith(prefix)) {
					code = aliasToCode.get(key.substring(prefix.length()));
					if (code != null) {
						break;
					}
				}
			}
		}
		return Optional.ofNullable(code).map(byCode::get);
	}

	public Optional<Route> shortestPath(String from, String to) {
		Optional<District> start = resolve(from);
		Optional<District> goal = resolve(to);
		if (start.isEmpty() || goal.isEmpty()) {
			return Optional.empty();
		}
		return shortestPath(start.get(), goal.get());
	}

	public Optional<Route> shortestPath(String from, String to, Set<String> allowedDistrictCodes) {
		Optional<District> start = resolve(from);
		Optional<District> goal = resolve(to);
		if (start.isEmpty() || goal.isEmpty()) {
			return Optional.empty();
		}
		return shortestPath(start.get(), goal.get(), allowedDistrictCodes);
	}

	public Optional<Route> shortestPath(District start, District goal) {
		return shortestPath(start, goal, byCode.keySet());
	}

	/** Finds the shortest route using only districts in {@code allowedDistrictCodes}. */
	public Optional<Route> shortestPath(District start, District goal, Set<String> allowedDistrictCodes) {
		if (start == null || goal == null || allowedDistrictCodes == null
				|| !allowedDistrictCodes.contains(start.code()) || !allowedDistrictCodes.contains(goal.code())) {
			return Optional.empty();
		}

		Map<String, Double> dist = new HashMap<>();
		Map<String, String> prev = new HashMap<>();
		PriorityQueue<Map.Entry<String, Double>> queue = new PriorityQueue<>(Map.Entry.comparingByValue());

		dist.put(start.code(), 0.0);
		queue.add(Map.entry(start.code(), 0.0));

		while (!queue.isEmpty()) {
			Map.Entry<String, Double> current = queue.poll();
			String code = current.getKey();

			if (current.getValue() > dist.getOrDefault(code, Double.MAX_VALUE)) {
				continue; // stale queue entry
			}
			if (code.equals(goal.code())) {
				break;
			}
			for (Map.Entry<String, Double> edge : adjacency.getOrDefault(code, Map.of()).entrySet()) {
				if (!allowedDistrictCodes.contains(edge.getKey())) {
					continue;
				}
				double candidate = current.getValue() + edge.getValue();
				if (candidate < dist.getOrDefault(edge.getKey(), Double.MAX_VALUE)) {
					dist.put(edge.getKey(), candidate);
					prev.put(edge.getKey(), code);
					queue.add(Map.entry(edge.getKey(), candidate));
				}
			}
		}

		if (!dist.containsKey(goal.code())) {
			return Optional.empty();
		}

		LinkedList<District> path = new LinkedList<>();
		for (String at = goal.code(); at != null; at = prev.get(at)) {
			path.addFirst(byCode.get(at));
		}
		return Optional.of(new Route(List.copyOf(path), path.size() - 1, dist.get(goal.code())));
	}

	public boolean areAdjacent(District first, District second) {
		return first != null && second != null
				&& adjacency.getOrDefault(first.code(), Map.of()).containsKey(second.code());
	}

	// ---------- loading ----------

	private void readDistricts(InputStream in) throws IOException {
		for (Row row : rows(in, "code")) {
			if (row.cells.length < 2) {
				throw bad("districts.csv", row.line, "expected: code,name[,aliases]");
			}
			String code = row.cells[0].toUpperCase(Locale.ROOT);
			String name = row.cells[1];
			if (code.isEmpty() || name.isEmpty()) {
				throw bad("districts.csv", row.line, "code and name must not be empty");
			}
			if (byCode.putIfAbsent(code, new District(code, name)) != null) {
				throw bad("districts.csv", row.line, "duplicate code " + code);
			}

			addAlias(code, code, row.line);
			addAlias(code, name, row.line);
			if (row.cells.length > 2) {
				for (String alias : row.cells[2].split("\\|")) {
					addAlias(code, alias, row.line);
				}
			}
		}
		if (byCode.isEmpty()) {
			throw new IllegalStateException("districts.csv has no districts");
		}
	}

	private void addAlias(String code, String alias, int line) {
		String key = normalize(alias);
		if (key.isEmpty()) {
			return;
		}
		String previous = aliasToCode.putIfAbsent(key, code);
		if (previous != null && !previous.equals(code)) {
			throw bad("districts.csv", line, "'" + alias + "' is used by both " + previous + " and " + code);
		}
	}

	private void readLinks(InputStream in) throws IOException {
		for (Row row : rows(in, "from")) {
			if (row.cells.length < 2) {
				throw bad("links.csv", row.line, "expected: from,to[,weight]");
			}
			String a = row.cells[0].toUpperCase(Locale.ROOT);
			String b = row.cells[1].toUpperCase(Locale.ROOT);
			if (!byCode.containsKey(a)) {
				throw bad("links.csv", row.line, "unknown district code " + a);
			}
			if (!byCode.containsKey(b)) {
				throw bad("links.csv", row.line, "unknown district code " + b);
			}
			if (a.equals(b)) {
				throw bad("links.csv", row.line, "a district cannot link to itself");
			}

			double weight = 1.0;
			if (row.cells.length > 2 && !row.cells[2].isEmpty()) {
				try {
					weight = Double.parseDouble(row.cells[2]);
				} catch (NumberFormatException e) {
					throw bad("links.csv", row.line, "weight is not a number: " + row.cells[2]);
				}
			}
			if (weight <= 0) {
				throw bad("links.csv", row.line, "weight must be greater than 0");
			}

			adjacency.computeIfAbsent(a, k -> new TreeMap<>()).put(b, weight);
			adjacency.computeIfAbsent(b, k -> new TreeMap<>()).put(a, weight);
		}
	}

	private record Row(int line, String[] cells) {
	}

	// Skips blank lines, # comments, the header row and a UTF-8 BOM (Windows editors add one).
	private static List<Row> rows(InputStream in, String headerFirstCell) throws IOException {
		List<Row> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			int number = 0;
			while ((line = reader.readLine()) != null) {
				number++;
				if (number == 1 && line.startsWith("\uFEFF")) {
					line = line.substring(1);
				}
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				String[] cells = trimmed.split(",", -1);
				for (int i = 0; i < cells.length; i++) {
					cells[i] = cells[i].trim();
				}
				if (cells[0].equalsIgnoreCase(headerFirstCell)) {
					continue;
				}
				rows.add(new Row(number, cells));
			}
		}
		return rows;
	}

	private static IllegalStateException bad(String file, int line, String problem) {
		return new IllegalStateException(file + " line " + line + ": " + problem);
	}
}