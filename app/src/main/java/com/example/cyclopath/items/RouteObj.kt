import com.mapbox.api.directions.v5.models.DirectionsRoute

class RouteObj {
//    var dr: DirectionsRoute? = null
    var route_name_text: String? = null
    var route_length_text: String? = null
    var route_elevation_text: String? = null
    var route_popularity_text: String? = null
    var route_description_text : String? = null

    init {
//        dr = null
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

