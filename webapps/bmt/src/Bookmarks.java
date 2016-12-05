import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.ajax.JSON.Source;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Provides handling of tag-related requests.
 * 
 * @author Guillaume Halb, Benjamin Lebit, Ayutaya Rattanatray
 */
public class Bookmarks {
	/**
	 * Handles the request for the bookmark list.
	 * 
	 * @param req
	 *           the request
	 * @param resp
	 *           the response
	 * @param method
	 *           request method to appply
	 * @param requestPath
	 *           request path
	 * @param queryParams
	 *           query parameters
	 * @param user
	 *           the user
	 * @throws IOException
	 *            if the response cannot be written
	 * @throws SQLException 
	 */
	public static void handleBookmarkList(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException, SQLException {
		// Rule-out PUT and DELETE requests
		System.out.println("Action: handleBookmarkList - " + method + "-" + queryParams);
		if (method == Dispatcher.RequestMethod.PUT || method == Dispatcher.RequestMethod.DELETE) {
			resp.setStatus(405);
			return;
		}

		// Handle GET
		if (method == Dispatcher.RequestMethod.GET) {
			// Get the Bookmark list
			List<Bookmark> bookmarks = null;
			try {
				bookmarks = BookmarkDAO.getBookmarksFromUser(user);
			} catch (SQLException ex) {
				resp.setStatus(500);
				return;
			}
			// Encode the tag list to JSON
			String json = "[";
			for (int i = 0, n = bookmarks.size(); i < n; i++) {
				Bookmark bookmark = bookmarks.get(i);
				json += bookmark.toJson();
				if (i < n - 1)
					json += ", ";
			}
			json += "]";
//TODO : gérer l'affichage

			// Send the response
			resp.setStatus(200);
			resp.setContentType("application/json");
			resp.getWriter().print(json);
			return;
		}

		// Handle POST
		
		if (method == Dispatcher.RequestMethod.POST) {
			// Get the Bookmark list
			List<Bookmark> bookmarks = null;
			try {
				bookmarks = BookmarkDAO.getBookmarksFromUser(user);
				} catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
			//parse the query params
			JSONObject bookmarkToAddJson = new JSONObject(queryParams.get("json").get(0));
			String bookmarkTitle = (String) bookmarkToAddJson.get("title");
			String bookmarkDescription = (String) bookmarkToAddJson.get("description");
			String bookmarkLink = (String) bookmarkToAddJson.get("link");
			Bookmark bookmarkToAdd = new Bookmark(bookmarkTitle, bookmarkDescription, bookmarkLink);
			//le tag existe deja
			if (BookmarkDAO.getBookmarkByTitle(bookmarkTitle, user)!=null)
			{
				System.out.println("existe déja");
				resp.setStatus(304);
				return;
			} else 
			{
				BookmarkDAO.saveBookmark(bookmarkToAdd, user);
				resp.setStatus(200);
				return;
			}

		}

		// Other
		resp.setStatus(405);
	}
	
	
	/**
	 * TODO comment
	 * 
	 * @param req
	 * @param resp
	 * @param method
	 * @param requestPath
	 * @param queryParams
	 * @param user
	 */
	public static void handleBookmark(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException{
		System.out.println("Action: handleBookmark - " + method + "-" + queryParams);
		// TODO 2
		// Rule-out POST requests
			if (method == Dispatcher.RequestMethod.POST) {
				resp.setStatus(405);
				return;
			}
			//Récupérer l'id du bookmark
			long id = Long.parseLong(requestPath[requestPath.length - 1]);
			
			//Handle GET 
			if (method == Dispatcher.RequestMethod.GET) {
				try{
					//On vérifie si le tag existe bien
					if ( BookmarkDAO.getBookmarkById(id, user) == null ){
						resp.setStatus(404);
						return;
					}else{
						resp.getWriter().print(BookmarkDAO.getBookmarkById(id, user).toJson());
						resp.setStatus(200);
						return;
					}
				}catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
			}
			//Handle PUT 
			if (method == Dispatcher.RequestMethod.PUT) {
				//TODO : attention aux tags !
			}
			//Handle DELETE 
			if (method == Dispatcher.RequestMethod.DELETE) {
				try{
					Bookmark bookmark = BookmarkDAO.getBookmarkById(id, user);
					if ( BookmarkDAO.checkBookmarkUser(bookmark, user) ){
						BookmarkDAO.deleteBookmark(bookmark, user);
						resp.setStatus(204);
						return;
					}else{
						resp.setStatus(403);
						return;
					}

				}catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
			}
	}
	
}
