import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the data-base access object for tags.
 * 
 * @author Guillaume Halb, Benjamin Lebit, Ayutaya Rattanatray
 */
public class BookmarkDAO {
	private static final String SQL_READ_BOOKMARKS = "select id,title,description,link from Bookmark where id in (select Bookmarks_id from Bookmark_Tag where Tags_id=?)";
	private static final String SQL_READ_USER_BOOKMARKS = "select id,title,description,link from Bookmark where user_id=?";
	private static final String SQL_DELETE_BOOKMARK_FROM_TAG = "delete from Bookmark_Tag where Tags_id=? and Bookmarks_id=?";
	// TODO : trouver la bonne requete; idée mettre 3 requêtes
	private static final String SQL_CHECK_BOOKMARK_USER_TAG = "(select count(1) from bookmark where user_id=? and id=?) and (select count(1) from tag where user_id=? and id =?)";
	private static final String SQL_ADD_BOOKMARK_TO_TAG = "insert into Bookmark_Tag values (?,?)";
	private static final String SQL_SAVE_BOOKMARK = "insert into Bookmark " + "values(?,?,?,?,?)";
	private static final String SQL_DELETE_BOOKMARK = "delete from Bookmark where id=?";
	private static final String SQL_EMPTY_BOOKMARK_TAGS = "delete from Bookmark_Tag where Bookmarks_id=?";
	private static final String SQL_CHECK_BOOKMARK_USER = "select count(1) from Bookmark where id=? and user_id=?";
	private static final String SQL_MODIFY_BOOKMARK_WITHOUT_TAGS = "update Bookmark set title=?, description=?, link=? where id =?";
	private static final String SQL_GET_TAGS ="select Tags_id from Bookmark_Tag where Bookmarks_id  = ?";
	/**
	 * Provides the tags of a user.
	 * 
	 * @param tag
	 *            a tag
	 * @return bookmarks attached to the tag
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static List<Bookmark> getBookmarksFromTag(Tag tag) throws SQLException {
		List<Bookmark> list = new ArrayList<Bookmark>();
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_READ_BOOKMARKS);
			stmt.setLong(1, tag.getId());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				long id = result.getLong(1);
				String title = result.getString(2);
				String description = result.getString(3);
				String link = result.getString(4);
				Bookmark bookmark = new Bookmark(id, title, description, link);
				list.add(bookmark);
			}
			return list;
		} finally {
			conn.close();
		}
	}

	/**
	 * Provides the bookmarks of a user.
	 * 
	 * @param user
	 *            a user
	 * @return user Bookmarks
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static List<Bookmark> getBookmarksFromUser(User user) throws SQLException {
		List<Bookmark> list = new ArrayList<Bookmark>();
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_READ_USER_BOOKMARKS);
			PreparedStatement stmt2;
			stmt.setLong(1, user.getId());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				long id = result.getLong(1);
				String title = result.getString(2);
				String description = result.getString(3);
				String link = result.getString(4);
				Map<Long,Tag> tags = new HashMap<>();
				//Ajouter les tags
				System.out.println("on prepare la nouvelle requete");
				stmt2 = conn.prepareStatement(SQL_GET_TAGS);
				System.out.println("on initie les entrée");
				stmt2.setLong(1, id);
				System.out.println("on va executer la nouvelle requete");
				ResultSet result2 = stmt2.executeQuery();
				System.out.println("execution OK");
				while ( result2.next() ){
					long tagId = result2.getLong(1);
					System.out.println(" tag id : " + tagId);
					try{
						Tag tag = TagDAO.getTagById(tagId, user);
						if ( tag == null)
							System.out.println("le tag ets nul");
						tags.put(tagId, tag);
					}catch(SQLException e){
						e.printStackTrace();
					}
				}
				System.out.println("ajout du bookmark a la liste");
				Bookmark bookmark = new Bookmark(id, title, description, link, tags);
				list.add(bookmark);
			}
			return list;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
		finally{conn.close();}
	}

	/**
	 * Provides the bookmark which id is id and which is attached to the tag
	 * which is tag
	 * 
	 * @param tag
	 *            a tag
	 * @param id
	 *            bookmark's id
	 * 
	 * @return bookmark attached to tag and whose id is id
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static Bookmark getBookmarkFromTag(Tag tag, long id) throws SQLException {
		List<Bookmark> bookmarks = null;
		try {
			bookmarks = getBookmarksFromTag(tag);
		} catch (SQLException e) {
			System.out.println("getBookmarkFromTag");
		}

		if (bookmarks != null) {
			for (Bookmark bookmark : bookmarks) {
				if (bookmark.getId() == id)
					return bookmark;
			}
		}
		return null;
	}

	/**
	 * Unlink bookmark and tag
	 * 
	 * @param tag
	 *            a tag
	 * @param id
	 *            bookmark's id
	 * 
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static void deleteBookmarkFromTag(Tag tag, long id) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BOOKMARK_FROM_TAG);
			stmt.setLong(1, tag.getId());
			stmt.setLong(2, id);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("deleteBookmarkFromTag exception: " + e);
		} finally {
			conn.close();
		}
	}

	/**
	 * Provides the tags of a user.
	 * 
	 * @param tag
	 * 
	 * @param id
	 *            of the bookmark
	 * 
	 * @return boolean to know if the tag and bookmark are linked and if they
	 *         have a common user
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static boolean checkBookmarkUserTag(Tag tag, long id, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_BOOKMARK_USER_TAG);
			stmt.setLong(1, user.getId());
			stmt.setLong(2, id);
			stmt.setLong(3, user.getId());
			stmt.setLong(4, tag.getId());
			ResultSet result = stmt.executeQuery();
			// Vaut 1 si l'utilisateur a les droits, 0 sinon
			long check = 0;
			while (result.next()) {
				check = result.getLong(1);
			}
			return (check == 1);
		} catch (Exception e) {
			System.out.println("checkBookmarkUserTag exception: " + e);
			return false;
		} finally {
			conn.close();
		}
	}

	/**
	 * Provides the tags of a user.
	 * 
	 * @param tag
	 * 
	 * @param id
	 *            of the bookmark
	 * 
	 * @return Link bookmark and tag
	 * @throws SQLException
	 *             if the DB connection fails
	 */
	public static void addBookmarkToTag(Tag tag, long id) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			if (tag == null)
				return;
			PreparedStatement stmt = conn.prepareStatement(SQL_ADD_BOOKMARK_TO_TAG);
			stmt.setLong(1, id);
			stmt.setLong(2, tag.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("addBookmarkToTag exception: " + e);
		} finally {
			conn.close();
		}
	}

	// Save bookmark
	// TODO : Gérer les tags !
	public static void saveBookmark(Bookmark bookmark, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			if (bookmark.getId() == null) {
				PreparedStatement stmt = conn.prepareStatement("select max(id) from Bookmark");
				ResultSet result = stmt.executeQuery();
				while (result.next()) {
					long id = result.getLong(1) + 1;
					bookmark.setId(id);
				}
			}
			PreparedStatement stmt = conn.prepareStatement(SQL_SAVE_BOOKMARK);
			stmt.setLong(1, bookmark.getId());
			stmt.setString(2, bookmark.getDescription());
			stmt.setString(3, bookmark.getLink());
			stmt.setString(4, bookmark.getTitle());
			stmt.setLong(5, user.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("saveBookmark exception: " + e);
		} finally {
			conn.close();
		}
	}

	// Get bookmark from a name
	public static Bookmark getBookmarkByTitle(String title, User user) throws SQLException {
		List<Bookmark> list = null;
		try {
			list = getBookmarksFromUser(user);
		} catch (Exception e) {
			System.out.println("getTagByName");
		}
		if (list != null) {
			for (Bookmark bookmark : list) {
				if (bookmark.getTitle().equals(title))
					return bookmark;
			}
		}
		// Essayer de renvoyer une exception plutôt
		return null;

	}

	// Get bookmark from an ID
	public static Bookmark getBookmarkById(long id, User user) throws SQLException {
		List<Bookmark> list = null;
		try {
			list = getBookmarksFromUser(user);
		} catch (Exception e) {
			System.out.println("getTagByName");
		}
		if (list != null) {
			for (Bookmark bookmark : list) {
				if (bookmark.getId() == id)
					return bookmark;
			}
		}
		// Essayer de renvoyer une exception plutôt
		return null;

	}

	/**
	 * Modify a bookmark given in parameter
	 * 
	 * @param newTitle
	 * @param newDescription
	 * @param newLink
	 * @param tags
	 * @param bookmark
	 * @param user
	 */
	public static void modifyBookmark(String newTitle, String newDescription, String newLink, Map<Long,Tag> tags,
			Bookmark bookmark, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = null;
			System.out.println("modifyBookmark");
			
			stmt = conn.prepareStatement(SQL_EMPTY_BOOKMARK_TAGS);
			stmt.setLong(1, bookmark.getId());
			stmt.executeUpdate();
			if (!tags.isEmpty()) {
				System.out.println("if");
				for (Tag tag : tags.values()) {
					System.out.println("tag courant : " + tag.toString());
					stmt = conn.prepareStatement(SQL_ADD_BOOKMARK_TO_TAG);
					stmt.setLong(1, bookmark.getId());
					stmt.setLong(2, tag.getId());
					System.out.println("stmt: " + stmt.toString());
					stmt.executeUpdate();		
				}
			}
			stmt = conn.prepareStatement(SQL_MODIFY_BOOKMARK_WITHOUT_TAGS);
			stmt.setString(1, newTitle);
			stmt.setLong(4, bookmark.getId());
			stmt.setString(2, newDescription);
			stmt.setString(3, newLink);
			stmt.executeUpdate();
			
		} catch (Exception e) {
			System.out.println("modifyTag exception: " + e);
		} finally {
			conn.close();
		}
	}

	// Delete Bookmark
	public static void deleteBookmark(Bookmark bookmark, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			// Une fois qu'on a vidé le bookmark de ses tags on peut le
			// supprimer
			PreparedStatement stmt = conn.prepareStatement(SQL_EMPTY_BOOKMARK_TAGS);
			stmt.setLong(1, bookmark.getId());
			stmt.executeUpdate();
			stmt = conn.prepareStatement(SQL_DELETE_BOOKMARK);
			stmt.setLong(1, bookmark.getId());
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("deleteBookmark exception: " + e);
		} finally {
			conn.close();
		}
	}

	// Check if the user has access to the bookmark
	public static boolean checkBookmarkUser(Bookmark bookmark, User user) throws SQLException {
		Connection conn = DBConnection.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_BOOKMARK_USER);
			stmt.setLong(1, bookmark.getId());
			stmt.setLong(2, user.getId());
			ResultSet result = stmt.executeQuery();
			// Vaut 1 si l'utilisateur a les droits, 0 sinon
			long check = 0;
			while (result.next()) {
				check = result.getLong(1);
			}
			return (check == 1);
		} catch (Exception e) {
			System.out.println("checkBookmarkUser exception: " + e);
			return false;
		} finally {
			conn.close();
		}
	}

}
