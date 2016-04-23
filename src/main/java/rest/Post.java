package rest;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by vladislav on 18.03.16.
 */
@SuppressWarnings("OverlyComplexMethod")
@Singleton
@Path("/post")
public class Post {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', %s, '%s', '%s', '%s', %s, %s, %s, %s, %s, %s",
                    jsonObject.getString("date"), jsonObject.getString("thread"), jsonObject.getString("message"), jsonObject.getString("user"), jsonObject.getString("forum"), jsonObject.getString("parent"),
                    jsonObject.has("isApproved") ? (jsonObject.getBoolean("isApproved") ? '1' : '0') : '0',  jsonObject.has("isHighlighted") ? (jsonObject.getBoolean("isHighlighted") ? '1' : '0') : '0',
            jsonObject.has("isEdited") ? (jsonObject.getBoolean("isEdited") ? '1' : '0') : '0',  jsonObject.has("isSpam") ? (jsonObject.getBoolean("isSpam") ? '1' : '0') : '0',
            jsonObject.has("isDeleted") ? (jsonObject.getBoolean("isDeleted") ? '1' : '0') : '0');

            final int pID = RestApplication.DATABASE.execUpdate(
                    String.format("INSERT INTO post (date, tID, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (%s)", values));

            jsonObject.put("id", pID);
            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
            System.out.println("Post invalid error:");
            System.out.println(e.getMessage());
        } catch (NoSuchElementException | ClassCastException | NullPointerException | SQLException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
            System.out.println("Post unknown error:");
            System.out.println(e.getMessage());
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    public static void postDetails(String id, JSONObject response) throws SQLException {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        RestApplication.DATABASE.execQuery(String.format("SELECT * FROM post WHERE pID=%s", id),
                result -> {
                    result.next();
                    response.put("date", df.format(new Date(result.getTimestamp("date").getTime())));
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
                    response.put("thread", result.getInt("tID"));
                    response.put("user", result.getString("user"));
                });
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String id = params.get("post")[0];
            final JSONObject response = new JSONObject();
            postDetails(id, response);

            if (params.containsKey("related")) {
                final String[] related = params.get("related");
                if (Arrays.asList(related).contains("user")) {
                    final JSONObject user = new JSONObject();
                    User.userDetails(response.getString("user"), user);
                    response.put("user", user);
                }
                if (Arrays.asList(related).contains("forum")) {
                    final JSONObject forum = new JSONObject();
                    Forum.forumDetails(response.getString("forum"), forum);
                    response.put("forum", forum);
                }
                if (Arrays.asList(related).contains("thread")) {
                    final JSONObject thread = new JSONObject();
                    Thread.threadDetails(response.getString("thread"), thread);
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
    public Response list(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            String query = "";
            if (params.containsKey("forum")) {
                final String shortName = params.get("forum")[0];
                query = String.format("SELECT pID FROM post WHERE forum='%s'%s ORDER BY date DESC", shortName,
                        (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
            } else if (params.containsKey("thread")) {
                final String id = params.get("thread")[0];
                query = String.format("SELECT pID FROM post WHERE tID=%s%s ORDER BY date DESC", id,
                        (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
            }

            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject post = new JSONObject();
                            postDetails(result.getString("pID"), post);
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
    public Response remove(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET isDeleted=1 WHERE pID=%s", jsonObject.getString("post")));
            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET posts=posts-1 WHERE tID=(SELECT tID FROM post WHERE pID=%s)", jsonObject.getString("post")));

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
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
    public Response restore(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET isDeleted=0 WHERE pID=%s", jsonObject.getString("post")));
            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET posts=posts+1 WHERE tID=(SELECT tID FROM post WHERE pID=%s)", jsonObject.getString("post")));

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
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
    public Response update(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("post");

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET message='%s' WHERE pID=%s", jsonObject.get("message"), id));

            final JSONObject response = new JSONObject();
            postDetails(id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
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
    public Response vote(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("post");
            final int vote = jsonObject.getInt("vote");
            String likes = "likes=likes+1";
            if (vote < 0) likes = "dislikes=dislikes+1";

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET points=points+(%d), %s WHERE pID=%s", vote, likes, id));

            final JSONObject response = new JSONObject();
            postDetails(id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
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
