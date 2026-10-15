CREATE TABLE usuarios (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(255), setor VARCHAR(255), email VARCHAR(255) NOT NULL UNIQUE,
 senha VARCHAR(255) NOT NULL, papel ENUM('ADMINISTRADOR','FUNCIONARIO')
);
CREATE TABLE gerenciamentos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(255) NOT NULL, descricao VARCHAR(255), criador_id BIGINT,
 arquivado BOOLEAN DEFAULT FALSE, versao BIGINT DEFAULT 0,
 FOREIGN KEY (criador_id) REFERENCES usuarios(id)
);
CREATE TABLE etapas (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(255) NOT NULL, setor VARCHAR(255), categoria VARCHAR(20), ordem INT NOT NULL,
 gerenciamento_id BIGINT NOT NULL, FOREIGN KEY (gerenciamento_id) REFERENCES gerenciamentos(id)
);
CREATE TABLE processos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 numero_processo VARCHAR(255) NOT NULL UNIQUE, pessoa VARCHAR(255) NOT NULL,
 responsavel VARCHAR(255), status VARCHAR(255), prioridade VARCHAR(255), arquivado BOOLEAN DEFAULT FALSE,
 data_emissao DATE, prazo_etapa DATE, prazo_geral DATE, data_conclusao DATE, data_cancelamento DATE,
 motivo_cancelamento TEXT, observacoes TEXT, etapa_id BIGINT NOT NULL,
 FOREIGN KEY (etapa_id) REFERENCES etapas(id)
);
CREATE TABLE historicos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 acao VARCHAR(255) NOT NULL, descricao LONGTEXT NOT NULL, data_hora DATETIME(6) NOT NULL,
 usuario VARCHAR(255), processo_id BIGINT, FOREIGN KEY (processo_id) REFERENCES processos(id)
);
CREATE TABLE comentarios (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 texto TEXT NOT NULL, funcionario VARCHAR(255) NOT NULL, data_hora DATETIME(6) NOT NULL,
 processo_id BIGINT NOT NULL, FOREIGN KEY (processo_id) REFERENCES processos(id)
);
CREATE TABLE atividades (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 gerenciamento_id BIGINT NOT NULL, tipo VARCHAR(255) NOT NULL,
 alvo_tipo VARCHAR(255) NOT NULL, alvo_id BIGINT NOT NULL, alvo_nome LONGTEXT NOT NULL,
 autor_id BIGINT NOT NULL, autor_nome LONGTEXT NOT NULL, data_hora TIMESTAMP(6) NOT NULL,
 alteracoes_json LONGTEXT NOT NULL, historico_id BIGINT UNIQUE,
 INDEX idx_atividade_quadro_data_id(gerenciamento_id,data_hora,id),
 FOREIGN KEY (gerenciamento_id) REFERENCES gerenciamentos(id)
);
CREATE TABLE acessos_usuarios (
 usuario_id BIGINT NOT NULL PRIMARY KEY, aprovado BOOLEAN NOT NULL,
 aprovado_por BIGINT, aprovado_em DATETIME(6),
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id), FOREIGN KEY (aprovado_por) REFERENCES usuarios(id)
);
