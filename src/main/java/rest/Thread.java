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
@Path("/thread")
public class Thread {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            String values = String.format("'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'",
                    jsonObject.get("forum"), jsonObject.get("title"), (jsonObject.getBoolean("isClosed") ? '1' : '0'), jsonObject.get("user"), jsonObject.get("date"), jsonObject.get("message"), jsonObject.get("slug"),
                    jsonObject.has("isDeleted") ? (jsonObject.getBoolean("isDeleted") ? '1' : '0') : '0').replaceAll("'null'", "null");

            int tID = RestApplication.DATABASE.execUpdate(String.format("INSERT INTO thread (forum, title, isClosed, user, date, message, slug, isDeleted) VALUES (%s)", values));
            jsonObject.put("id", tID);

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

    public static void threadDetails(String id, JSONObject response) throws SQLException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        RestApplication.DATABASE.execQuery(String.format("SELECT * FROM thread WHERE tID=%s", id),
                result -> {
                    result.next();
                    response.put("date", df.format(new Date(result.getTimestamp("date").getTime())));
                    response.put("dislikes", result.getInt("dislikes"));
                    response.put("forum", result.getString("forum"));
                    response.put("id", result.getInt("tID"));
                    response.put("isClosed", result.getBoolean("isClosed"));
                    response.put("isDeleted", result.getBoolean("isDeleted"));
                    response.put("likes", result.getInt("likes"));
                    response.put("message", result.getString("message"));
                    response.put("points", result.getInt("points"));
                    response.put("posts", result.getInt("posts"));
                    response.put("slug", result.getString("slug"));
                    response.put("title", result.getString("title"));
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
            String id = params.get("thread")[0];
            JSONObject response = new JSONObject();
            threadDetails(id, response);

            if (params.containsKey("related")) {
                String[] related = params.get("related");
                if (Arrays.asList(related).contains("user")) {
                    RestApplication.DATABASE.execQuery(String.format("SELECT email FROM thread t JOIN user u on t.uID=u.uID WHERE tID=%s", id),
                            result -> {
                                result.next();
                                JSONObject user = new JSONObject();
                                User.userDetails(result.getString("email"), user);
                                response.put("user", user);
                            });
                }
                if (Arrays.asList(related).contains("forum")) {
                    RestApplication.DATABASE.execQuery(String.format("SELECT short_name FROM thread t JOIN forum f on t.fID=f.fID WHERE tID=%s", id),
                            result -> {
                                result.next();
                                JSONObject forum = new JSONObject();
                                Forum.forumDetails(result.getString("short_name"), forum);
                                response.put("forum", forum);
                            });
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
        } catch (RuntimeException e) {
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

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        JSONObject jsonResult = new JSONObject();

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    @POST
    @Path("open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET isClosed=0 WHERE tID=%s", jsonObject.getString("thread")));

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
    @Path("close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response close(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET isClosed=1 WHERE tID=%s", jsonObject.getString("thread")));

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
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET isDeleted=1, posts=0 WHERE tID=%s", jsonObject.getString("thread")));
            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET isDeleted=1 WHERE tID=%s", jsonObject.getString("thread")));


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
            String id = jsonObject.getString("thread");

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET isDeleted=0, posts=(SELECT COUNT(*) FROM post WHERE tID=%s) WHERE tID=%s", id, id));
            RestApplication.DATABASE.execUpdate(String.format("UPDATE post SET isDeleted=0 WHERE tID=%s", jsonObject.getString("thread")));

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
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribe(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("INSERT INTO user_thread VALUES ((SELECT uID FROM user WHERE email='%s'), %s)", jsonObject.getString("user"), jsonObject.getString("thread")));

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
    @Path("unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unsubscribe(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);

            RestApplication.DATABASE.execUpdate(String.format("DELETE FROM user_thread WHERE uID=(SELECT uID FROM user WHERE email='%s') AND tID=%s", jsonObject.getString("user"), jsonObject.getString("thread")));

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
            String id = jsonObject.getString("thread");

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET message='%s', slug='%s' WHERE tID=%s", jsonObject.get("message"), jsonObject.get("slug"), id));

            JSONObject response = new JSONObject();
            threadDetails(id, response);
            jsonResult.put("code", 0);
            jsonResult.put("response", response);
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

    @POST
    @Path("vote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response vote(final String input, @Context HttpServletRequest request) {
        JSONObject jsonResult = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject(input);
            String id = jsonObject.getString("thread");
            int vote = jsonObject.getInt("vote");
            String likes = "likes=likes+1";
            if (vote < 0) likes = "dislikes=dislikes+1";

            RestApplication.DATABASE.execUpdate(String.format("UPDATE thread SET points=points+(%d), %s WHERE tID=%s", vote, likes, id));

            JSONObject response = new JSONObject();
            threadDetails(id, response);
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
