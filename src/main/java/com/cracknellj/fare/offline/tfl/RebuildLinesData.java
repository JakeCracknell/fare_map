package com.cracknellj.fare.offline.tfl;

import com.cracknellj.fare.objects.TransportLine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RebuildLinesData {
    private static final Logger LOG = LogManager.getLogger(RebuildLinesData.class);

    private static TflLinesScraper tflLinesScraper = new TflLinesScraper();

    public static void main(String[] args) throws Exception {
        Set<String> lineIds = tflLinesScraper.getLineIds();
        LOG.info("Got " + lineIds + " line ids: " + lineIds);
        List<TransportLine> lines = lineIds.parallelStream()
                .map(id -> tflLinesScraper.getTransportLine(id))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
        writeRouteLinesToFile(lines);
    }

    private static void writeRouteLinesToFile(List<TransportLine> lines) {
        try (Writer writer = Files.newBufferedWriter(Paths.get("web", "data", "transport_lines.json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(lines, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
