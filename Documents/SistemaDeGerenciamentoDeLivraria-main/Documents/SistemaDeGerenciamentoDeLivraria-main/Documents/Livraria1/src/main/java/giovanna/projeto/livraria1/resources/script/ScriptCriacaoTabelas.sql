-- ************************************************************
-- SCRIPT 2: Criação das Tabelas, Funções e Triggers no banco "Livraria"
-- ************************************************************

-- Criação da tabela de gêneros
CREATE TABLE IF NOT EXISTS public.generos
(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.generos
    OWNER TO postgres;

-- Criação da tabela de livros
CREATE TABLE IF NOT EXISTS public.livros
(
    etiqueta_livro INTEGER PRIMARY KEY,
    titulo VARCHAR(80),
    autor VARCHAR(80),
    editora VARCHAR(50),
    genero_id INTEGER,
    isbn VARCHAR(13) UNIQUE,
    data_publicacao DATE,
    data_inclusao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_alteracao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_genero FOREIGN KEY (genero_id) REFERENCES public.generos (id)
);

ALTER TABLE IF EXISTS public.livros OWNER TO postgres;

-- Função para atualizar a data de alteração
CREATE OR REPLACE FUNCTION public.fn_atualizar_data_alteracao() 
RETURNS trigger AS $$
BEGIN
    NEW.data_alteracao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Função para gerar etiqueta única
CREATE OR REPLACE FUNCTION public.fn_gerar_etiqueta()
RETURNS trigger AS $$
DECLARE
    etiqueta_nova INTEGER;
BEGIN
    LOOP
        etiqueta_nova := FLOOR(random() * (9999 - 1000 + 1) + 1000);
        -- Verifica se o valor já existe
        IF NOT EXISTS (SELECT 1 FROM public.livros WHERE etiqueta_livro = etiqueta_nova) THEN
            NEW.etiqueta_livro := etiqueta_nova;
            EXIT;
        END IF;
    END LOOP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

ALTER FUNCTION public.fn_gerar_etiqueta()
    OWNER TO postgres;

-- Criação dos triggers
CREATE TRIGGER trg_data_alteracao
    BEFORE UPDATE ON public.livros
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_atualizar_data_alteracao();

CREATE TRIGGER trg_gerar_etiqueta
    BEFORE INSERT ON public.livros
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_gerar_etiqueta();

-- Criação da tabela de livros semelhantes
CREATE TABLE IF NOT EXISTS public.livros_semelhantes
(
    etiqueta_livro INTEGER NOT NULL,
    etiqueta_semelhante INTEGER NOT NULL,
    PRIMARY KEY (etiqueta_livro, etiqueta_semelhante),
    FOREIGN KEY (etiqueta_livro) REFERENCES public.livros (etiqueta_livro) ON DELETE CASCADE,
    FOREIGN KEY (etiqueta_semelhante) REFERENCES public.livros (etiqueta_livro) ON DELETE CASCADE
);

ALTER TABLE IF EXISTS public.livros_semelhantes OWNER TO postgres;

-- Ajustar propriedade de ownership das tabelas
ALTER TABLE IF EXISTS public.generos OWNER TO postgres;

-- ************************************************************
-- INSTRUÇÕES FINAIS:
-- 1. Após conectar-se ao banco "Livraria" no pgAdmin, abra este script no painel de consultas.
-- 2. Clique em "Executar" para rodar o script e criar as tabelas e funções necessárias.
-- ************************************************************