package giovanna.projeto.livraria1.util;

import giovanna.projeto.livraria1.model.Livro;
import giovanna.projeto.livraria1.services.LivroService;
import giovanna.projeto.livraria1.services.LivroSimilaresService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Classe responsável por gerenciar o cálculo de similaridades entre livros no
 * banco de dados. A classe realiza a comparação de livros dentro do mesmo
 * gênero e adiciona as relações de similaridade.
 *
 * <p>
 * O cálculo de similaridade envolve a busca de livros do mesmo gênero e a
 * verificação se já existe uma relação de similaridade entre os livros. Se não
 * existir, a similaridade é adicionada para ambos os livros.
 * </p>
 * <p>
 * Esta classe utiliza o {@link LivroService} para buscar livros e o
 * {@link LivroSimilaresService} para gerenciar as relações de similaridade.
 * </p>
 *
 * @author Giovanna
 */
public class GerenciadorSimilares {

    private static final Logger LOGGER = Logger.getLogger(GerenciadorSimilares.class.getName());

    /**
     * Calcula as similaridades para todos os livros cadastrados no sistema.
     * Este método percorre todos os livros e, para cada um, busca outros livros
     * do mesmo gênero. Se não houver uma relação de similaridade entre os
     * livros, ela é criada.
     *
     * <p>
     * Este método agora usa o novo método `buscarLivrosPorGeneros` para buscar
     * livros do mesmo gênero e gerenciar a similaridade entre eles.
     * </p>
     *
     * @throws Exception Se ocorrer um erro durante o processo de cálculo das
     * similaridades.
     */
    public static void calcularSimilaridadesParaLivrosExistentes() throws Exception {
        // Instancia os serviços necessários
        LivroService livroService = new LivroService();
        LivroSimilaresService similaresService = new LivroSimilaresService();

        try (Connection connection = ConnectionFactory.getConnection()) { // Conexão para a transação principal
            connection.setAutoCommit(false); // Desabilita autocommit para controle transacional

            try {
                // Obtém a lista de todos os livros cadastrados
                List<Livro> todosOsLivros = livroService.listarLivros();

                // Verifica se há livros cadastrados para processar
                if (todosOsLivros != null) {
                    // Para cada livro, busca outros livros do mesmo gênero
                    for (Livro livro : todosOsLivros) {
                        // A busca agora utiliza o novo método que retorna livros do mesmo gênero, excluindo o próprio livro
                        List<Livro> livrosDoMesmoGenero = livroService.buscarLivrosPorGeneros(livro.getGenero_id());

                        if (livrosDoMesmoGenero != null) {
                            // Verifica e adiciona as relações de similaridade entre os livros
                            for (Livro livroSimilar : livrosDoMesmoGenero) {
                                try (Connection innerConnection = ConnectionFactory.getConnection()) { // Conexão interna no loop
                                    // Verifica se já existe a similaridade, caso contrário, cria a relação
                                    if (!similaresService.similaridadeExiste(livro.getEtiqueta_livro(), livroSimilar.getEtiqueta_livro())) {
                                        similaresService.adicionarLivroSimilar(livro.getEtiqueta_livro(), livroSimilar.getEtiqueta_livro());
                                        similaresService.adicionarLivroSimilar(livroSimilar.getEtiqueta_livro(), livro.getEtiqueta_livro());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Exibe uma mensagem caso não existam livros cadastrados
                    JOptionPane.showMessageDialog(null, "Não há livros cadastrados para calcular similaridades.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                }

                // Commit da transação após concluir o processamento
                connection.commit();
                System.out.println("Similaridades recalculadas com sucesso!");

            } catch (SQLException ex) {
                // Caso ocorra erro, faz rollback da transação
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Erro durante o recálculo de similaridades (rollback efetuado)", ex);
                JOptionPane.showMessageDialog(null, "Erro ao calcular similaridades: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                throw new Exception("Erro ao calcular similaridades (transação revertida): " + ex.getMessage());
            }
        } catch (SQLException ex) {
            // Tratamento de erro ao obter a conexão
            LOGGER.log(Level.SEVERE, "Erro ao obter conexão para a transação principal", ex);
            JOptionPane.showMessageDialog(null, "Erro ao obter a conexão com o banco de dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            throw new Exception("Erro ao obter a conexão com o banco de dados: " + ex.getMessage());
        } finally {
            try {
                // Bloco de finalização (se necessário, o código de limpeza pode ser adicionado aqui)
            } catch (Throwable ex) {
                LOGGER.log(Level.WARNING, "Erro ao finalizar o LivroService", ex);
            }
        }
    }
}
