/**
 * @FileName 	: DBController.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 5. 27.
 * @Author 		: NCri
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;

/**
 * @Class : DBController
 * @Date : 2014. 5. 27.
 * @Author : NCri
 */
public class DBController extends Controller {
	private final String _BASE_URL = "jdbc:mysql://203.253.23.40/tfd";
	
	public static final String PLACE_FIELD_NAME = "name";
	public static final String PLACE_FIELD_CATE = "category";
	public static final String PLACE_FIELD_ADDR = "address";
	public static final String PLACE_FIELD_TELE = "telephone";
	public static final String PLACE_FIELD_URL = "url";
	public static final String PLACE_FIELD_DESC = "description";
	public static final String PLACE_FIELD_POINT_X = "pointx";
	public static final String PLACE_FIELD_POINT_Y = "pointy";
	
	public void getData(String query){
		
	}
	/**
	 * 
	 * @method Name : insertData
	 * @date : 2014. 5. 27.
	 * @author : NCri
	 * @description :
	 * @param data
	 *            실제 DB에 들어갈 필드 네임을 Key 값으로 필드의 값을 Value값 형태를 한 HashMap 객체를
	 *            전달하도록 한다.
	 */
	public void insertData(String tableName, ArrayList<Map<String, String>> insertDatas) {
		/*	place_info tables
		 * +-------------+------------------+------+-----+---------+----------------+
		 * | Field       | Type             | Null | Key | Default | Extra          |
		 * +-------------+------------------+------+-----+---------+----------------+
		 * | id          | int(10) unsigned | NO   | PRI | NULL    | auto_increment |
		 * | name        | varchar(50)      | NO   |     | -       |                |
		 * | category    | varchar(50)      | NO   |     | -       |                |
		 * | address     | varchar(100)     | NO   |     | -       |                |
		 * | telephone   | varchar(20)      | YES  |     | -       |                |
		 * | url         | varchar(300)     | YES  |     | NULL    |                |
		 * | description | varchar(1000)    | YES  |     | -       |                |
		 * | pointx      | float            | NO   |     | NULL    |                |
		 * | pointy      | float            | NO   |     | NULL    |                |
		 * +-------------+------------------+------+-----+---------+----------------+
		 */
		Connection con = null;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(_BASE_URL, "dbmaster", "tfd1234");
			System.out.println("DBms connection success");
			System.out.println("DB load success");
			
			// create query string.
			String queryFormat = " insert into " + tableName + " (name, category, address, telephone, url, description, pointx, pointy)"
					+ " values (?, ?, ?, ?, ?, ?)";

			// create the mysql insert preparedstatement
			PreparedStatement preparedStmt = con.prepareStatement(queryFormat);

			for(Map<String, String> data : insertDatas){
				preparedStmt.setString(1, data.get(DBController.PLACE_FIELD_NAME));
				preparedStmt.setString(2, data.get(DBController.PLACE_FIELD_CATE));
				preparedStmt.setString(4, data.get(DBController.PLACE_FIELD_TELE));
				preparedStmt.setString(3, data.get(DBController.PLACE_FIELD_ADDR));
				preparedStmt.setString(5, data.get(DBController.PLACE_FIELD_URL));
				preparedStmt.setString(6, data.get(DBController.PLACE_FIELD_DESC));

				// execute the preparedstatement
				preparedStmt.execute();
			}

			con.close();
			System.out.println("데이터 Insert 완료.");
			System.out.println("DB 접속 종료.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void connect(String tableName) throws Exception {

	}
}
