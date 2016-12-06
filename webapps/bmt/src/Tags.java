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
 * @author Jan Mikac
 */
public class Tags {
	/**
	 * Handles the request for the tag list.
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
	public static void handleTagList(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException, SQLException {
		// Rule-out PUT and DELETE requests
		System.out.println("Action: handleTagList - " + method + "-" + queryParams);
		if (method == Dispatcher.RequestMethod.PUT || method == Dispatcher.RequestMethod.DELETE) {
			resp.setStatus(405);
			return;
		}

		// Handle GET
		if (method == Dispatcher.RequestMethod.GET) {
			// Get the tag list
			List<Tag> tags = null;
			try {
				tags = TagDAO.getTags(user);
			} catch (SQLException ex) {
				resp.setStatus(500);
				return;
			}

			// Encode the tag list to JSON
			String json = "[";
			for (int i = 0, n = tags.size(); i < n; i++) {
				Tag tag = tags.get(i);
				json += tag.toJson();
				if (i < n - 1)
					json += ", ";
			}
			json += "]";

			// Send the response
			resp.setStatus(200);
			resp.setContentType("application/json");
			resp.getWriter().print(json);
			return;
		}

		// Handle POST
		
		if (method == Dispatcher.RequestMethod.POST) {
			// Get the tag list
			List<Tag> tags = null;
			try {
				tags = TagDAO.getTags(user);
				} catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
			
			//parse the query params
			JSONObject tagToAddJson = new JSONObject(queryParams.get("json").get(0));
			String tagName = (String) tagToAddJson.get("name");
			Tag tagToAdd = new Tag(tagName);
			
			
			//le tag existe deja
			if (TagDAO.getTagByName(tagToAdd.getName(), user)!=null)
			{
				resp.setStatus(304);
				return;
			} else 
			{
				TagDAO.saveTag(tagToAdd, user);
				resp.setStatus(201);
				return;
			}
			// TODO 1
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
	public static void handleTag(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException{
		System.out.println("Action: handleTag - " + method + "-" + queryParams);
		// TODO 2
		// Rule-out POST requests
			if (method == Dispatcher.RequestMethod.POST) {
				resp.setStatus(405);
				return;
			}
			
			//Récupérer l'id du tag
			long id = Long.parseLong(requestPath[requestPath.length - 1]);
			
			//Handle GET 
			if (method == Dispatcher.RequestMethod.GET) {
				try{
					//On vérifie si le tag existe bien
					if ( TagDAO.getTagById(id, user) == null ){
						resp.setStatus(404);
						return;
					}else{
						resp.getWriter().print(TagDAO.getTagById(id, user).toJson());
						resp.setStatus(200);
						return;
					}
				}catch (SQLException ex) {
					resp.setStatus(500);
					return;
				}
				
			}
			
			// Handle PUT
			if (method == Dispatcher.RequestMethod.PUT) {
				try{
					JSONObject tagToAddJson = new JSONObject(queryParams.get("json").get(0));
					String newName = (String) tagToAddJson.get("name");
					Tag tag = TagDAO.getTagById(id, user);
					if ( TagDAO.checkTagUser(tag, user) ){
						TagDAO.modifyTag(newName, tag, user);
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
			
			// Handle DELETE
			if (method == Dispatcher.RequestMethod.DELETE) {
				try{
					Tag tag = TagDAO.getTagById(id, user);
					if ( TagDAO.checkTagUser(tag, user) ){
						TagDAO.deleteTag(tag, user);
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
	public static void handleTagBookmarks(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException {
		
		System.out.println("Action: handleTagBookmarks - " + method + "-" + queryParams);
		// Rule-out POST, PUT and DELETE requests
			if (method == Dispatcher.RequestMethod.POST || method == Dispatcher.RequestMethod.PUT || method == Dispatcher.RequestMethod.DELETE) {
				resp.setStatus(405);
				return;
			}
			//TODO : trouver comment tester
			// Handle GET
			if (method == Dispatcher.RequestMethod.GET) {
				//Récupérer l'id du tag
				long id = Long.parseLong(requestPath[requestPath.length - 1]);
				try{
					Tag tag = TagDAO.getTagById(id, user);
					List<Bookmark> bookmarks = null;
					bookmarks = BookmarkDAO.getBookmarksFromTag(tag);
					String json = "[";
					for (int i = 0, n = bookmarks.size(); i < n; i++) {
						Bookmark bookmark = bookmarks.get(i);
						json += bookmark.toJson();
						if (i < n - 1)
							json += ", ";
					}
					json += "]";

					// Send the response
					resp.setStatus(200);
					resp.setContentType("application/json");
					resp.getWriter().print(json);
				}catch(SQLException e){
					resp.setStatus(500);
					return;
				}
			}
		
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
	public static void handleTagBookmark(HttpServletRequest req, HttpServletResponse resp,
			Dispatcher.RequestMethod method, String[] requestPath,
			Map<String, List<String>> queryParams, User user) throws IOException {
		System.out.println("Action: handleTagBookmark - " + method + "-" + queryParams);
		// TODO 2
		// Rule-out POST requests
		if (method == Dispatcher.RequestMethod.POST) {
			resp.setStatus(405);
			return;
		}
		
		
		//Handle GET 
		//TODO : A tester !
		if (method == Dispatcher.RequestMethod.GET) {
			//Donne l'information si le bookmark bID est attaché au tag tID
			long tagID = Long.parseLong(requestPath[1]);
			long bookmarkID = Long.parseLong(requestPath[requestPath.length - 1]);
			try{
				Tag tag = TagDAO.getTagById(tagID, user);
				if ( tag == null){
					resp.setStatus(404);
					return;
				}
				Bookmark bookmark = BookmarkDAO.getBookmarkFromTag(tag, bookmarkID);
				if ( bookmark == null ){
					resp.setStatus(404);
					return;
				}
				resp.getWriter().print(bookmark.toJson());
				resp.setContentType("application/json");
				resp.setStatus(204);
				return;
				
			}catch(SQLException e){
				resp.setStatus(500);
				return;
			}
		}
		
		// Handle PUT
		//TODO : a tester !
		if (method == Dispatcher.RequestMethod.PUT) {
			long tagID = Long.parseLong(requestPath[1]);
			long bookmarkID = Long.parseLong(requestPath[requestPath.length - 1]);
			try{
				Tag tag = TagDAO.getTagById(tagID, user);
				if ( BookmarkDAO.checkBookmarkUserTag(tag, bookmarkID,user) ){
					BookmarkDAO.addBookmarkToTag(tag, bookmarkID);
					resp.setStatus(204);
					return;
				}else{
					resp.setStatus(403);
					return;
				}
			}catch(SQLException e){
				resp.setStatus(500);
				return;
			}
		}
		
		// Handle DELETE
		//TODO : A tester !
		if (method == Dispatcher.RequestMethod.DELETE) {
			long tagID = Long.parseLong(requestPath[1]);
			long bookmarkID = Long.parseLong(requestPath[requestPath.length - 1]);
			try{
				Tag tag = TagDAO.getTagById(tagID, user);
				if ( BookmarkDAO.checkBookmarkUserTag(tag, bookmarkID,user) ){
					BookmarkDAO.deleteBookmarkFromTag(tag, bookmarkID);
					resp.setStatus(204);
					return;
				}else{
					resp.setStatus(403);
					return;
				}
			}catch(SQLException e){
				resp.setStatus(500);
				return;
			}
		}
	}
}
