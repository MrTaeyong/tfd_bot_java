/**
 * @FileName	: CoordinateConverter.java
 * @Project		: tfd_bot_java
 * @Date		: 2014. 05. 29.
 * @Author		: Taeyong
 */
package util;

/**
 * @Class	: CoordinateConverter
 * @Date	: 2014. 05. 29.
 * @Author	: Taeyong
 */
public class CoordinatesConverter {
	
	/**
	 * Katech 좌표계를 WGS84 좌표계로 변환하는 메소드
	 * @param x	: Katech 좌표계의 x좌표
	 * @param y	: Katech 좌표계의 y좌표
	 * @return	: 변환된 좌표정보가 담긴 GeoPoint 객체
	 */
	public static GeoPoint katechToWgs84(double x, double y){
		GeoPoint katech = new GeoPoint(x, y);
		GeoPoint converted = GeoTrans.convert(GeoTrans.KATEC, GeoTrans.GEO, katech);
		double tmp = converted.x;
		converted.x = converted.y;
		converted.y = tmp;
		return converted;
	}
	
	/**
	 * WGS84 좌표계를 Katech 좌표계로 변환하는 메소드
	 * @param latitude	: WGS84 좌표계의 latitude좌표
	 * @param longitude	: WGS84 좌표계의 longitude좌표
	 * @return	: 변환된 좌표정보가 담긴 GeoPoint 객체
	 */
	public static GeoPoint wgs84ToKatech(double latitude, double longitude){
		GeoPoint wgs84 = new GeoPoint(longitude, latitude);
		return  GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, wgs84);
	}
}
