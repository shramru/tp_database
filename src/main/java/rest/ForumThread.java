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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import static rest.Forum.DUPLICATE_ENTRY;

/**
 * Created by vladislav on 18.03.16.
 */
@SuppressWarnings("OverlyComplexMethod")
@Singleton
@Path("/thread")
public class ForumThread {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'",
                    jsonObject.get("forum"), jsonObject.get("title"), jsonObject.get("user"),
                    jsonObject.get("date"), jsonObject.get("message"), jsonObject.get("slug"),
                    (jsonObject.getBoolean("isClosed") ? '1' : '0'),
                    jsonObject.has("isDeleted") ? (jsonObject.getBoolean("isDeleted") ? '1' : '0') : '0');

            final int tID = database.execUpdate(
                    String.format("INSERT INTO thread (forum, title, user, date, message, slug, isClosed, isDeleted) VALUES (%s)", values));
            jsonObject.put("id", tID);

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
            System.out.println("Thread invalid error:");
            System.out.println(e.getMessage());
        } catch (NoSuchElementException | ClassCastException | NullPointerException | SQLException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
            System.out.println("Thread unknown error:");
            System.out.println(e.getMessage());
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    public static void threadDetails(Database database, String id, JSONObject response) throws SQLException {
        database.execQuery(String.format("SELECT * FROM thread WHERE tID=%s", id),
                result -> {
                    result.next();
                    threadDetailstoJSON(result, response);
                });
    }

    public static void threadDetailstoJSON(ResultSet result, JSONObject response) throws SQLException {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String id = params.get("thread")[0];
            final JSONObject response = new JSONObject();
            threadDetails(database, id, response);

            if (params.containsKey("related")) {
                final String[] related = params.get("related");
                if (Arrays.asList(related).contains("user")) {
                    final JSONObject user = new JSONObject();
                    User.userDetails(database, response.getString("user"), user);
                    response.put("user", user);
                }
                if (Arrays.asList(related).contains("forum")) {
                    final JSONObject forum = new JSONObject();
                    Forum.forumDetails(database, response.getString("forum"), forum);
                    response.put("forum", forum);
                }
                if (Arrays.asList(related).contains("thread"))
                    throw new NullPointerException();
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
            final String table = (params.containsKey("forum") ? "forum" : "user");
            final String query = String.format("SELECT * FROM thread WHERE %s='%s' %s %s %s",
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
                            final JSONObject thread = new JSONObject();
                            threadDetailstoJSON(result, thread);
                            jsonArray.put(thread);
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

    @GET
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String tID = params.get("thread")[0];
            String order = "ORDER BY date DESC";
            String sort = "";
            if (params.containsKey("sort")) {
                sort = params.get("sort")[0];
                if (sort.equals("tree")) {
                    order = "ORDER BY SUBSTRING_INDEX(mpath,'.',1) DESC, mpath ASC";
                } else if (sort.equals("parent_tree")) {
                    String subquery = String.format("SELECT DISTINCT SUBSTRING_INDEX(mpath,'.',1) as head FROM post WHERE thread=%s%s ORDER BY head DESC",
                    tID, (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
                    if (params.containsKey("order"))
                        subquery = subquery.replace("DESC", params.get("order")[0]);
                    if (params.containsKey("limit"))
                        subquery += " LIMIT " + params.get("limit")[0];
                    order = String.format("AND SUBSTRING_INDEX(mpath,'.',1) BETWEEN \n" +
                            "(SELECT MIN(t.head) FROM \n" +
                            "(%s) t)\n" +
                            "AND\n" +
                            "(SELECT MAX(t.head) FROM\n" +
                            "(%s) t)\n" +
                            "ORDER BY SUBSTRING_INDEX(mpath,'.',1) DESC, mpath ASC", subquery, subquery);
                }
            }

            String query = String.format("SELECT * FROM post WHERE thread=%s%s %s", tID,
                    (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""), order) ;
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit") && !sort.equals("parent_tree"))
                query += " LIMIT " + params.get("limit")[0];

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
    @Path("open")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(String.format("UPDATE thread SET isClosed=0 WHERE tID=%s", jsonObject.getString("thread")));

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
    @Path("close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response close(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(String.format("UPDATE thread SET isClosed=1 WHERE tID=%s", jsonObject.getString("thread")));

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
    @Path("remove")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(
                    String.format("UPDATE thread SET isDeleted=1, posts=0 WHERE tID=%s; UPDATE post SET isDeleted=1 WHERE thread=%s",
                            jsonObject.getString("thread"), jsonObject.getString("thread"))
            );

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
    public Response restore(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("thread");

            database.execUpdate(
                    String.format("UPDATE thread SET isDeleted=0, posts=(SELECT COUNT(*) FROM post WHERE thread=%s) WHERE tID=%s;" +
                            "UPDATE post SET isDeleted=0 WHERE thread=%s",
                            id, id, jsonObject.getString("thread"))
            );

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
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribe(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(String.format("INSERT INTO user_thread (user, tID) VALUES ('%s', %s)",
                    jsonObject.getString("user"), jsonObject.getString("thread"))
            );

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                subscribeDuplicate(input, jsonResult);
            } else {
                jsonResult.put("code", 4);
                jsonResult.put("response", "Unknown error");
                System.out.println("Thread sql error:");
                System.out.println(e.getMessage());
            }
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    private static void subscribeDuplicate(String input, JSONObject jsonResult) {
        try {
            final JSONObject jsonObject = new JSONObject(input);
            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (ParseException e1) {
            jsonResult.put("code", (e1.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
            System.out.println("Forum invalid error:");
            System.out.println(e1.getMessage());
        }
    }

    @POST
    @Path("unsubscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unsubscribe(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            database.execUpdate(String.format("DELETE FROM user_thread WHERE user='%s' AND tID=%s",
                    jsonObject.getString("user"), jsonObject.getString("thread"))
            );

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
    public Response update(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("thread");

            database.execUpdate(String.format("UPDATE thread SET message='%s', slug='%s' WHERE tID=%s",
                    jsonObject.get("message"), jsonObject.get("slug"), id)
            );

            final JSONObject response = new JSONObject();
            threadDetails(database, id, response);
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
    public Response vote(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String id = jsonObject.getString("thread");
            final int vote = jsonObject.getInt("vote");
            String likes = "likes=likes+1";
            if (vote < 0) likes = "dislikes=dislikes+1";

            database.execUpdate(String.format("UPDATE thread SET points=points+(%d), %s WHERE tID=%s", vote, likes, id));

            final JSONObject response = new JSONObject();
            threadDetails(database, id, response);
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
