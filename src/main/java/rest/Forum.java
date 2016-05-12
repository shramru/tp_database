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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import static main.Helper.*;

/**
 * Created by vladislav on 18.03.16.
 */
@SuppressWarnings("OverlyComplexMethod")
@Singleton
@Path("/forum")
public class Forum {

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final String input, @Context Database database) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);
            final String values = String.format("'%s', '%s', '%s'",
                    jsonObject.getString("name"), jsonObject.getString("short_name"), jsonObject.getString("user"));

            final int fID = database.execUpdate(String.format("INSERT INTO forum (name, short_name, user) VALUES (%s)", values));
            jsonObject.put("id", fID);

            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                forumDuplicate(database, input, jsonResult);
            } else {
                jsonResult.put("code", 4);
                jsonResult.put("response", "Unknown error");
            }
        } catch (ParseException e) {
            jsonResult.put("code", (e.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        } catch (NoSuchElementException | NullPointerException | ClassCastException e) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        }

        return Response.status(Response.Status.OK).entity(jsonResult.toString()).build();
    }

    private static void forumDuplicate(Database database, String input, JSONObject jsonResult) {
        try {
            final JSONObject jsonObject = new JSONObject(input);
            database.execQuery(String.format("SELECT fID FROM forum WHERE short_name='%s'", jsonObject.getString("short_name")),
                    result -> {
                        result.next();
                        jsonObject.put("id", result.getInt("fID"));
                    });
            jsonResult.put("code", 0);
            jsonResult.put("response", jsonObject);
        } catch (SQLException e1) {
            jsonResult.put("code", 4);
            jsonResult.put("response", "Unknown error");
        } catch (ParseException e1) {
            jsonResult.put("code", (e1.getMessage().contains("not found") ? 3 : 2));
            jsonResult.put("response", "Invalid request");
        }
    }

    public static void forumDetails(Database database, String shortName, JSONObject response) throws SQLException {
        database.execQuery(String.format("SELECT * FROM forum WHERE short_name='%s'", shortName),
                result -> {
                    result.next();
                    forumDetailstoJSON(result, response);
                });
    }

    public static void forumDetailstoJSON(ResultSet result, JSONObject response) throws SQLException {
        response.put("short_name", result.getString("short_name"));
        response.put("id", result.getInt("fID"));
        response.put("name", result.getString("name"));
        response.put("user", result.getString("user"));
    }

    @GET
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String shortName = params.get("forum")[0];
            final JSONObject response = new JSONObject();
            forumDetails(database, shortName, response);

            if (params.containsKey("related")) {
                final List<String> related = Arrays.asList(params.get("related"));
                if (related.contains("user")) {
                    final JSONObject user = new JSONObject();
                    User.userDetails(database, response.getString("user"), user);
                    response.put("user", user);
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
    @Path("listPosts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPosts(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT * FROM post WHERE forum='%s' %s %s %s",
                    params.get("forum")[0],
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

                            if (params.containsKey("related")) {
                                final List<String> related = Arrays.asList(params.get("related"));
                                if (related.contains("thread")) {
                                    final JSONObject thread = new JSONObject();
                                    ForumThread.threadDetails(database, post.getString("thread"), thread);
                                    post.put("thread", thread);
                                }
                                if (related.contains("forum")) {
                                    final JSONObject forum = new JSONObject();
                                    Forum.forumDetails(database, post.getString("forum"), forum);
                                    post.put("forum", forum);
                                }
                                if (related.contains("user")) {
                                    final JSONObject user = new JSONObject();
                                    User.userDetails(database, post.getString("user"), user);
                                    post.put("user", user);
                                }
                            }

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

    @GET
    @Path("listThreads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listThreads(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT * FROM thread WHERE forum='%s' %s %s %s",
                    params.get("forum")[0],
                    (params.containsKey("since") ? String.format("AND date >= '%s'", params.get("since")[0]) : ""),
                    String.format("ORDER BY date %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject thread = new JSONObject();
                            ForumThread.threadDetailstoJSON(result, thread);

                            if (params.containsKey("related")) {
                                final List<String> related = Arrays.asList(params.get("related"));
                                if (related.contains("forum")) {
                                    final JSONObject forum = new JSONObject();
                                    Forum.forumDetails(database, thread.getString("forum"), forum);
                                    thread.put("forum", forum);
                                }
                                if (related.contains("user")) {
                                    final JSONObject user = new JSONObject();
                                    User.userDetails(database, thread.getString("user"), user);
                                    thread.put("user", user);
                                }
                            }

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
    @Path("listUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@Context HttpServletRequest request, @Context Database database) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String query = String.format("SELECT * FROM user WHERE email IN (SELECT DISTINCT user FROM post WHERE forum='%s') %s %s %s",
                    params.get("forum")[0],
                    (params.containsKey("since_id") ? String.format("AND uID >= %s", params.get("since_id")[0]) : ""),
                    String.format("ORDER BY name %s", ((params.containsKey("order") ? params.get("order")[0] : "desc"))),
                    (params.containsKey("limit") ? String.format("LIMIT %s", params.get("limit")[0]) : "")
            );

            database.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            User.userDetailstoJSON(database, result, user);
                            jsonArray.put(user);
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
}
