import android.graphics.Bitmap
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.GeoJson

class RouteObj {
//    var dr: DirectionsRoute? = null
//    var route_geojson : GeoJson? = null
    var route_name_text: String? = null
    var route_length_text: String? = null
    var route_elevation_text: String? = null
    var route_popularity_text: String? = null
    var route_description_text : String? = null
    var route_start : String? = null
    var route_end : String? = null
    var route_duration: String? = null
    var difficulty: Double?= null
    var geoJsonurl: String?= null
    var snapshot: Bitmap? = null

    init {
        route_name_text = ""
        route_length_text = ""
        route_elevation_text = ""
        route_popularity_text = ""
        route_description_text = ""
    }

    constructor()

    override fun toString(): String {
        return "RouteObj(route_name_text=$route_name_text, route_length_text=$route_length_text, route_elevation_text=$route_elevation_text, route_popularity_text=$route_popularity_text, route_description_text=$route_description_text)"
    }


}

