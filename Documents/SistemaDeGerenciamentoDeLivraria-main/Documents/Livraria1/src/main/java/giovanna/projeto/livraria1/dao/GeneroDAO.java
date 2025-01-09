package giovanna.projeto.livraria1.dao;

import giovanna.projeto.livraria1.model.Genero;
import giovanna.projeto.livraria1.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável pelas manipulações de dados na tabela de gêneros no banco
 * de dados.
 *
 * Esta classe fornece métodos para listar, salvar, atualizar e excluir gêneros
 * na base de dados. Ela utiliza consultas SQL predefinidas e se conecta ao
 * banco de dados por meio da classe {@link ConnectionFactory}.
 *
 * @author Giovanna
 */
public class GeneroDAO {

    private final String SQL_LISTA_GENEROS = "SELECT * FROM generos ORDER BY nome";
    private final String SQL_SALVA_GENERO = "INSERT INTO generos (nome) VALUES (?)";
    private static final String SQL_ATUALIZA_GENERO = "UPDATE generos SET nome= ? WHERE id= ?";
    private static final String SQL_EXCLUI_GENERO = "DELETE FROM generos WHERE id=?";
    private static final String SELECT_GENERO_BY_ID = "SELECT nome FROM generos WHERE id = ?";
    private static final String SELECT_ID_BY_NOME = "SELECT id FROM generos WHERE nome = ?";

    /**
     * Lista todos os gêneros cadastrados na base de dados.
     *
     * Este método executa uma consulta SQL para listar todos os gêneros
     * presentes na tabela `generos`, ordenados pelo nome. Os resultados são
     * mapeados para objetos {@link Genero} e retornados em uma lista.
     *
     * @return Uma lista de objetos {@link Genero} contendo os gêneros
     * cadastrados na base de dados.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    // Tenta realizar a conexão com o banco, realizar o select (que consta na variável SQL_LISTA_GENEROS e então executar a cosnulta.
    public List<Genero> listaGeneros() throws SQLException {
        List<Genero> generos = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareCall(SQL_LISTA_GENEROS); ResultSet result = stmt.executeQuery()) {
            while (result.next()) {
                Genero genero = new Genero();
                genero.setId(result.getInt("id"));
                genero.setNome(result.getString("nome"));
                generos.add(genero);
            }

        }
        return generos;
    }

    /**
     * Salva um novo gênero na base de dados.
     *
     * Este método executa uma consulta SQL do tipo {@code INSERT} para salvar
     * um novo gênero no banco de dados. O gênero a ser salvo é passado como
     * parâmetro e seus dados são inseridos na tabela generos.
     *
     * @param genero O objeto {@link Genero} contendo os dados do gênero a ser
     * salvo.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    // Salva o novo gênero na base de dados. Primeiro tenta realizar a conexão e então executa o SQL informado.
    public void salvaGenero(Genero genero) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareCall(SQL_SALVA_GENERO)) {
            stmt.setString(1, genero.getNome());
            stmt.executeUpdate();

        }
    }

    /**
     * Atualiza um gênero pré-existente na base de dados.
     *
     * Este método executa uma consulta SQL do tipo {@code UPDATE} para
     * modificar um gênero existente na tabela `generos`. O gênero a ser
     * atualizado é identificado pelo seu {@code id}.
     *
     * @param genero O objeto {@link Genero} contendo os dados do gênero a ser
     * atualizado.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public void atualizaGenero(Genero genero) throws SQLException {
        //Tenta realizar a conexão e então executa a atualização por meio do SQL informado.
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SQL_ATUALIZA_GENERO)) {
            stmt.setString(1, genero.getNome());
            stmt.setInt(2, genero.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Exclui um gênero da base de dados.
     *
     * Este método executa uma consulta SQL do tipo {@code DELETE} para remover
     * um gênero da tabela `generos`, identificado pelo seu {@code id}.
     *
     * @param id O {@code id} do gênero a ser excluído.
     * @throws SQLException Se ocorrer um erro ao acessar o banco de dados.
     */
    public void excluiGenero(int id) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SQL_EXCLUI_GENERO)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public String buscarNomeGeneroPorId(int id) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SELECT_GENERO_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("nome") : null;
            }
        }
    }

    public int buscarIdGeneroPorNome(String nome) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection(); PreparedStatement stmt = connection.prepareStatement(SELECT_ID_BY_NOME)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Gênero não encontrado: " + nome);
                }
            }
        }
    }

    /**
     * Busca um gênero a partir de seu nome. Este método consulta a tabela de
     * gêneros e retorna o gênero correspondente ao nome fornecido.
     *
     * @param nome O nome do gênero para filtrar.
     * @return O objeto Genero correspondente ao nome informado, ou null se não
     * for encontrado.
     */
    public Genero buscarGeneroPorNome(String nome) {
        Genero genero = null; // Variável para armazenar o gênero encontrado
        // SQL para buscar o gênero pelo nome
        String query = "SELECT * FROM generos WHERE nome = ?";

        try (Connection connection = ConnectionFactory.getConnection(); // Estabelece a conexão com o banco de dados
                 PreparedStatement statement = connection.prepareStatement(query)) { // Prepara a consulta SQL

            statement.setString(1, nome); // Define o valor do parâmetro nome na consulta

            try (ResultSet resultSet = statement.executeQuery()) { // Executa a consulta e obtém os resultados
                // Se o gênero for encontrado, cria o objeto Genero e preenche os dados
                if (resultSet.next()) {
                    genero = new Genero();
                    genero.setId(resultSet.getInt("id")); // Preenche o ID do gênero
                    genero.setNome(resultSet.getString("nome")); // Preenche o nome do gênero
                }
            }
        } catch (SQLException ex) {
            // Em caso de erro ao executar a consulta, imprime o erro
            ex.printStackTrace();
        }

        return genero; // Retorna o gênero encontrado ou null se não houver resultado
    }
/**
     * Busca os gêneros a partir dos nomes fornecidos.
     * 
     * Este método consulta o banco de dados para encontrar os gêneros correspondentes aos nomes fornecidos.
     * 
     * @param nomesGêneros Array de nomes de gêneros para buscar no banco de dados.
     * @return Lista de objetos <code>Genero</code> encontrados no banco de dados.
     * @throws SQLException Caso ocorra um erro durante a consulta ao banco de dados.
     */
    public List<Genero> buscarGenerosPorNome(String[] nomesGêneros) throws SQLException {
        List<Genero> generos = new ArrayList<>();
        String query = "SELECT * FROM generos WHERE nome IN (";
        
        // Criar placeholders para os parâmetros IN
        for (int i = 0; i < nomesGêneros.length; i++) {
            query += "?";
            if (i < nomesGêneros.length - 1) {
                query += ", ";
            }
        }
        query += ")";
        
        try (Connection connection= ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            // Setando os valores para os parâmetros IN
            for (int i = 0; i < nomesGêneros.length; i++) {
                statement.setString(i + 1, nomesGêneros[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Genero genero = new Genero();
                    genero.setId(resultSet.getInt("id"));
                    genero.setNome(resultSet.getString("nome"));
                    generos.add(genero);
                }
            }
        }
        return generos;
    }
}
