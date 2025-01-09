package giovanna.projeto.livraria1.services;

import giovanna.projeto.livraria1.dao.LivroDAO;
import giovanna.projeto.livraria1.dao.GeneroDAO;
import giovanna.projeto.livraria1.model.Livro;
import giovanna.projeto.livraria1.model.Genero;
import giovanna.projeto.livraria1.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.xml.rpc.ServiceException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe responsável pelos serviços relacionados à manipulação de livros.
 * <p>
 * Esta classe fornece métodos para cadastrar, atualizar, excluir e buscar
 * livros no banco de dados, além de integrar-se a uma API externa para buscar
 * informações de livros por ISBN.
 * </p>
 *
 * <h2>Principais funcionalidades:</h2>
 * <ul>
 * <li>Validação de informações antes de cadastrar ou atualizar livros.</li>
 * <li>Conversão entre nome do gênero e id_genero.</li>
 * <li>Integração com a API externa para buscar informações de livros.</li>
 * <li>Operações CRUD no banco de dados utilizando a classe
 * {@link LivroDAO}.</li>
 * <li>Filtragem e busca de livros por critérios variados.</li>
 * </ul>
 *
 * @author
 */
public class LivroService {

    private static final Logger LOGGER = Logger.getLogger(LivroService.class.getName());
    private final LivroDAO livroDAO;
    private final GeneroDAO generoDAO;

    /**
     * Construtor padrão. Inicializa os DAOs responsáveis pelas operações no
     * banco de dados.
     */
    public LivroService() {
        this.livroDAO = new LivroDAO();
        this.generoDAO = new GeneroDAO();
    }

    /**
     * Valida as informações de um livro.
     *
     * @param livro Objeto {@link Livro} a ser validado.
     * @throws ServiceException Caso algum campo obrigatório esteja inválido ou
     * ausente.
     */
    private void validarLivro(Livro livro) throws ServiceException {
        LOGGER.info("Iniciando validação do livro...");
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new ServiceException("Título é obrigatório.");
        }
        if (livro.getIsbn() == null || livro.getIsbn().length() != 13) {
            throw new ServiceException("ISBN deve conter 13 caracteres.");
        }
        // Verifica se contém apenas números
        if (!livro.getIsbn().matches("\\d+")) {
            throw new ServiceException("ISBN deve conter apenas números.");
        }
        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            throw new ServiceException("Autor é obrigatório.");
        }
        if (livro.getEditora() == null || livro.getEditora().isBlank()) {
            throw new ServiceException("Editora é obrigatória.");
        }
        if (livro.getGeneroNome() == null || livro.getGeneroNome().isBlank()) {
            throw new ServiceException("Gênero é obrigatório.");
        }
        if (livro.getData_publicacao() == null) {
            throw new ServiceException("Gênero é obrigatório.");
        }
        LOGGER.info("Validação concluída com sucesso.");
    }

    /**
     * Cadastra um novo livro no banco de dados.
     *
     * @param livro Objeto {@link Livro} contendo as informações do livro.
     * @return O livro cadastrado com a etiqueta gerada.
     * @throws ServiceException Caso ocorra um erro durante a validação ou
     * cadastro.
     */
    public Livro cadastrarLivro(Livro livro) throws ServiceException {
        try {
            validarLivro(livro);

            // Converte o nome do gênero para id_genero
            int generoId = generoDAO.buscarIdGeneroPorNome(livro.getGeneroNome());
            livro.setGenero_id(generoId);

            livroDAO.inserirLivro(livro);
            LOGGER.info("Livro cadastrado com sucesso: " + livro.getTitulo());
            return livroDAO.busca_porISBN(livro.getIsbn());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar livro", ex);
            throw new ServiceException("Erro ao cadastrar livro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Atualiza as informações de um livro existente.
     *
     * @param livro Objeto {@link Livro} contendo as informações atualizadas.
     * @throws ServiceException Caso ocorra erro durante a validação ou
     * atualização.
     */
    public void atualizarLivro(Livro livro) throws ServiceException {
        try {
            validarLivro(livro);

            if (livro.getEtiqueta_livro() <= 0) {
                throw new ServiceException("Etiqueta de livro inválida para atualização.");
            }

            // Converte o nome do gênero para id_genero
            int genero_id = generoDAO.buscarIdGeneroPorNome(livro.getGeneroNome());
            livro.setGenero_id(genero_id);

            livroDAO.alterarLivro(livro);
            LOGGER.info("Livro atualizado com sucesso: " + livro.getTitulo());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar livro", ex);
            throw new ServiceException("Erro ao atualizar livro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lista todos os livros cadastrados no banco de dados.
     *
     * @return Lista de livros cadastrados.
     * @throws ServiceException Caso ocorra erro durante a execução.
     */
    public List<Livro> listarLivros() throws ServiceException {
        try {
            return livroDAO.consultaLivros();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar livros", ex);
            throw new ServiceException("Erro ao listar livros: " + ex.getMessage(), ex);
        }
    }

    /**
     * Exclui um livro pelo número de etiqueta.
     *
     * @param etiqueta Etiqueta do livro a ser excluído.
     * @throws ServiceException Caso ocorra erro durante a execução.
     */
    public void excluirLivro(int etiqueta) throws ServiceException {
        try {
            if (etiqueta <= 0) {
                throw new ServiceException("Etiqueta inválida.");
            }
            livroDAO.excluirLivro(etiqueta);
            LOGGER.info("Livro excluído com sucesso: " + etiqueta);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir livro", ex);
            throw new ServiceException("Erro ao excluir livro: " + ex.getMessage(), ex);
        }
    }

    /**
     * Busca um livro pelo ISBN.
     *
     * @param isbn ISBN do livro a ser buscado.
     * @return O livro correspondente ou null se não encontrado.
     * @throws ServiceException Caso ocorra erro durante a execução.
     */
    public Livro buscarPorISBN(String isbn) throws ServiceException {
        try {
            Livro livro = livroDAO.busca_porISBN(isbn);
            if (livro != null) {
                String generoNome = generoDAO.buscarNomeGeneroPorId(livro.getGenero_id());
                livro.setGeneroNome(generoNome);
            }
            return livro;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livro por ISBN", ex);
            throw new ServiceException("Erro ao buscar livro por ISBN: " + ex.getMessage(), ex);
        }
    }

    /**
     * Busca um livro por etiqueta.
     *
     * @param etiqueta A etiqueta do livro a ser buscado.
     * @return O livro encontrado ou null se não encontrado.
     * @throws SQLException Caso ocorra erro de banco de dados.
     */
    public Livro buscaPorEtiqueta(int etiqueta) throws SQLException {
        LivroDAO livroDAO = new LivroDAO(); // Cria uma instância de LivroDAO
        return livroDAO.buscarLivroPorEtiqueta(etiqueta); // Chama o método do DAO e retorna o livro
    }

    /**
     * Busca os livros filtrados por um gênero específico.
     *
     * <p>
     * Este método chama o método correspondente da camada de persistência (DAO)
     * para buscar os livros no banco de dados, filtrados pelo gênero.
     * </p>
     *
     * @param generoId ID do gênero utilizado como filtro para a busca.
     * @param etiqueta_livro etiqueta do livro utilizado como parâmetro, ele não
     * será retornado junto
     * @return Lista de livros encontrados que pertencem ao gênero especificado.
     */
    public List<Livro> buscarLivrosPorGeneros(int generoId, int etiqueta_livro) {
        try {
            // Chama o método do DAO para buscar os livros por gênero
            return livroDAO.buscarLivrosPorGeneros(generoId, etiqueta_livro);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livros por gênero no serviço: " + generoId, e);
            throw new RuntimeException("Erro ao buscar livros por gênero", e);
        }
    }

    /**
     * Busca os gêneros no banco de dados com base nos nomes fornecidos.
     *
     * Este método recebe um array de nomes de gêneros e retorna uma lista com
     * os objetos `Genero` correspondentes a esses nomes, incluindo seus
     * respectivos IDs. A consulta é feita diretamente no banco de dados.
     *
     * @param nomesGeneros Array de nomes de gêneros a serem buscados no banco
     * de dados.
     * @return Lista de objetos `Genero` contendo os nomes e IDs dos gêneros
     * encontrados.
     * @throws SQLException Se ocorrer erro ao acessar o banco de dados.
     */
    public List<Genero> buscarGenerosPorNome(String[] nomesGeneros) throws SQLException {
        // Consulta SQL para buscar os gêneros pelo nome
        String sql = "SELECT * FROM generos WHERE nome IN (" + String.join(", ", Collections.nCopies(nomesGeneros.length, "?")) + ")";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Setando os parâmetros da consulta para os nomes dos gêneros
            for (int i = 0; i < nomesGeneros.length; i++) {
                stmt.setString(i + 1, nomesGeneros[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Genero> generos = new ArrayList<>();
                while (rs.next()) {
                    Genero genero = new Genero();
                    genero.setId(rs.getInt("id"));
                    genero.setNome(rs.getString("nome"));
                    generos.add(genero);
                }
                return generos;
            }
        }
    }

    /**
     * Busca os livros no banco de dados com base nos IDs dos gêneros
     * fornecidos.
     *
     * Este método recebe um array de IDs de gêneros e retorna uma lista de
     * livros que pertencem a esses gêneros. A consulta é feita diretamente no
     * banco de dados.
     *
     * @param generos Array de IDs dos gêneros para filtrar os livros.
     * @return Lista de objetos `Livro` que correspondem aos gêneros filtrados.
     * @throws SQLException Se ocorrer erro ao acessar o banco de dados.
     */
    public List<Livro> buscarLivrosPorGenerosPorId(Integer[] generos) throws SQLException {
        // Consulta SQL para buscar livros pelos IDs dos gêneros
        String sql = "SELECT * FROM livros WHERE genero_id IN (" + String.join(", ", Collections.nCopies(generos.length, "?")) + ")";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Setando os parâmetros da consulta para os IDs dos gêneros
            for (int i = 0; i < generos.length; i++) {
                stmt.setInt(i + 1, generos[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Livro> livros = new ArrayList<>();
                while (rs.next()) {
                    Livro livro = new Livro();
                    livro.setTitulo(rs.getString("titulo"));
                    livro.setAutor(rs.getString("autor"));
                    livro.setGenero_id(Integer.parseInt(rs.getString("genero_id")));
                    livro.setGeneroNome(generoDAO.buscarNomeGeneroPorId(livro.getGenero_id()));
                    livro.setIsbn(rs.getString("isbn"));
                    livro.setEditora(rs.getString("editora"));
                    livro.setData_publicacao(rs.getDate("data_publicacao").toLocalDate());
                    livros.add(livro);
                }
                return livros;
            }
        }
    }

    /**
     * Busca livros com o mesmo gênero (ID do gênero) que o livro principal,
     * excluindo o próprio livro.
     *
     * @param generoId ID do gênero.
     * @param etiquetaLivro Etiqueta do livro que será excluída da busca.
     * @return Lista de livros com o mesmo gênero, excluindo o próprio livro.
     * @throws SQLException Se ocorrer erro na consulta ao banco de dados.
     */
    public List<Livro> buscarLivrosPorGenero(int generoId, int etiquetaLivro) throws SQLException {
        // Chama o método do DAO para buscar os livros com o mesmo gênero
        return livroDAO.buscarLivrosPorGenero(generoId, etiquetaLivro);
    }

    /**
     * Verifica se já existe uma relação de similaridade entre dois livros.
     *
     * Este método consulta a tabela 'livro_similar' no banco de dados para
     * verificar se já existe um vínculo entre o livro principal e o livro
     * similar. Antes de adicionar uma nova relação, é importante garantir que
     * não haja duplicidade.
     *
     * @param etiquetaLivro A etiqueta do livro principal.
     * @param etiquetaSimilar A etiqueta do livro similar.
     * @return true se a relação de livros similares já existir, caso contrário
     * false.
     * @throws SQLException Caso ocorra algum erro durante a execução da
     * consulta SQL.
     */
    public boolean verificarSimilarsExistem(int etiquetaLivro, int etiquetaSimilar) throws SQLException {
        return livroDAO.verificarSimilarsExistem(etiquetaLivro, etiquetaSimilar); // Chama o método do DAO para verificar no banco.
    }

    /**
     * Adiciona um livro similar ao livro principal, se a relação ainda não
     * existir.
     *
     * Este método verifica primeiro se a relação de similaridade já existe. Se
     * não existir, ela é adicionada na tabela 'livro_similar'. Caso contrário,
     * não faz nada.
     *
     * @param etiquetaLivro A etiqueta do livro principal.
     * @param etiquetaSimilar A etiqueta do livro similar.
     * @throws SQLException Caso ocorra algum erro durante a inserção no banco
     * de dados.
     */
    public void adicionarLivroSimilar(int etiquetaLivro, int etiquetaSimilar) throws SQLException {
        if (!verificarSimilarsExistem(etiquetaLivro, etiquetaSimilar)) {
            // Se a relação de similaridade não existir, insere no banco de dados.
            livroDAO.adicionarLivroSimilar(etiquetaLivro, etiquetaSimilar);
        } else {
            // Se a relação já existe, apenas loga ou retorna, sem fazer nada.
            System.out.println("A relação de livros similares já existe.");
        }
    }

    /**
     * Filtra os livros com base nos critérios fornecidos.
     *
     * Este método permite filtrar os livros por título, autor, ISBN, gênero, e
     * data de publicação. Ele cria uma consulta SQL dinâmica com base nos
     * parâmetros fornecidos e executa a busca no banco de dados.
     *
     * @param titulo Título do livro para filtro (opcional).
     * @param autor Autor do livro para filtro (opcional).
     * @param isbn ISBN do livro para filtro (opcional).
     * @param generoNome Nome do gênero para filtro (opcional).
     * @param dataPublicacao Data de publicação do livro para filtro (opcional).
     * @return Lista de livros que atendem aos critérios fornecidos.
     * @throws SQLException Caso ocorra erro ao executar a consulta no banco de
     * dados.
     */
    public List<Livro> filtrarLivros(String titulo, String autor, String isbn, String generoNome, LocalDate dataPublicacao) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM livros l ");
        sql.append("JOIN generos g ON l.genero_id = g.id WHERE 1=1");
        // Adiciona os filtros à consulta SQL
        if (titulo != null && !titulo.isEmpty()) {
            sql.append(" AND titulo LIKE ?");
        }
        if (autor != null && !autor.isEmpty()) {
            sql.append(" AND autor LIKE ?");
        }
        if (isbn != null && !isbn.isEmpty()) {
            sql.append(" AND isbn LIKE ?");
        }
        if (generoNome != null && !generoNome.isEmpty()) {
            sql.append(" AND g.nome LIKE ?");
        }
        if (dataPublicacao != null) {
            sql.append(" AND data_publicacao = ?");
        }

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            // Define os parâmetros da consulta
            int index = 1;
            if (titulo != null && !titulo.isEmpty()) {
                stmt.setString(index++, "%" + titulo + "%");
            }
            if (autor != null && !autor.isEmpty()) {
                stmt.setString(index++, "%" + autor + "%");
            }
            if (isbn != null && !isbn.isEmpty()) {
                stmt.setString(index++, "%" + isbn + "%");
            }

            // Apenas adiciona o parâmetro de gênero se o nome de gênero for fornecido
            if (generoNome != null && !generoNome.isEmpty()) {
                stmt.setString(index++, "%" + generoNome + "%");
            }

            if (dataPublicacao != null) {
                stmt.setDate(index++, java.sql.Date.valueOf(dataPublicacao));
            }

            // Executa a consulta e processa os resultados
            try (ResultSet rs = stmt.executeQuery()) {
                List<Livro> livros = new ArrayList<>();
                while (rs.next()) {
                    Livro livro = new Livro();
                    livro.setEtiqueta_livro(rs.getInt("etiqueta_livro"));
                    livro.setTitulo(rs.getString("titulo"));
                    livro.setAutor(rs.getString("autor"));
                    livro.setEditora(rs.getString("editora"));
                    livro.setGeneroNome(rs.getString("nome"));
                    livro.setIsbn(rs.getString("isbn"));
                    java.sql.Date dataPub = rs.getDate("data_publicacao");
                    if (dataPub != null) {
                        livro.setData_publicacao(dataPub.toLocalDate());
                    } else {
                        livro.setData_publicacao(null);
                    }
                    livros.add(livro);
                }
                return livros;
            }
        }
    }

    /**
     * Busca todos os livros relacionados a um determinado gênero, utilizando o
     * ID do gênero. Este método consulta a tabela de livros e retorna os livros
     * que correspondem ao ID de gênero fornecido.
     *
     * @param idGenero O ID do gênero para filtrar os livros.
     * @return Uma lista de objetos Livro que correspondem ao gênero
     * especificado.
     */
    public List<Livro> buscarLivrosPorGenero(int idGenero) {
        List<Livro> livros = new ArrayList<>(); // Lista para armazenar os livros encontrados
        // SQL para buscar livros com base no ID do gênero
        String query = "SELECT * FROM livros WHERE genero_id = ?";

        try (Connection connection = ConnectionFactory.getConnection(); // Estabelece a conexão com o banco de dados
                 PreparedStatement statement = connection.prepareStatement(query)) { // Prepara a consulta SQL

            statement.setInt(1, idGenero); // Define o valor do parâmetro ID do gênero na consulta

            try (ResultSet resultSet = statement.executeQuery()) { // Executa a consulta e obtém os resultados
                // Processa os resultados da consulta
                while (resultSet.next()) {
                    Livro livro = new Livro();

                    // Preenche os dados do livro a partir dos resultados da consulta
                    livro.setTitulo(resultSet.getString("titulo"));
                    livro.setAutor(resultSet.getString("autor"));
                    livro.setGenero_id(Integer.parseInt(resultSet.getString("genero_id")));  // O id do gênero é armazenado no livro
                    livro.setGeneroNome(generoDAO.buscarNomeGeneroPorId(livro.getGenero_id())); // seta o nome do gênero também.
                    livro.setIsbn(resultSet.getString("isbn"));
                    livro.setEditora(resultSet.getString("editora"));
                    livro.setData_publicacao(resultSet.getDate("data_publicacao").toLocalDate()); // Converte a data para LocalDate

                    livros.add(livro); // Adiciona o livro à lista de livros
                }
            }
        } catch (SQLException ex) {
            // Em caso de erro ao executar a consulta, imprime o erro
            ex.printStackTrace();
        }

        return livros; // Retorna a lista de livros encontrados
    }

    /**
     * Busca livros que correspondem ao filtro fornecido. O filtro pode ser
     * aplicado em campos como título, autor, gênero (nome) ou ISBN.
     *
     * @param filtro Texto a ser pesquisado nos campos relevantes.
     * @return Lista de livros que correspondem ao filtro.
     * @throws ServiceException Caso ocorra erro na execução da consulta.
     */
    public List<Livro> buscarLivrosPorFiltro(String filtro) throws ServiceException {
        try {
            LivroDAO livroDAO = new LivroDAO(); // Instancia o DAO
            return livroDAO.buscarLivrosPorFiltro(filtro); // Chama o método do DAO
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livros por filtro", ex);
            throw new ServiceException("Erro ao buscar livros por filtro: " + ex.getMessage(), ex);
        }
    }

}
