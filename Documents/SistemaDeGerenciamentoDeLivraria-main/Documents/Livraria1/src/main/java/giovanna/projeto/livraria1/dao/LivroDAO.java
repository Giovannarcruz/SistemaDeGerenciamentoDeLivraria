package giovanna.projeto.livraria1.dao;

import giovanna.projeto.livraria1.model.Livro;
import giovanna.projeto.livraria1.util.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe responsável pelas manipulações no banco de dados, mais especificamente
 * na tabela livros.
 *
 * <p>
 * A classe agora suporta a relação entre as tabelas livros e gêneros,
 * utilizando o ID do gênero para armazenamento e o nome do gênero para
 * exibição.
 * </p>
 *
 * @author Giovanna
 */
public class LivroDAO {

    private static final Logger LOGGER = Logger.getLogger(LivroDAO.class.getName());

    // Consultas SQL
    private static final String INSERT_LIVRO_SQL
            = "INSERT INTO livros (titulo, autor, editora, genero_id, isbn, data_publicacao, data_inclusao) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_LIVRO_SQL
            = "UPDATE livros SET titulo = ?, autor = ?, editora = ?, genero_id = ?, isbn = ?, data_publicacao = ? "
            + "WHERE etiqueta_livro = ?";
    private static final String SELECT_LIVRO_SQL
            = "SELECT l.*, g.nome AS genero_nome FROM livros l "
            + "JOIN generos g ON l.genero_id = g.id";
    private static final String SELECT_LIVRO_ETIQUETA_SQL = SELECT_LIVRO_SQL + " WHERE etiqueta_livro = ?";
    private static final String SELECT_LIVRO_POR_ISBN_SQL = SELECT_LIVRO_SQL + " WHERE isbn = ?";
    private static final String DELETE_LIVRO_SQL = "DELETE FROM livros WHERE etiqueta_livro = ?";

    /**
     * Construtor vazio da classe
     */
    public LivroDAO() {
    }

    /**
     * Insere um novo livro no banco de dados.
     *
     * @param livro Objeto Livro contendo as informações do livro.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public void inserirLivro(Livro livro) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(INSERT_LIVRO_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatementParaInsercao(stmt, livro);
            stmt.executeUpdate();

            // Obtém a etiqueta gerada automaticamente
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    livro.setEtiqueta_livro(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter a etiqueta gerada.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inserir o livro: " + livro.getTitulo(), e);
            throw e;
        }
    }

    /**
     * Atualiza as informações de um livro existente no banco de dados.
     *
     * @param livro Objeto Livro contendo as novas informações.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public void alterarLivro(Livro livro) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(UPDATE_LIVRO_SQL)) {

            preencherStatementParaAtualizacao(stmt, livro);
            stmt.setInt(7, livro.getEtiqueta_livro());
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                LOGGER.log(Level.WARNING, "Nenhuma etiqueta encontrada para atualização: " + livro.getEtiqueta_livro());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar o livro: " + livro.getTitulo(), e);
            throw e;
        }
    }

    /**
     * Exclui um livro do banco de dados.
     *
     * @param etiquetaLivro Etiqueta do livro a ser excluído.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public void excluirLivro(int etiquetaLivro) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(DELETE_LIVRO_SQL)) {

            stmt.setInt(1, etiquetaLivro);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted == 0) {
                LOGGER.log(Level.WARNING, "Nenhum livro encontrado para exclusão com etiqueta: " + etiquetaLivro);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro na exclusão do livro com etiqueta: " + etiquetaLivro, e);
            throw e;
        }
    }

    /**
     * Lista todos os livros cadastrados no banco de dados, incluindo os nomes
     * dos gêneros.
     *
     * @return Lista de livros encontrados.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public List<Livro> consultaLivros() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SELECT_LIVRO_SQL); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                livros.add(criarLivroDoResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar os livros!", e);
            throw e;
        }
        return livros;
    }

    /**
     * Busca um livro por etiqueta (chave primária).
     *
     * @param etiqueta A etiqueta do livro a ser buscado.
     * @return O livro encontrado ou null se não encontrado.
     * @throws SQLException Caso ocorra erro de banco de dados.
     */
    public Livro buscarLivroPorEtiqueta(int etiqueta) throws SQLException {
        Livro livro = null;

        // Consulta ajustada para unir as tabelas `livros` e `generos`
        String sql = """
        SELECT 
            livros.etiqueta_livro, 
            livros.isbn, 
            livros.titulo, 
            livros.autor, 
            livros.editora, 
            livros.data_publicacao, 
            livros.genero_id,
            generos.nome AS genero_nome
        FROM 
            livros
        LEFT JOIN 
            generos ON livros.genero_id = generos.id
        WHERE 
            livros.etiqueta_livro = ?
    """;

        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, etiqueta); // Define o parâmetro da etiqueta

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    livro = new Livro();
                    livro.setEtiqueta_livro(resultSet.getInt("etiqueta_livro"));
                    livro.setIsbn(resultSet.getString("isbn"));
                    livro.setTitulo(resultSet.getString("titulo"));
                    livro.setAutor(resultSet.getString("autor"));
                    livro.setEditora(resultSet.getString("editora"));

                    // Gênero
                    livro.setGenero_id(resultSet.getInt("genero_id"));
                    livro.setGeneroNome(resultSet.getString("genero_nome"));

                    // Data de publicação
                    Date sqlDate = resultSet.getDate("data_publicacao");
                    livro.setData_publicacao((sqlDate != null) ? sqlDate.toLocalDate() : null);
                }
            }
        }

        return livro;
    }

    /**
     * Busca um livro pelo ISBN.
     *
     * @param isbn ISBN do livro a ser consultado.
     * @return Livro correspondente ou null se não encontrado.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public Livro busca_porISBN(String isbn) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SELECT_LIVRO_POR_ISBN_SQL)) {

            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? criarLivroDoResultSet(rs) : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livro por ISBN: " + isbn, e);
            throw e;
        }
    }

    /**
     * Preenche o {@link PreparedStatement} para a inserção de um novo livro.
     *
     * @param stmt Objeto PreparedStatement.
     * @param livro Objeto Livro contendo os dados.
     * @throws SQLException Caso ocorra um erro ao definir os parâmetros.
     */
    private void preencherStatementParaInsercao(PreparedStatement stmt, Livro livro) throws SQLException {
        stmt.setString(1, livro.getTitulo());
        stmt.setString(2, livro.getAutor());
        stmt.setString(3, livro.getEditora());
        stmt.setInt(4, livro.getGenero_id()); // Usa o ID do gênero
        stmt.setString(5, livro.getIsbn());
        if (livro.getData_publicacao() != null) {
            stmt.setDate(6, Date.valueOf(livro.getData_publicacao()));
        } else {
            stmt.setNull(6, Types.DATE);
        }
        stmt.setDate(7, Date.valueOf(LocalDate.now())); // Data de inclusão
    }

    /**
     * Preenche o {@link PreparedStatement} para a atualização de um livro
     * existente.
     *
     * @param stmt Objeto PreparedStatement.
     * @param livro Objeto Livro contendo os dados atualizados.
     * @throws SQLException Caso ocorra um erro ao definir os parâmetros.
     */
    private void preencherStatementParaAtualizacao(PreparedStatement stmt, Livro livro) throws SQLException {
        preencherStatementParaInsercao(stmt, livro); // Reutiliza a lógica de inserção
    }

    /**
     * Converte um {@link ResultSet} em um objeto {@link Livro}.
     *
     * @param rs Objeto ResultSet contendo os dados.
     * @return Objeto Livro preenchido.
     * @throws SQLException Caso ocorra um erro ao acessar os dados.
     */
    private Livro criarLivroDoResultSet(ResultSet rs) throws SQLException {
        Livro livro = new Livro();
        livro.setEtiqueta_livro(rs.getInt("etiqueta_livro"));
        livro.setTitulo(rs.getString("titulo"));
        livro.setAutor(rs.getString("autor"));
        livro.setEditora(rs.getString("editora"));
        livro.setGenero_id(rs.getInt("genero_id"));
        livro.setGeneroNome(rs.getString("genero_nome")); // Nome do gênero
        livro.setIsbn(rs.getString("isbn"));
        Date dataSQL = rs.getDate("data_publicacao");
        livro.setData_publicacao(dataSQL != null ? dataSQL.toLocalDate() : null);
        return livro;
    }

    /**
     * Busca os livros filtrados por um gênero específico.
     *
     * <p>
     * Este método realiza uma consulta no banco de dados para recuperar todos
     * os livros que pertencem a um determinado gênero. O gênero é identificado
     * pelo seu ID, que é passado como parâmetro para o método.
     * </p>
     *
     * @param genero_id ID do gênero utilizado como filtro para a busca.
     * @return Lista de livros encontrados que pertencem ao gênero especificado.
     * @throws SQLException Caso ocorra um erro ao realizar a operação no banco.
     */
    public List<Livro> buscarLivrosPorGeneros(int genero_id, int etiqueta_livro) throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String query = "SELECT l.*, g.nome AS genero_nome FROM livros l "
                + "JOIN generos g ON l.genero_id = g.id "
                + "WHERE l.genero_id = ? AND etiqueta_livro<>?"; // Filtro por gênero excluindo o livro filtrado

        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {

            // Define os parâmetros
            stmt.setInt(1, genero_id);
            stmt.setInt(2, etiqueta_livro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(criarLivroDoResultSet(rs)); // Cria e adiciona o livro à lista
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livros por gênero: " + genero_id, e);
            throw e;
        }
        return livros;
    }

    /**
     * Busca livros com o mesmo gênero (ID do gênero), excluindo o livro com a
     * etiqueta fornecida.
     *
     * @param generoId ID do gênero.
     * @param etiquetaLivro Etiqueta do livro que será excluída da busca.
     * @return Lista de livros com o mesmo gênero, excluindo o próprio livro.
     * @throws SQLException Se ocorrer erro na consulta ao banco de dados.
     */
    public List<Livro> buscarLivrosPorGenero(int generoId, int etiquetaLivro) throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE genero_id = ? AND etiqueta_livro != ?"; // Consulta usando o ID do gênero

        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, generoId);  // Filtra pelo ID do gênero
            stmt.setInt(2, etiquetaLivro);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Livro livro = new Livro();
                livro.setEtiqueta_livro(rs.getInt("etiqueta_livro"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setAutor(rs.getString("autor"));
                livro.setGenero_id(rs.getInt("genero_id")); // Pega o ID do gênero
                livro.setGeneroNome(rs.getString("genero_nome")); // Pega o nome do gênero
                livro.setIsbn(rs.getString("isbn"));
                livro.setEditora(rs.getString("editora"));
                livro.setData_publicacao(rs.getDate("data_publicacao").toLocalDate());
                livros.add(livro);
            }
        }

        return livros;
    }

    /**
     * Verifica se já existe uma relação de livros similares entre dois livros
     * no banco de dados.
     *
     * Este método consulta a tabela 'livro_similar' para verificar se já há um
     * registro associando o livro principal (identificado por 'etiquetaLivro')
     * e o livro similar (identificado por 'etiquetaSimilar').
     *
     * @param etiquetaLivro A etiqueta do livro principal (livro que será
     * verificado).
     * @param etiquetaSimilar A etiqueta do livro similar a ser verificado.
     * @return true se já existir uma relação de livros similares no banco de
     * dados, caso contrário false.
     * @throws SQLException Caso ocorra algum erro durante a execução da
     * consulta SQL.
     */
    public boolean verificarSimilarsExistem(int etiquetaLivro, int etiquetaSimilar) throws SQLException {
        // SQL para verificar se já existe uma relação entre os livros
        String sql = "SELECT 1 FROM livros_semelhantes WHERE etiqueta_livro = ? AND etiqueta_similar = ?";

        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Define os parâmetros na consulta SQL
            stmt.setInt(1, etiquetaLivro);   // Define a etiqueta do livro principal
            stmt.setInt(2, etiquetaSimilar); // Define a etiqueta do livro similar

            // Executa a consulta e verifica se algum registro foi encontrado
            try (ResultSet rs = stmt.executeQuery()) {
                // Se houver um registro, significa que a relação de livros similares já existe
                return rs.next();
            }
        }
    }

    /**
     * Adiciona uma relação de similaridade entre dois livros na tabela
     * 'livro_similar'.
     *
     * Este método realiza a inserção de uma nova relação de similaridade entre
     * dois livros na tabela 'livro_similar', caso a relação ainda não exista.
     * Caso a relação já tenha sido inserida, o método não faz nada.
     *
     * @param etiquetaLivro A etiqueta do livro principal.
     * @param etiquetaSimilar A etiqueta do livro similar.
     * @throws SQLException Caso ocorra algum erro durante a execução da
     * inserção SQL.
     */
    public void adicionarLivroSimilar(int etiquetaLivro, int etiquetaSimilar) throws SQLException {
        String sql = "INSERT INTO livros_semelhantes (etiqueta_livro, etiqueta_similar) VALUES (?, ?)";
        Connection connection = ConnectionFactory.getConnection();
        // Verifica se a relação já existe, se não, realiza a inserção
        if (!verificarSimilarsExistem(etiquetaLivro, etiquetaSimilar)) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, etiquetaLivro);
                stmt.setInt(2, etiquetaSimilar);
                stmt.executeUpdate(); // Executa a inserção no banco
            }
        }
    }

    public List<Livro> buscarLivrosPorFiltro(String filtro) throws SQLException {
        String sql = """
                 SELECT livros.etiqueta_livro, livros.titulo, livros.autor,
                        livros.isbn, livros.editora, livros.data_publicacao, generos.nome AS genero_nome
                 FROM livros
                 JOIN generos ON livros.genero_id = generos.id
                 WHERE (livros.titulo ILIKE ? 
                        OR livros.autor ILIKE ? 
                        OR livros.isbn ILIKE ? 
                        OR generos.nome ILIKE ?
                        OR CAST(livros.etiqueta_livro AS TEXT) ILIKE ? 
                        OR livros.data_publicacao::TEXT ILIKE ?)
                        OR livros.editora ILIKE ?
                 """;

        List<Livro> livros = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Define os valores para os parâmetros do filtro
            String filtroLike = "%" + filtro + "%";
            stmt.setString(1, filtroLike); // Filtro em título
            stmt.setString(2, filtroLike); // Filtro em autor
            stmt.setString(3, filtroLike); // Filtro em ISBN
            stmt.setString(4, filtroLike); // Filtro em nome do gênero
            stmt.setString(5, filtroLike); // Filtro em etiqueta (convertido para texto)
            stmt.setString(6, filtroLike); // Filtro em data_publicacao (convertido para texto)
            stmt.setString(7, filtroLike); // Filtro em editora

            // Executa a consulta e processa os resultados
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Livro livro = new Livro();
                    livro.setEtiqueta_livro(rs.getInt("etiqueta_livro"));
                    livro.setTitulo(rs.getString("titulo"));
                    livro.setAutor(rs.getString("autor"));
                    livro.setIsbn(rs.getString("isbn"));
                    livro.setEditora(rs.getString("editora"));
                    livro.setGeneroNome(rs.getString("genero_nome"));
                    Date sqlDate = rs.getDate("data_publicacao");
                    if (sqlDate != null) {
                        livro.setData_publicacao(sqlDate.toLocalDate());
                    }
                    livros.add(livro);
                }
            }
        }

        return livros;
    }

}
