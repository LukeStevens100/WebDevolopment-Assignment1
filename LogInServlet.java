import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class LogInServlet extends HTTPServlet{

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException{

            String username = request.doGet("username");

            String password = request.doGet("password");

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            String pwdHash = PasswordUtil.sha256Hex(password);

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT credits, password_hash FROM players WHERE gamer_tag = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, gamerTag);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        int credits = rs.getInt("credits");
                        if (storedHash.equals(pwdHash)) {
                            HttpSession session = req.getSession(true);
                            session.setAttribute("gamerTag", gamerTag);
                            session.setAttribute("credits", credits);
                            resp.sendRedirect("dashboard");
                            return;
                        } else {
                            out.println("<h3>Invalid password.</h3>");
                            out.println("<a href='login.html'>Back to Login</a>");
                            return;
                        }
                    } else {
                        out.println("<h3>Gamer tag not found.</h3>");
                        out.println("<a href='register.html'>Register</a>");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(out);
            out.println("<h3>Database error.</h3>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("Login.html");
    }
}

        
    

        