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
	private static final String SQL_CHECK_TAG_USER = "select count(1) from Tag where id=? and user_id=?";
	private static final String SQL_MODIFY_TAG = "update Tag set name=? where id =?";
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
			if (tag.getId() == null) {
				PreparedStatement stmt = conn.prepareStatement("select max(id) from Tag");
				ResultSet result = stmt.executeQuery();
				while (result.next()) {
					long id = result.getLong(1)+1;
					tag.setId(id);
				}
			}
			PreparedStatement stmt = conn.prepareStatement(SQL_SAVE_TAG);
			stmt.setLong(1, tag.getId());
			stmt.setString(2, tag.getName());
			stmt.setLong(3, user.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("saveTag exception: " + e);
		} finally{conn.close();}
	}

	//Delete Tag
	public static void deleteTag (Tag tag, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_TAG);
			stmt.setLong(1, tag.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("deleteTag exception: " + e);
		} finally{conn.close();}
	}
	
	//Modify Tag
	public static void modifyTag (String newName, Tag tag, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_MODIFY_TAG);
			stmt.setString(1, newName);
			stmt.setLong(2, tag.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("modifyTag exception: " + e);
		} finally{conn.close();}
	}
	
	//Check if the user has access for the tag
	public static boolean checkTagUser (Tag tag, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_TAG_USER);
			stmt.setLong(1, tag.getId());
			stmt.setLong(2, user.getId());
			ResultSet result = stmt.executeQuery();
			//Vaut 1 si l'utilisateur a les droits, 0 sinon
			long check = 0;
			while (result.next()) {
				check = result.getLong(1); 
			}
			return ( check == 1 );
		} catch (Exception e) {
			System.out.println("checkTagUser exception: " + e);
			return false;
		} finally{conn.close();}
	}

	//Get Tag from a name
	public static Tag getTagByName(String name, User user) throws SQLException {
		List<Tag> list = null;
		try {
			list = getTags(user);
		} catch (Exception e) {
			System.out.println("getTagByName");
		}
		if (list != null) {
			for( Tag tag : list ){
				if ( tag.getName().equals(name) )
					return tag;
			}
		}
		//Essayer de renvoyer une exception plutôt
		return null;
		
	}
	
	//Get Tag from an ID
	public static Tag getTagById(long id, User user) throws SQLException{
		List<Tag> list = null;
		try {
			list = getTags(user);
		} catch (Exception e) {
			System.out.println("getTagById");
		}
		if (list != null) {
			for( Tag tag : list ){
				if ( tag.getId() == id )
					return tag;
			}
		}
		//Essayer de renvoyer une exception plutôt
		return null;
	}
}
