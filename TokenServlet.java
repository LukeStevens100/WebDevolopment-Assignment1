import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class TokenServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("Login.html");
            return;
        }

        String username = (String) session.getAttribute("username");
        String action = req.getParameter("action");
        String amountStr = req.getParameter("amount");
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                out.println("<h3>Amount must be positive.</h3><a href='HomePage'>Back to Home Page</a>");
                return;
            }
        } catch (NumberFormatException e) {
            out.println("<h3>Invalid amount.</h3><a href='HomePage'>Back to Home Page</a>");
            return;
        }

        // Transaction: lock row, check balance, update
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String selectForUpdate = "SELECT credits FROM users WHERE username = ? FOR UPDATE";
                int currentCredits;
                try (PreparedStatement ps = conn.prepareStatement(selectForUpdate)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            out.println("<h3>User not found.</h3>");
                            return;
                        }
                        currentCredits = rs.getInt("credits");
                    }
                }

                int newCredits = currentCredits;
                if ("add".equals(action)) {
                    newCredits = currentCredits + amount;
                } else if ("spend".equals(action)) {
                    if (currentCredits - amount < 0) {
                        conn.rollback();
                        out.println("<h3>Cannot spend " + amount + " of credits. Balance not enough.</h3>");
                        out.println("<a href='FrontPage'>Back to FrontPage</a>");
                        return;
                    } else {
                        newCredits = currentCredits - amount;
                    }
                } else {
                    conn.rollback();
                    out.println("<h3>Invalid action.</h3>");
                    return;
                }

                String updateSql = "UPDATE users SET credits = ? WHERE username = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                    ps2.setInt(1, newCredits);
                    ps2.setString(2, username);
                    ps2.executeUpdate();
                }

                conn.commit();
                session.setAttribute("credits", newCredits);

                // Personalized message
                out.println("<!doctype html><html><head><meta charset='utf-8'><title>Update</title></head><body>");
                out.println("<h2>Transaction successful!</h2>");
                out.println("<p>Username: <strong>" + escapeHtml(username) + "</strong></p>");
                out.println("<p>Updated credits: <strong>" + newCredits + "</strong></p>");
                out.println("<p><a href='HomePage'>Back to HomePage</a></p>");
                out.println("</body></html>");

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace(out);
            out.println("<h3>Database error occurred.</h3>");
            out.println("<a href='HomePage'>Back to HomePage</a>");
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}