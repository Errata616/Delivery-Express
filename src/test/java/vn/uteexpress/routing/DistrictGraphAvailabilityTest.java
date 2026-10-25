package vn.uteexpress.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DistrictGraphAvailabilityTest {

	@Test
	void findsShortestRouteOnlyThroughDistrictsWithAvailableShippers() throws Exception {
		DistrictGraph graph = graph();

		DistrictGraph.Route route = graph.shortestPath("A", "D", Set.of("A", "C", "D")).orElseThrow();

		assertEquals(Set.of("A", "C", "D"), route.path().stream().map(DistrictGraph.District::code)
				.collect(java.util.stream.Collectors.toSet()));
		assertEquals(2, route.hops());
	}

	@Test
	void returnsNoRouteWhenAvailableDistrictsDoNotConnectEndpoints() throws Exception {
		DistrictGraph graph = graph();

		assertTrue(graph.shortestPath("A", "D", Set.of("A", "B")).isEmpty());
	}

	private DistrictGraph graph() throws Exception {
		String districts = "code,name\nA,A\nB,B\nC,C\nD,D\n";
		String links = "from,to,weight\nA,B,1\nB,D,1\nA,C,1\nC,D,1\n";
		return DistrictGraph.load(stream(districts), stream(links));
	}

	private ByteArrayInputStream stream(String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}
}
