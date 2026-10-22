package vn.uteexpress.dto;

import java.util.List;

import vn.uteexpress.routing.DistrictGraph.District;

public record RouteResponse(District from, District to, List<District> path, int hops, double totalWeight) {
}