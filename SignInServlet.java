import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SignInServlet extends HTTPServlet{
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException{

            String username = request.doGet("username");

            String password = request.doGet("password")

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

        

    }
}
