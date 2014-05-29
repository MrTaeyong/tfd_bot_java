package util;


public class CoordinateConverter {
	public static GeoPoint katechToWgs84(double x, double y){
		GeoPoint katech = new GeoPoint(x, y);
		GeoPoint converted = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, katech);
		double tmp = converted.x;
		converted.x = converted.y;
		converted.y = tmp;
		return converted;
	}
	
	public static GeoPoint wgs84ToKatech(double latitude, double longitude){
		GeoPoint wgs84 = new GeoPoint(longitude, latitude);
		return  GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, wgs84);
	}
}
