package com.patp.sistema.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.patp.sistema.service.SessaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    private final SessaoService sessaoService;

    public AutenticacaoInterceptor(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String caminho = request.getRequestURI();

        // Rotas que não precisam de login
        if (caminho.equals("/api/usuarios/login")
                || caminho.equals("/api/usuarios/cadastro")) {

            return true;
        }

        // OPTIONS é usado pelo navegador em requisições CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String autorizacao =
                request.getHeader("Authorization");

        if (autorizacao == null ||
                !autorizacao.startsWith("Bearer ")) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"erro\":\"Usuário não autenticado.\"}"
            );

            return false;
        }

        String token =
                autorizacao.substring(7);

        try {

            sessaoService.buscarUsuario(token);

            return true;

        } catch (RuntimeException e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"erro\":\"Sessão inválida ou expirada.\"}"
            );

            return false;
        }
    }
}
