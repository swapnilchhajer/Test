package cp.helpers;

import java.util.List;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
public class Polyline {

	public static List<GeoPoint> decodePolyline(String encodedPolyline) {

		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		int asciiChar, i = 0, lat = 0, lng = 0, dlat = 0, dlng = 0, shift, result;

		while (i < encodedPolyline.length()) {

			result = shift = 0;
			do {
				asciiChar = encodedPolyline.charAt(i++);
				asciiChar -= 63;
				result |= (asciiChar & 0x1f) << shift;
				shift += 5;
			} while ((asciiChar & 0x20) > 0);

			dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
			lat += dlat;

			result = shift = 0;
			do {
				asciiChar = encodedPolyline.charAt(i++);
				asciiChar -= 63;
				result |= (asciiChar & 0x1f) << shift;
				shift += 5;
			} while ((asciiChar & 0x20) > 0);

			dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
			lng += dlng;

			pointList.add(new GeoPoint(lat * 10, lng * 10));

		}

		return pointList;

	}

}

