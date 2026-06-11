from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

old = '''@Composable
fun PosterMapScreen(posters: List<Poster>) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(posters.firstOrNull()?.latitude ?: 51.4592, posters.firstOrNull()?.longitude ?: 12.6331))
            }
        },
        update = { map ->
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
            map.invalidate()
        }
    )
}
'''

new = '''@Composable
fun PosterMapScreen(posters: List<Poster>) {
    val context = LocalContext.current
    val bswBerlinFallback = remember { GeoPoint(52.5119, 13.4116) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var initialCenterDone by remember { mutableStateOf(false) }

    fun centerOnOwnLocation(map: MapView?, showHint: Boolean) {
        val targetMap = map ?: return
        val fused = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        targetMap.controller.setZoom(16.5)
                        targetMap.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                        targetMap.invalidate()
                    } else {
                        if (showHint) Toast.makeText(context, "Standort nicht verfügbar. Bitte GPS aktivieren.", Toast.LENGTH_LONG).show()
                        targetMap.controller.setZoom(15.0)
                        targetMap.controller.animateTo(bswBerlinFallback)
                        targetMap.invalidate()
                    }
                }
                .addOnFailureListener {
                    if (showHint) Toast.makeText(context, "Standort konnte nicht ermittelt werden.", Toast.LENGTH_LONG).show()
                    targetMap.controller.setZoom(15.0)
                    targetMap.controller.animateTo(bswBerlinFallback)
                    targetMap.invalidate()
                }
        } else {
            if (showHint) Toast.makeText(context, "Bitte zuerst die Standort-Berechtigung erlauben.", Toast.LENGTH_LONG).show()
            targetMap.controller.setZoom(15.0)
            targetMap.controller.animateTo(bswBerlinFallback)
            targetMap.invalidate()
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
                    controller.setCenter(bswBerlinFallback)
                    mapView = this
                }
            },
            update = { map ->
                mapView = map
                if (!initialCenterDone) {
                    initialCenterDone = true
                    centerOnOwnLocation(map, showHint = false)
                }
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
                map.invalidate()
            }
        )

        FloatingActionButton(
            onClick = { centerOnOwnLocation(mapView, showHint = true) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Text("⌖")
        }
    }
}
'''

if old not in text:
    raise SystemExit("PosterMapScreen block not found. Nothing changed.")

path.write_text(text.replace(old, new), encoding="utf-8")
print("Map location button applied.")
