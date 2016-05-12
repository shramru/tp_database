package rest;

import db.Database;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static main.Helper.*;

/**
 * Created by vladislav on 18.03.16.
 */
@Singleton
@Path("/post")
public class Post {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', %s, '%s', '%s', '%s', %s, %s, %s, %s, %s, %s",
                    jsonObject.getString("date"), jsonObject.getString("thread"), jsonObject.getString("message"),
                    jsonObject.getString("user"), jsonObject.getString("forum"), jsonObject.getString("parent"),
                    serializeBoolean(jsonObject.optBoolean("isApproved")),
                    serializeBoolean(jsonObject.optBoolean("isHighlighted")),
                    serializeBoolean(jsonObject.optBoolean("isEdited")),
                    serializeBoolean(jsonObject.optBoolean("isSpam")),
                    serializeBoolean(jsonObject.optBoolean("isDeleted")));

            final int pID = database.execQuery(String.format("CALL insert_post(%s)", values),
                    result -> {
                        result.next();
                        return result.getInt("ID");
                    });

            jsonObject.put("id", pID);
            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | ClassCastException | NullPointerException | SQLException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    public static void postDetails(Database database, String id, JSONObject response) throws SQLException {
        database.execQuery(String.format("SELECT * FROM post WHERE pID=%s", id),
                result -> {
                    result.next();
                    postDetailstoJSON(result, response);
                });
    }

    public static void postDetailstoJSON(ResultSet result, JSONObject response) throws SQLException {
        response.put("date", DATE_FORMAT.format(new Date(result.getTimestamp("date").getTime())));
        response.put("dislikes", result.getInt("dislikes"));
        response.put("forum", result.getString("forum"));
        response.put("id", result.getInt("pID"));
        response.put("isApproved", result.getBoolean("isApproved"));
        response.put("isDeleted", result.getBoolean("isDeleted"));
        response.put("isEdited", result.getBoolean("isEdited"));
        response.put("isHighlighted", result.getBoolean("isHighlighted"));
        response.put("isSpam", result.getBoolean("isSpam"));
        response.put("likes", result.getInt("likes"));
        response.put("message", result.getString("message"));
        response.put("parent", result.getObject("parent") == null ? JSONObject.NULL : result.getObject("parent"));
        response.put("points", result.getInt("points"));
        response.put("thread", result.getInt("thread"));
        response.put("user", result.getString("user"));
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String id = params.get("post")[0];
            final JSONObject response = new JSONObject();
            postDetails(database, id, response);

            if (params.containsKey("related")) {
                final List<String> related = Arrays.asList(params.get("related"));
                if (related.contains("user")) {
                    final JSONObject user = new JSONObject();
                    User.userDetails(database, response.getString("user"), user);
                    response.put("user", user);
                }
                if (related.contains("forum")) {
                    final JSONObject forum = new JSONObject();
                    Forum.forumDetails(database, response.getString("forum"), forum);
                    response.put("forum", forum);
                }
                if (related.contains("thread")) {
                    final JSONObject thread = new JSONObject();
                    ForumThread.threadDetails(database, response.getString("thread"), thread);
                    response.put("thread", thread);
                }
            }

            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String table = (params.containsKey("forum") ? "forum" : "thread");
            final String query = String.format("SELECT * FROM post WHERE %s='%s' %s %s %s",
                    table,
                    params.get(table)[0],
                    (params.containsKey("since") ? String.format("AND date >= '%s'", params.get("since")[0]) : ""),
                    String.format("ORDER BY date %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();
                        while (result.next()) {
                            final JSONObject post = new JSONObject();
                            Post.postDetailstoJSON(result, post);
                            jsonArray.put(post);
                        }

                        jsonResult.put("code", 0);
                        jsonResult.put("response", jsonArray);
                    });
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (NullPointerException e) {
            jsonResult.put("code", 3);
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(
                    String.format("UPDATE post SET isDeleted=1 WHERE pID=%s; UPDATE thread SET posts=posts-1 WHERE tID=(SELECT thread FROM post WHERE pID=%1$s)",
                            jsonObject.getString("post"))
            );

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("restore")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restore(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(
                    String.format("UPDATE post SET isDeleted=0 WHERE pID=%s; UPDATE thread SET posts=posts+1 WHERE tID=(SELECT thread FROM post WHERE pID=%1$s)",
                            jsonObject.getString("post"))
            );

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("post");

            database.execUpdate(String.format("UPDATE post SET message='%s' WHERE pID=%s", jsonObject.get("message"), id));

            final JSONObject response = new JSONObject();
            postDetails(database, id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response vote(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("post");
            final int vote = jsonObject.getInt("vote");
            String likes = "likes=likes+1";
            if (vote < 0) likes = "dislikes=dislikes+1";

            database.execUpdate(String.format("UPDATE post SET points=points+(%d), %s WHERE pID=%s", vote, likes, id));

            final JSONObject response = new JSONObject();
            postDetails(database, id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 1);
            jsonResult.put("response", "Not found");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException | NumberFormatException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
