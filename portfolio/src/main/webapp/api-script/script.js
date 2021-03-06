mapOptions = {
  center: { lat: 0, lng: 0 },
  zoom: 3,
  scaleControl: false,
  streetViewControl: false,
  mapTypeControlOptions: {
    mapTypeIds: ["moon"]
  }
};

google.charts.load('current', { 'packages': ['corechart'] });
google.charts.setOnLoadCallback(loadChart);

async function loadChart() {
  const response = await fetch("/nasa-data");
  const payload = await response.json();

  const data = new google.visualization.DataTable();
  data.addColumn("string", "ID");
  data.addColumn("number", "year");
  data.addColumn("number", "budget");

  // Per Google's documentation this column will affect how the color shifts, since
  // I want a constant color for every bubble I'm including it but leaving it blank
  data.addColumn("string", "color");
  data.addColumn("number", "patents");


  for (dataPoint of payload) {
    data.addRow(["", dataPoint.year, dataPoint.budget, "", dataPoint.patents]);
  }

  const options = {
    title: "Correlation between Nasa's discretionary budget v.s. patents acquired",
    hAxis: { title: 'Year' },
    vAxis: { title: 'Budget in USD Millions' },
    series: {"": {color:"darkcyan"}},
    legend: {position:"none"},
    width: 500,
    sizeAxis: { minValue: 6, maxSize: 20 }
  };

  var chart = new google.visualization.BubbleChart(document.getElementById('chart'));
  chart.draw(data, options);
}

async function loadMap() {
  const response = await fetch("/landing-data");
  const payload = await response.json();


  var map = new google.maps.Map(document.getElementById("map"), mapOptions);

  var moonMapType = new google.maps.ImageMapType({
    getTileUrl: function (coord, zoom) {
      var normalizedCoord = getNormalizedCoord(coord, zoom);
      if (!normalizedCoord) {
        return null;
      }
      var bound = Math.pow(2, zoom) - normalizedCoord.y - 1;
      return (
        `https://mw1.google.com/mw-planetary/lunar/lunarmaps_v1/apollo/${zoom}/${normalizedCoord.x}/${bound}.jpg`
      );
    },
    tileSize: new google.maps.Size(256, 256),
    maxZoom: 19,
    minZoom: 0,
    radius: 1738000,
    name: "Moon"
  });

  map.mapTypes.set("moon", moonMapType);
  map.setMapTypeId("moon");

  var infowindow = new google.maps.InfoWindow();

  var marker;

  for (i in payload) {
    const info = payload[i];
    const imgRef = `http://mw1.google.com/mw-planetary/lunar/lunarmaps_v1/util/s${info.missionNum}.png`;
    marker = new google.maps.Marker({ position: { lat: info.lat, lng: info.lng }, icon: imgRef, map: map });

    google.maps.event.addListener(marker, 'click', (function (marker, i) {
      return function () {
        infowindow.setContent(info.description);
        infowindow.open(map, marker);
      }
    })(marker, i));
  }
}

// Normalizes the coords that tiles repeat across the x axis (horizontally)
// like the standard Google map tiles.
function getNormalizedCoord(coord, zoom) {
  var y = coord.y;
  var x = coord.x;

  // tile range in one direction range is dependent on zoom level
  // 0 = 1 tile, 1 = 2 tiles, 2 = 4 tiles, 3 = 8 tiles, etc
  var tileRange = Math.pow(2, zoom);

  // don't repeat across y-axis (vertically)
  if (y < 0 || y >= tileRange) {
    return null;
  }

  // repeat across x-axis
  if (x < 0 || x >= tileRange) {
    x = ((x % tileRange) + tileRange) % tileRange;
  }

  return { x: x, y: y };
}
