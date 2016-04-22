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
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

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
    public Response create(final String input, @Context HttpServletRequest request) {
        final JSONObject jsonResult = new JSONObject();

        try {
            final JSONObject jsonObject = new JSONObject(input);

            final String values = String.format("'%s', '%s', '%s'",
                    jsonObject.getString("name"), jsonObject.getString("short_name"), jsonObject.getString("user"));

            final int fID = RestApplication.DATABASE.execUpdate(String.format("INSERT INTO forum (name, short_name, user) VALUES (%s)", values));
            jsonObject.put("id", fID);

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

    public static void forumDetails(String shortName, JSONObject response) throws SQLException {
        RestApplication.DATABASE.execQuery(String.format("SELECT fID, name, user FROM forum WHERE short_name='%s'", shortName),
                result -> {
                    result.next();
                    response.put("short_name", shortName);
                    response.put("id", result.getInt("fID"));
                    response.put("name", result.getString("name"));
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
            final String shortName = params.get("forum")[0];
            final JSONObject response = new JSONObject();
            forumDetails(shortName, response);

            if (params.containsKey("related")) {
                final JSONObject user = new JSONObject();
                User.userDetails(response.getString("user"), user);
                response.put("user", user);
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
    public Response listPosts(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String shortName = params.get("forum")[0];
            String query = String.format("SELECT pID FROM post WHERE forum='%s'%s ORDER BY date DESC", shortName,
                    (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject post = new JSONObject();
                            Post.postDetails(result.getString("pID"), post);

                            if (params.containsKey("related")) {
                                final String[] param = params.get("related");
                                if(Arrays.asList(param).contains("thread")) {
                                    final JSONObject thread = new JSONObject();
                                    Thread.threadDetails(post.getString("thread"), thread);
                                    post.put("thread", thread);
                                }
                                if(Arrays.asList(param).contains("forum")) {
                                    final JSONObject forum = new JSONObject();
                                    Forum.forumDetails(post.getString("forum"), forum);
                                    post.put("forum", forum);
                                }
                                if(Arrays.asList(param).contains("user")) {
                                    final JSONObject user = new JSONObject();
                                    User.userDetails(post.getString("user"), user);
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
    public Response listThreads(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String shortName = params.get("forum")[0];
            String query = String.format("SELECT tID FROM thread WHERE forum='%s'%s ORDER BY date DESC", shortName,
                    (params.containsKey("since") ? String.format(" AND date >= '%s'", params.get("since")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject thread = new JSONObject();
                            Thread.threadDetails(result.getString("tID"), thread);

                            if (params.containsKey("related")) {
                                final String[] param = params.get("related");
                                if(Arrays.asList(param).contains("forum")) {
                                    final JSONObject forum = new JSONObject();
                                    Forum.forumDetails(thread.getString("forum"), forum);
                                    thread.put("forum", forum);
                                }
                                if(Arrays.asList(param).contains("user")) {
                                    final JSONObject user = new JSONObject();
                                    User.userDetails(thread.getString("user"), user);
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
    public Response listUsers(@Context HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final JSONObject jsonResult = new JSONObject();

        try {
            final String shortName = params.get("forum")[0];
            String query = String.format("SELECT DISTINCT email FROM post p JOIN user u ON p.user=u.email WHERE forum='%s'%s ORDER BY name DESC", shortName,
                    (params.containsKey("since_id") ? String.format(" AND uID >= %s", params.get("since_id")[0]) : ""));
            if (params.containsKey("order"))
                query = query.replace("DESC", params.get("order")[0]);
            if (params.containsKey("limit"))
                query += " LIMIT " + params.get("limit")[0];

            RestApplication.DATABASE.execQuery(query,
                    result -> {
                        final JSONArray jsonArray = new JSONArray();

                        while (result.next()) {
                            final JSONObject user = new JSONObject();
                            User.userDetails(result.getString("email"), user);
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
