package com.example.gaming;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

@WebServlet("/FrontPage") // matches form action
public class FrontPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("Login.html");
            return;
        }

        String username = (String) session.getAttribute("username");
        int credits = getCredits(username);

        // set attributes for JSP
        req.setAttribute("username", username);
        req.setAttribute("credits", credits);

        // forward to your existing FrontPage.jsp
        req.getRequestDispatcher("FrontPage.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("Login.html");
            return;
        }

        String username = (String) session.getAttribute("username");
        int credits = getCredits(username);

        int bet;
        int guess;

        try {
            bet = Integer.parseInt(req.getParameter("bet"));
            guess = Integer.parseInt(req.getParameter("guess"));
        } catch (NumberFormatException e) {
            req.setAttribute("message", "Please enter valid numbers.");
            doGet(req, resp);
            return;
        }

        if (bet <= 0 || bet > credits) {
            req.setAttribute("message", "Invalid bet amount. You have " + credits + " credits.");
            doGet(req, resp);
            return;
        }

        if (guess < 1 || guess > 10) {
            req.setAttribute("message", "Guess must be between 1 and 10.");
            doGet(req, resp);
            return;
        }

        int randomNum = new Random().nextInt(10) + 1;
        boolean win = (guess == randomNum);

        int newCredits = credits;
        String result;

        if (win) {
            int prize = bet * 10;
            newCredits += prize;
            result = "You guessed " + guess + " correctly! The number was " + randomNum + ". You won " + prize + " credits!";
        } else {
            newCredits -= bet;
            result = " You guessed " + guess + " but the number was " + randomNum + ". You lost " + bet + " credits.";
        }

        // update credits in DB
        updateCredits(username, newCredits);

        // set attributes for JSP
        req.setAttribute("username", username);
        req.setAttribute("credits", newCredits);
        req.setAttribute("message", result);

        // forward to your existing FrontPage.jsp
        req.getRequestDispatcher("FrontPage.jsp").forward(req, resp);
    }

    // --- helper methods ---
    private int getCredits(String username) {
        int credits = 0;
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT credits FROM players WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) credits = rs.getInt("credits");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return credits;
    }

    private void updateCredits(String username, int newCredits) {
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE players SET credits=? WHERE username=?");
            ps.setInt(1, newCredits);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}