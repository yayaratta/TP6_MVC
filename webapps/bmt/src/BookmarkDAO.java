import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the data-base access object for tags.
 * 
 * @author Guillaume Halb, Benjamin Lebit, Ayutaya Rattanatray
 */
public class BookmarkDAO {
	private static final String SQL_READ_BOOKMARKS = "select id,title,description,link from Bookmark where id in (select Bookmarks_id from Bookmark_Tag where Tags_id=?)";
	private static final String SQL_DELETE_BOOKMARK_FROM_TAG = "delete from Bookmark_Tag where Tags_id=? and Bookmarks_id=?";
	private static final String SQL_CHECK_BOOKMARK_USER_TAG = "";
	private static final String SQL_ADD_BOOKMARK_TO_TAG = "insert into Bookmark_Tag values (?,?)";
	
	/**
	 * Provides the tags of a user.
	 * 
	 * @param tag
	 *           a tag
	 * @return bookmarks attached to the tag
	 * @throws SQLException
	 *            if the DB connection fails
	 */
	public static List<Bookmark> getBookmarksFromTag(Tag tag) throws SQLException {
		List<Bookmark> list = new ArrayList<Bookmark>();
		Connection conn = DBConnection.getConnection();
		try{
			PreparedStatement stmt = conn.prepareStatement(SQL_READ_BOOKMARKS);
			stmt.setLong(1, tag.getId());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				long id = result.getLong(1);
				String title = result.getString(2);
				String description = result.getString(3);
				String link = result.getString(4);
				Bookmark bookmark = new Bookmark(id, title,description,link);
				list.add(bookmark);
			}
			return list;
		} finally{conn.close();}
	}
	
	/**
	 * Provides the bookmark which id is id and which is attached
	 * to the tag which is tag
	 * 
	 * @param tag
	 *           a tag
	 * @param id
	 *          bookmark's id
	 *           
	 * @return bookmark attached to tag and whose id is id
	 * @throws SQLException
	 *            if the DB connection fails
	 */
	public static Bookmark getBookmarkFromTag(Tag tag, long id) throws SQLException{
		List<Bookmark> bookmarks = null;
		try{
			bookmarks = getBookmarksFromTag(tag);
		}catch(SQLException e){
			System.out.println("getBookmarkFromTag");
		}
		
		if ( bookmarks != null ){
			for ( Bookmark bookmark : bookmarks ){
				if ( bookmark.getId() == id )
					return bookmark;
			}
		}
		return null;	
	}
	
	
	/**
	 * Unlink bookmark and tag
	 * 
	 * @param tag
	 *           a tag
	 * @param id
	 *          bookmark's id
	 *           
	 * @throws SQLException
	 *            if the DB connection fails
	 */
	public static void deleteBookmarkFromTag(Tag tag, long id) throws SQLException{
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BOOKMARK_FROM_TAG);
			stmt.setLong(1, tag.getId());
			stmt.setLong(2, id);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("deleteBookmarkFromTag exception: " + e);
		} finally{conn.close();}
	}
	
	public static boolean checkBookmarkUserTag(Tag tag, long id)throws SQLException{
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_BOOKMARK_USER_TAG);
			stmt.setLong(1, tag.getId());
			stmt.setLong(2, id);
			ResultSet result = stmt.executeQuery();
			//Vaut 1 si l'utilisateur a les droits, 0 sinon
			long check = 0;
			while (result.next()) {
				check = result.getLong(1); 
			}
			return ( check == 1 );
		} catch (Exception e) {
			System.out.println("checkBookmarkUserTag exception: " + e);
			return false;
		} finally{conn.close();}
	}
	
	public static void addBookmarkToTag( Tag tag, long id) throws SQLException{
		Connection conn = DBConnection.getConnection();
		try {
			if ( tag == null )
				return;
			PreparedStatement stmt = conn.prepareStatement(SQL_ADD_BOOKMARK_TO_TAG);
			stmt.setLong(1, id);
			stmt.setLong(2, tag.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("addBookmarkToTag exception: " + e);
		} finally{conn.close();}
	}
	
	
}
