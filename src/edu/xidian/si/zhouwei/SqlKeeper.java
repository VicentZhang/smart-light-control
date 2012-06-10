package edu.xidian.si.zhouwei;

public class SqlKeeper {
	
	public static final String CREATE_LIGHT = 
		"create table light " +
		"(" +
			"id integer primary key autoincrement, " +
			"location nvarchar, " +
			"on_code int unique, " +
			"off_code int unique, " +
			"power_state int8" +
		");";	
	
	public static final String SELECT_LIGHT_ALL = 
		"select * from light;";
	
	public static final String INSERT_LIGHT = 
		"insert into light " +
		"(id, location, on_code, off_code, power_state) " +
		"values (null,  ?, ?, ?, ?);";
	
	public static final String UPDATE_LIGHT_LOCATION = 
		"update light set location = ? where id = ?;";
	
	public static final String UPDATE_LIGHT_ON_CODE = 
		"update light set on_code = ? where id = ?;";
	
	public static final String UPDATE_LIGHT_OFF_CODE = 
		"update light set off_code = ? where id = ?;";
	
	public static final String DELETE_LIGHT = 
		"delete from light where id = ?;";
}
