package mesh.api;

import json.json;
import mesh.DBManager;
import mesh.MeshResponse;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(value = "/api/user", name = "User api")
public class User extends HttpServlet {

    @GET
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        mesh.db.User me = (mesh.db.User)request.getSession().getAttribute("user");
        //TODO: check if user is null
        EntityManager em = DBManager.getManager();
        String uid = request.getParameter("id");
        MeshResponse meshResponse = new MeshResponse(200);
        Query query;
        if(uid == null || uid.isEmpty()) {
            query = em.createQuery("select U from User U order by U.fio asc");
        } else {
            query = em
                    .createQuery("select U from User U where U.id = :uid order by U.fio asc")
                    .setParameter("uid", Integer.parseInt(uid));
        }
        List<mesh.db.User> users = query.getResultList();
        meshResponse.setData(users.toArray());
        out.write(new json(meshResponse).toString());
    }

    @POST
    @Transactional
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        mesh.db.User me = (mesh.db.User) request.getSession().getAttribute("user");
        //TODO: check if user is null
        EntityManager em = DBManager.getManager();
        json jData = new json(request.getReader().lines().collect(Collectors.joining(" ")));
        MeshResponse meshResponse = new MeshResponse(200);
        String fio = jData.get("fio");
        String login = jData.get("login");
        String password = jData.get("password");
        Integer access = jData.get("access");
        mesh.db.User user = (mesh.db.User)em
                .createNativeQuery("insert into users(fio, login, password, rid) values(:fio, :login, :password, :rid) returning *", mesh.db.User.class)
                .setParameter("fio", fio)
                .setParameter("login", login)
                .setParameter("password", password)
                .setParameter("rid", access)
                .getSingleResult();
        meshResponse.setData(user);
        out.write(new json(meshResponse).toString());
    }
}
