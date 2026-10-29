package vn.uteexpress.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.RouteResponse;
import vn.uteexpress.routing.DistrictGraph;
import vn.uteexpress.routing.DistrictGraph.District;
import vn.uteexpress.routing.DistrictGraph.Route;

@Service
public class RouteService {

	private final DistrictGraph graph;

	public RouteService() {
		try (InputStream districts = new ClassPathResource("routing/district.csv").getInputStream();
				InputStream links = new ClassPathResource("routing/links.csv").getInputStream()) {
			this.graph = DistrictGraph.load(districts, links);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load routing data", e);
		}
	}

	public List<District> districts() {
		return graph.districts();
	}

	public RouteResponse plan(String from, String to) {
		District start = graph.resolve(from)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
								"Không nhận diện được quận: " + from));
		District goal = graph.resolve(to)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Không nhận diện được quận: " + to));

		Route route = graph.shortestPath(start, goal).orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND, "Không tìm thấy tuyến giao hàng giữa " + start.name() + " và " + goal.name()));

		return new RouteResponse(start, goal, route.path(), route.hops(), route.totalWeight());
	}

	public Optional<Route> shortestPath(District start, District goal, Set<String> allowedDistrictCodes) {
		return graph.shortestPath(start, goal, allowedDistrictCodes);
	}

	public boolean areAdjacent(District first, District second) {
		return graph.areAdjacent(first, second);
	}

	public Optional<District> resolve(String text) {
		return graph.resolve(text);
	}

	public Optional<Integer> hops(District from, District to) {
		return graph.shortestPath(from, to).map(Route::hops);
	}
}