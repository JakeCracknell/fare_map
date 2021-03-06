package com.cracknellj.fare.routefinding;

import com.cracknellj.fare.objects.FareDetail;
import com.cracknellj.fare.objects.Station;
import com.cracknellj.fare.provider.FareDataProvider;

import java.util.Map;

public class PeakTimeDijkstraSplitTicketTask extends DijkstraSplitTicketTask {
    public PeakTimeDijkstraSplitTicketTask(Map<String, Station> stations, FareDataProvider fareDataProvider, String fromId) {
        super(stations, fareDataProvider, fromId);
    }

    @Override
    FareDetail getFareDetailIfExistsOrNull(String fromId, String toId) {
        return fareDataProvider.getFares(fromId, toId).cheapestPeak;
    }
}
