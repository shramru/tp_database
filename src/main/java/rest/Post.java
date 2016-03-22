package rest;

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
@Singleton
@Path("/post")
public class Post {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("all")
    public Response create(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            String values = String.format("'%s', %s, '%s', '%s', '%s', %s, %s, %s, %s, %s, %s",
                    jsonObject.getString("date"), jsonObject.getString("thread"), jsonObject.getString("message"), jsonObject.getString("user"), jsonObject.getString("forum"), jsonObject.getString("parent"),
                    jsonObject.has("isApproved") ? (jsonObject.getBoolean("isApproved") ? '1' : '0') : '0',  jsonObject.has("isHighlighted") ? (jsonObject.getBoolean("isHighlighted") ? '1' : '0') : '0',
            jsonObject.has("isEdited") ? (jsonObject.getBoolean("isEdited") ? '1' : '0') : '0',  jsonObject.has("isSpam") ? (jsonObject.getBoolean("isSpam") ? '1' : '0') : '0',
            jsonObject.has("isDeleted") ? (jsonObject.getBoolean("isDeleted") ? '1' : '0') : '0');

            int pID = RestApplication.DATABASE.execUpdate(
                    String.format("INSERT INTO post (date, tID, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (%s)", values));
            jsonObject.put("id", pID);

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (RuntimeException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    public static void postDetails(String id, JSONObject response) throws SQLException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        try {
            String id = params.get("post")[0];
            JSONObject response = new JSONObject();
            postDetails(id, response);

            if (params.containsKey("related")) {
                String[] related = params.get("related");
                if (Arrays.asList(related).contains("user")) {
                    RestApplication.DATABASE.execQuery(String.format("SELECT email FROM post p JOIN user u on p.uID=u.uID WHERE pID=%s", id),
                            result -> {
                                result.next();
                                JSONObject user = new JSONObject();
                                User.userDetails(result.getString("email"), user);
                                response.put("user", user);
                            });
                }
                if (Arrays.asList(related).contains("forum")) {
                    RestApplication.DATABASE.execQuery(String.format("SELECT short_name FROM post p JOIN forum f on p.fID=f.fID WHERE pID=%s", id),
                            result -> {
                                result.next();
                                JSONObject forum = new JSONObject();
                                Forum.forumDetails(result.getString("short_name"), forum);
                                response.put("forum", forum);
                            });
                }
                if (Arrays.asList(related).contains("thread")) {
                    JSONObject thread = new JSONObject();
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
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

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
        } catch (NoSuchElementException e) {
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
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

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
        } catch (NoSuchElementException e) {
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
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);
            String id = jsonObject.getString("post");

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET message='%s' WHERE pID=%s", jsonObject.get("message"), id));

            JSONObject response = new JSONObject();
            postDetails(id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
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
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);
            String id = jsonObject.getString("post");
            int vote = jsonObject.getInt("vote");
            String likes = "likes=likes+1";
            if (vote < 0) likes = "dislikes=dislikes+1";

            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET points=points+(%d), %s WHERE pID=%s", vote, likes, id));

            JSONObject response = new JSONObject();
            postDetails(id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
        } catch (SQLException e) {
            jsonResult.put("code", 5);
            jsonResult.put("response", "User exists");
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }
}
