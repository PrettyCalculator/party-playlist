package com.partyplaylist.servlet;

import com.partyplaylist.model.enums.PartyPhase;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("phases", PartyPhase.values());
        req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
    }
}
