package com.vinechallenge.servlet.vine_api_servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import vinechallenge.model.Response;
import vinechallenge.statuses.StatusRetrieval;

/**
 * Servlet implementation class VineChallengeServlet
 */
@WebServlet(name = "VineChallengeServlet", urlPatterns = { "/statuses" })
public class VineChallengeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PARAM1 = "screen_names";
    private static final String PARAM2 = "count";
    private static final String PARAM3 = "cursor";
    private StatusRetrieval statuses;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public VineChallengeServlet() {
        super();
        statuses = new StatusRetrieval();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String screenNames = null;
        String countStr = null;
        String cursor = null;

        // validate our request params and retrieve the param values if they're
        // valid
        Enumeration<String> params = request.getParameterNames();
        String nextParam;
        while (params.hasMoreElements()) {
            nextParam = params.nextElement();
            if (!nextParam.equalsIgnoreCase(PARAM1) && !nextParam.equalsIgnoreCase(PARAM2)
                    && !nextParam.equalsIgnoreCase(PARAM3)) {
                // if we have a param that doesn't match one of the params we
                // accept, throw a 404 error
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid parameters, only accept screen_names, count, and cursor as parameters");
                return;
            } else {
                switch (nextParam.toLowerCase()) {
                case PARAM1:
                    screenNames = request.getParameter(nextParam);
                    break;
                case PARAM2:
                    countStr = request.getParameter(nextParam);
                    break;
                case PARAM3:
                    cursor = request.getParameter(nextParam);
                    break;
                }
            }
        }

        // ensure screen_names and count were included in request. Ensure count
        // is an integer
        if (screenNames == null || countStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "screen_name and count are mandatory parameters, please try again");
            return;
        }
        int count = -1;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "count must be an integer");
            return;
        }

        if (count > 100 || count < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Please enter a valid count (between 1 - 100)");
            return;
        }

        Response responseObj = statuses.retrieveStatus(screenNames.split(","), count, cursor);

        // check if an error occured
        if (responseObj == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error occured during request, please try again");
            return;
        }

        PrintWriter writer = response.getWriter();
        writer.write(createResponse(responseObj));
    }

    /*
     * Creates a json string response
     */
    private String createResponse(Response responseObj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(responseObj);
    }
}
