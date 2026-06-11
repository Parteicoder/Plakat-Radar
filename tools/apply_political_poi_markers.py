from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

old_loop_end = '''                posters.forEach { p ->
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
'''

new_loop_end = '''                posters.forEach { p ->
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
'''

if old_loop_end not in text:
    raise SystemExit("Poster marker loop not found. Nothing changed.")
text = text.replace(old_loop_end, new_loop_end, 1)

helper_anchor = '''fun statusMarkerDrawable(context: Context, status: PosterStatus): Drawable = GradientDrawable().apply {
'''
helper = '''data class PoliticalPoiMarker(
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double,
    val iconRes: Int
)

fun addPoliticalPoiMarkers(context: Context, map: MapView) {
    val pois = listOf(
        PoliticalPoiMarker(
            title = "Kanzleramt",
            snippet = "Willy-Brandt-Straße 1, Berlin",
            latitude = 52.5200,
            longitude = 13.3694,
            iconRes = R.drawable.ic_poi_politician_silhouette
        ),
        PoliticalPoiMarker(
            title = "Europaparlament",
            snippet = "Rue Wiertz 60, Brüssel",
            latitude = 50.8387,
            longitude = 4.3755,
            iconRes = R.drawable.ic_poi_politician_silhouette
        ),
        PoliticalPoiMarker(
            title = "CDU-Zentrale",
            snippet = "Konrad-Adenauer-Haus, Berlin",
            latitude = 52.5073,
            longitude = 13.3526,
            iconRes = R.drawable.ic_poi_egg_symbol
        )
    )
    pois.forEach { poi ->
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

if helper_anchor not in text:
    raise SystemExit("statusMarkerDrawable anchor not found. Nothing changed.")
if "data class PoliticalPoiMarker" not in text:
    text = text.replace(helper_anchor, helper + helper_anchor, 1)

path.write_text(text, encoding="utf-8")
print("Political POI markers applied.")
