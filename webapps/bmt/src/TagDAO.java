import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the data-base access object for tags.
 * 
 * @author Jan Mikac, Sebastien Viardot
 */
public class TagDAO {
	/**
	 * SQL query for user login
	 */
	private static final String SQL_READ_TAGS = "select id,name from Tag where user_id=?";
	private static final String SQL_SAVE_TAG = "insert into Tag " + "values(?,?,?)";
	private static final String SQL_DELETE_TAG = "delete from Tag where id=?";
	private static final String SQL_CHECK_TAG_USER = "select count(1) from table where id=? and user_id=?";

	/**
	 * Provides the tags of a user.
	 * 
	 * @param user
	 *           a user
	 * @return user tags
	 * @throws SQLException
	 *            if the DB connection fails
	 */
	public static List<Tag> getTags(User user) throws SQLException {
		List<Tag> list = new ArrayList<Tag>();
		Connection conn = DBConnection.getConnection();
		try{
			PreparedStatement stmt = conn.prepareStatement(SQL_READ_TAGS);
			stmt.setLong(1, user.getId());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				long id = result.getLong(1);
				String name = result.getString(2);
				Tag tag = new Tag(id, name);
				list.add(tag);
			}
			return list;
		} finally{conn.close();}
	}
	
	//Save tag
	public static void saveTag (Tag tag, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_SAVE_TAG);
			stmt.setLong(1, tag.getId());
			stmt.setString(2, tag.getName());
			stmt.setLong(3, user.getId());
			ResultSet result = stmt.executeQuery();
		} finally{conn.close();}
	}

	//Delete Tag
	public static void deleteTag (Tag tag, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_TAG);
			stmt.setLong(1, tag.getId());
			ResultSet result = stmt.executeQuery();
		} finally{conn.close();}
	}
	
	
	
	//Get Tag from a name
	public static Tag getTagByName(String name, User user) throws SQLException{
		List<Tag> list = getTags(user);
		for( Tag tag : list ){
			if ( tag.getName().equals(name) )
				return tag;
		}
		//Essayer de renvoyer une exception plutôt
		return null;
		
	}
	
	//Get Tag from an ID
	public static Tag getTagById(long id, User user) throws SQLException{
		List<Tag> list = getTags(user);
		for( Tag tag : list ){
			if ( tag.getId() == id )
				return tag;
		}
		//Essayer de renvoyer une exception plutôt
		return null;
		
	}
}
