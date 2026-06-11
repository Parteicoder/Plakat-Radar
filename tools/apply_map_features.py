from pathlib import Path

p = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
s = p.read_text(encoding="utf-8")
start = s.index("@Composable\nfun PosterMapScreen")
end = s.index("fun statusMarkerDrawable", start)
replacement = r'''@Composable
fun PosterMapScreen(posters: List<Poster>) {
    val context = LocalContext.current
    val fallback = remember { GeoPoint(52.5119, 13.4116) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var centered by remember { mutableStateOf(false) }

    fun centerOwn(map: MapView?, hint: Boolean) {
        val m = map ?: return
        val fused = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fused.lastLocation.addOnSuccessListener { loc ->
                val point = if (loc != null) GeoPoint(loc.latitude, loc.longitude) else fallback
                if (loc == null && hint) Toast.makeText(context, "Standort nicht verfügbar. Fallback Berlin.", Toast.LENGTH_LONG).show()
                m.controller.setZoom(if (loc != null) 16.5 else 15.0)
                m.controller.animateTo(point)
                m.invalidate()
            }.addOnFailureListener {
                if (hint) Toast.makeText(context, "Standort konnte nicht ermittelt werden.", Toast.LENGTH_LONG).show()
                m.controller.setZoom(15.0)
                m.controller.animateTo(fallback)
                m.invalidate()
            }
        } else {
            if (hint) Toast.makeText(context, "Bitte Standort-Berechtigung erlauben.", Toast.LENGTH_LONG).show()
            m.controller.setZoom(15.0)
            m.controller.animateTo(fallback)
            m.invalidate()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                Configuration.getInstance().userAgentValue = context.packageName
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(fallback)
                    mapView = this
                }
            },
            update = { map ->
                mapView = map
                if (!centered) { centered = true; centerOwn(map, false) }
                map.overlays.clear()
                posters.forEach { p ->
                    Marker(map).apply {
                        position = GeoPoint(p.latitude, p.longitude)
                        title = p.addressHint.ifBlank { statusText(p.status) }
                        snippet = "${statusText(p.status)} · ${p.createdByName} · Tippen: Weg dorthin"
                        icon = statusMarkerDrawable(context, p.status)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { marker, _ ->
                            marker.showInfoWindow()
                            openNavigation(context, p.latitude, p.longitude, p.addressHint.ifBlank { "Plakat ${statusText(p.status)}" })
                            true
                        }
                        map.overlays.add(this)
                    }
                }
                addPoliticalPoiMarkers(context, map)
                map.invalidate()
            }
        )
        FloatingActionButton(onClick = { centerOwn(mapView, true) }, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) { Text("⌖") }
    }
}

data class PoliticalPoiMarker(val title: String, val snippet: String, val latitude: Double, val longitude: Double, val iconRes: Int)

fun addPoliticalPoiMarkers(context: Context, map: MapView) {
    listOf(
        PoliticalPoiMarker("Kanzleramt", "Willy-Brandt-Straße 1, Berlin", 52.5200, 13.3694, R.drawable.ic_poi_politician_silhouette),
        PoliticalPoiMarker("Europaparlament", "Rue Wiertz 60, Brüssel", 50.8387, 4.3755, R.drawable.ic_poi_politician_silhouette),
        PoliticalPoiMarker("CDU-Zentrale", "Konrad-Adenauer-Haus, Berlin", 52.5073, 13.3526, R.drawable.ic_poi_egg_symbol)
    ).forEach { poi ->
        Marker(map).apply {
            position = GeoPoint(poi.latitude, poi.longitude)
            title = poi.title
            snippet = poi.snippet
            icon = ContextCompat.getDrawable(context, poi.iconRes) ?: statusMarkerDrawable(context, PosterStatus.REPLACED)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(this)
        }
    }
}

'''
p.write_text(s[:start] + replacement + s[end:], encoding="utf-8")
print("Map features applied")
