/**
 * @FileName 	: TFDDBController.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 6. 24.
 * @Author 		: NCri
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.StringUtil;


/**
 * @Class		: TFDDBController
 * @Date 		: 2014. 6. 24.
 * @Author 		: NCri
 */
public class TFDDBController extends DBController{
	/**
	 * +----------+--------------+------+-----+---------+-------+
	 * | Field    | Type         | Null | Key | Default | Extra |
	 * +----------+--------------+------+-----+---------+-------+
	 * | local_id | int(10)      | NO   | PRI | NULL    |       |
	 * | link     | varchar(100) | NO   | PRI | NULL    |       |
	 * | width    | int(4)       | YES  |     | NULL    |       |
	 * | height   | int(4)       | YES  |     | NULL    |       |
	 * +----------+--------------+------+-----+---------+-------+
	 */
	public static enum PlaceLink{
		LOCAL_ID("local_id"), LINK("link"), WIDTH("width"), HEIGHT("height") ;
		final private String name;
		
		PlaceLink(String name) {
		        this.name = name;
		    }
		    String value() {
		        return name;
		}
	}
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
	
	public static enum Place{
		ID("id"), NAME("name"), CATEGORY("category"), ADDRESS("address"), TELEPHONE("telephone"), URL("url"),
		DESC("description"), POINT_X("pointx"), POINT_Y("pointy");
		
		final private String name;
		
		Place(String name) {
		        this.name = name;
		    }
		    String value() {
		        return name;
		}
		
	}
	
//	private final String _BASE_URL = "jdbc:mysql://203.253.23.40/tfd";
//	private final String _BASE_URL = "jdbc:mysql://203.253.23.38/tfd";
	private final String _BASE_URL = "jdbc:mysql://220.70.0.4/tfd";
	
	public static final String PLACE_FIELD_NAME = "name";
	public static final String PLACE_FIELD_CATE = "category";
	public static final String PLACE_FIELD_ADDR = "address";
	public static final String PLACE_FIELD_TELE = "telephone";
	public static final String PLACE_FIELD_URL = "url";
	public static final String PLACE_FIELD_DESC = "description";
	public static final String PLACE_FIELD_POINT_X = "pointx";
	public static final String PLACE_FIELD_POINT_Y = "pointy";
	
	public Connection getConnection(String baseUrl, String id, String passwd){
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(baseUrl, id, passwd);
//			System.out.println("DBms connection success");
//			System.out.println("DB load success");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (SQLException e) {
			
		}
		
		return con;
		
	}
	
	public List<Map<String, String>> getData(String query){
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		String[] fields;
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			fields = query.substring(query.indexOf("select") + 7, query.indexOf("from")).split(",");
		} catch (Exception e){
			return null;
		}
		
		try{
			con = DriverManager.getConnection(_BASE_URL, "tfd", "tfd1234");
			
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			
			while(rs.next()){
				Map<String, String> record = new HashMap<String, String>();
				for(String field : fields){
					String temp = field.trim();
					record.put(temp, rs.getString(temp));
				}
				result.add(record);
			}
			if(result.size() > 0)
				return result;
			return null;
		} catch (Exception exception){
			return null;
		} finally {
			try {
				con.close();
				stmt.close();
				rs.close();
			} catch (SQLException e) {}
			catch (NullPointerException e){}
		}
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
		Connection con = null;
		PreparedStatement preparedStmt = null;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(_BASE_URL, "tfd", "tfd1234");
//			System.out.println("DBms connection success");
//			System.out.println("DB load success");
			
			// create query string.
			String test1 = String.format("insert into %s ", tableName);
			
			// 쿼리를 하기 위해서 칼럼 이름들을 가져온다.
			String delim = ",";
			Map<String, String> insertData = insertDatas.get(0);
			String columns = generateColumn(insertData);
			String values = generateValues(insertData);
			
			String queryFormat = " insert into " + tableName + columns
					+ " values" + values;
			// create the mysql insert preparedstatement
			preparedStmt = con.prepareStatement(queryFormat);
			ArrayList<String> keySequence = new ArrayList<String>(insertData.keySet());
			for(Map<String, String> data : insertDatas){
				for(int i = 0; i < keySequence.size(); i++)
					preparedStmt.setString(i+1, data.get(keySequence.get(i)));

				// execute the preparedstatement
				try{
					preparedStmt.execute();
				} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException exception){}
			}

//			System.out.println("데이터 Insert 완료.");
//			System.out.println("DB 접속 종료.");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
				preparedStmt.close();
			} catch (SQLException e) {}
		}
	}
	
	public void queryExecute(String query) {
		Connection con = null;

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(_BASE_URL, "tfd", "tfd1234");
			
			con.prepareStatement(query).execute();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String generateColumn(Map<String, String> data){
		String delim = ",";
		return "(" + StringUtil.join(new ArrayList<String>(data.keySet()), delim) + ")";
	}
	public static String generateValues(Map<String, String> data){
		String delim = ",";
		ArrayList<String> temp = new ArrayList<String>();
		
		// queryFormat 을 만들기 위한 작업.
		// column의 해당하는 값을 읽어 오기 위한
		// 포맷을 만들기 위해서
		// (?, ?, ?, ?....)형태로 만들어야 한다.
		// 예) insert into my_table_name (column1, column2, ... ) (?, ?, ?, ....)
		// 형태로 format 이 들어가면 PreparedStatement 클래스를 이용해서 ? 에 해당하는 값을
		// 가져 올수 있다.
		for (int i = 0; i < data.keySet().size(); i++)
			temp.add("?");
		return "(" + StringUtil.join(temp, delim) + ")";
	}
	public void connect(String tableName) throws Exception {

	}
	
	public static void main(String[] args){
		ArrayList<String> test = new ArrayList<String>();
		test.add("name1");
		test.add("name2");
		test.add("name3");
		test.add("name4");
		test.add("name5");
		test.add("name6");
		
		System.out.println(StringUtil.join(test, ","));
		
	}

}
