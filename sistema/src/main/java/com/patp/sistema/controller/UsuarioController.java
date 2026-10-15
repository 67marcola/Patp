package com.patp.sistema.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.patp.sistema.model.Usuario;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.SessaoService;
import com.patp.sistema.service.UsuarioService;
import com.patp.sistema.service.AprovacaoCadastroService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SessaoService sessaoService;
    private final AprovacaoCadastroService aprovacao;

    public UsuarioController(
            UsuarioService usuarioService,
            SessaoService sessaoService, AprovacaoCadastroService aprovacao) {

        this.usuarioService = usuarioService;
        this.sessaoService = sessaoService;
        this.aprovacao = aprovacao;
    }

    // Cadastro
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @RequestBody Usuario usuario) {

        Usuario salvo = usuarioService.cadastrar(usuario);
        if (aprovacao.modoInternet()) return ResponseEntity.accepted().body(
                new CadastroPendenteResponse("PENDENTE", AprovacaoCadastroService.MENSAGEM_PENDENTE));
        return ResponseEntity.ok(criarRespostaDeEntrada(salvo));
    }

    // Login
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.login(
                request.email(),
                request.senha()
        );

        return criarRespostaDeEntrada(usuario);
    }

    private LoginResponse criarRespostaDeEntrada(Usuario usuario) {

        String token = sessaoService.criarSessao(usuario);

        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getSetor(),
                usuario.getEmail(),
                usuario.getPapel()
        );
    }

    // Usuário atualmente logado
    @GetMapping("/me")
    public UsuarioResponse usuarioLogado(
            @RequestHeader("Authorization") String token) {

        Usuario usuario = sessaoService.buscarUsuario(
                removerBearer(token)
        );

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSetor(),
                usuario.getEmail(),
                usuario.getPapel()
        );
    }

    // Logout
    @PostMapping("/logout")
    public String logout(
            @RequestHeader("Authorization") String token) {

        sessaoService.encerrarSessao(
                removerBearer(token)
        );

        return "Logout realizado com sucesso.";
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {

        return usuarioService.buscarPorId(id);
    }

    // Remove "Bearer " do início do token
    private String removerBearer(String token) {

        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    // Dados recebidos no login
    public record CadastroPendenteResponse(String estado, String mensagem) {}

    public record LoginRequest(
            String email,
            String senha
    ) {}

    // Dados devolvidos no login
    public record LoginResponse(
            String token,
            Long id,
            String nome,
            String setor,
            String email,
            PapelUsuario papel
    ) {}

    // Dados seguros do usuário logado
    // A senha NÃO é enviada
    public record UsuarioResponse(
            Long id,
            String nome,
            String setor,
            String email,
            PapelUsuario papel
    ) {}
}
