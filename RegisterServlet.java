import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class RegisterServlet extends HttpServlet {
    private static final int STARTING_CREDITS = 500;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // Server-side verification: ensure passwords match exactly
        if (username == null || password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            out.println("<h3>Passwords do not match or missing fields. Please retry</h3>");
            out.println("<a href='register.html'>Back to Register</a>");
            return;
        }

        String pwdHash = PasswordUtil.sha256Hex(password);

        try (Connection conn = DBUtil.getConnection()) {
            String insertSql = "INSERT INTO users (user_name, password_hash, credits) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, pwdHash);
                ps.setInt(3, STARTING_CREDITS);
                ps.executeUpdate();
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            out.println("<h3>Gamer tag already taken. Choose another one.</h3>");
            out.println("<a href='register.html'>Back to Register</a>");
            return;
        } catch (SQLException e) {
            e.printStackTrace(out);
            out.println("<h3>Database error. Contact administrator.</h3>");
            return;
        }

        // Registration success: start session and redirect to dashboard
        HttpSession session = req.getSession(true);
        session.setAttribute("Username", username);
        session.setAttribute("credits", STARTING_CREDITS);

        resp.sendRedirect("dashboard");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("Register.html");
    }
}