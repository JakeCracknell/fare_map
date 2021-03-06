let lastFareAjaxRequest;

function loadStationsAsync() {
    resetProgressBarAndShowText("Loading stations data...");
    d3.json('./data/stations.json', onStationsLoaded)
        .on("progress", showAjaxProgressOnProgressBar);
}

function onStationsLoaded(stationList) {
    stationsByIdMap = new Map(stationList.map((p) => [p.stationId, p]));
    initialiseTypeAhead(stationList);
    map.on('viewreset zoomstart', removeSvgLayer); // otherwise the old overlay will be visible for a short time after zoom.
    map.on('moveend', drawMap); // on zoom, fires viewreset, then moveend.
    drawMap();
    hideProgressBar()
}

function loadFaresAsync() {
    gtag('event', 'FareSetRequest', {'event_label' : selectedSourceStation.stationName});
    resetProgressBarAndShowText("Loading fare data for " + selectedSourceStation.stationId + "...");
    lastFareAjaxRequest = d3.json(`./data/fares/${selectedSourceStation.stationId}.json`, onFaresLoaded)
        .on("progress", showAjaxProgressOnProgressBar);
}

function onFaresLoaded(fareJson) {
    faresList = (fareJson && fareJson.fares) || {};
    stationsByIdMap.forEach((station, stationId) => station.fares = faresList[stationId]);
    drawMap();
    hideProgressBar()
}

function showAjaxProgressOnProgressBar() {
    let total = d3.event.total || Math.max(500000, d3.event.loaded); // if (mod_deflate) then total=0
    $("#progress-container").show();
    $("#progress-bar").css("width", 100 * (d3.event.loaded / total) + "%");
    $("#progress-footer").text(`${Math.round(d3.event.loaded / 1024)} / ${Math.round(total / 1024)} KB`);
}

function resetProgressBarAndShowText(text) {
    $("#progress-bar").css("width", 0 + "%");
    $("#progress-header").text(text);
    $("#progress-footer").text();
}

function hideProgressBar() {
    $("#progress-container").hide();
}

function abortAnyFareAjaxRequests() {
    lastFareAjaxRequest && lastFareAjaxRequest.abort();
    hideProgressBar();
}
