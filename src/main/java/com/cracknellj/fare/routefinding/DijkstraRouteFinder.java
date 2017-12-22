package com.cracknellj.fare.routefinding;

import com.cracknellj.fare.objects.FareDetail;
import com.cracknellj.fare.objects.FareSet;
import com.cracknellj.fare.objects.Station;
import com.cracknellj.fare.provider.FareDataProvider;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public class DijkstraRouteFinder {
    public static final double MAX_PRICE = Double.MAX_VALUE;

    private final Map<String, Station> stations;
    private final FareDataProvider fareDataProvider;

    public DijkstraRouteFinder(Collection<Station> stations, FareDataProvider fareDataProvider) {
        this.stations = Maps.uniqueIndex(stations, s -> s.stationId);
        this.fareDataProvider = fareDataProvider;
    }

    public FareSet findCheapestRoutes(String fromId) {
        Set<String> unsettled = Sets.newHashSet(fromId);
        Set<String> settled = Sets.newHashSet();
        Map<String, Double> minFaresForStations = new HashMap<>(stations.size());
        stations.keySet().forEach(s -> minFaresForStations.put(s, MAX_PRICE));
        minFaresForStations.put(fromId, 0.0);
        Map<String, String> predecessors = new HashMap<>(stations.size());
        Map<String, FareDetailAndWaypoint> stationIdToNode = new HashMap<>(stations.size());
        stationIdToNode.put(fromId, FareDetailAndWaypoint.startNode(fromId));

        while (!unsettled.isEmpty()) {
            String node = unsettled.stream().sorted(Comparator.comparingDouble(minFaresForStations::get)).findFirst().get();
            unsettled.remove(node);
            settled.add(node);
            Double fareToNode = minFaresForStations.get(node);
            for (String nextStationId : stations.keySet()) {
                if (!settled.contains(nextStationId)) {
                    getFareDetailIfExists(node, nextStationId).ifPresent(fareDetail -> {
                        double proposedFare = fareToNode + fareDetail.price.doubleValue();
                        if (minFaresForStations.get(nextStationId) > proposedFare) {
                            minFaresForStations.put(nextStationId, proposedFare);
                            FareDetailAndWaypoint nextNode = new FareDetailAndWaypoint(nextStationId, fareDetail);
                            predecessors.put(nextStationId, node);
                            unsettled.add(nextStationId);
                            stationIdToNode.put(nextStationId, nextNode);
                        }
                    });
                }
            }
        }
        Map<FareDetailAndWaypoint, FareDetailAndWaypoint> nodePredecessors = predecessors.entrySet().stream()
                .collect(Collectors.toMap(e -> stationIdToNode.get(e.getKey()), e -> stationIdToNode.get(e.getValue())));
        MultiHopFareDetailBuilder multiHopFareDetailBuilder = new MultiHopFareDetailBuilder(stations, nodePredecessors);
        return new FareSet(fromId, multiHopFareDetailBuilder.createMap());
    }


    private Optional<FareDetail> getFareDetailIfExists(String fromId, String toId) {
        return fareDataProvider.getFares(fromId, toId).stream()
                .sorted(Comparator.comparing(f -> f.price)).findFirst();
    }


}