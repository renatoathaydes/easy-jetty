package com.athaydes.easyjetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Easy-Jetty Sample code.
 */
public class Sample {

    public static void main(String[] args) {
        new EasyJetty().servlet("/hello", HelloServlet.class).start();
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getOutputStream().println("Hello");
        }
    }

}
