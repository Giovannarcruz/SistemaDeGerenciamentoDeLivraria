package giovanna.projeto.livraria1.view;

import giovanna.projeto.livraria1.model.Livro;
import giovanna.projeto.livraria1.services.LivroSimilaresService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Diálogo para exibição de detalhes de um livro em formato de formulário.
 * Inclui uma tabela para exibir os livros similares.
 */
public class DetalhesLivroDialog extends JDialog {

    private final DefaultTableModel modeloTabelaSimilares; // Modelo para a tabela de livros similares
    private final JTable tabelaSimilares; // Tabela para exibir livros similares
    private final LivroSimilaresService similaresService; // Serviço para buscar livros similares

    /**
     * Construtor da classe DetalhesLivroDialog.
     *
     * @param parent Janela pai que chamou o diálogo.
     * @param livro Livro cujos detalhes serão exibidos.
     * @throws SQLException Caso ocorra erro ao buscar livros similares.
     */
    public DetalhesLivroDialog(JFrame parent, Livro livro) throws SQLException, Exception {
        super(parent, "Detalhes do Livro", true);
        setSize(600, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Serviço de livros similares
        this.similaresService = new LivroSimilaresService();

        // Criação do painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel com os detalhes do livro
        JPanel detalhesPanel = criarPainelDetalhes(livro);
        mainPanel.add(detalhesPanel, BorderLayout.NORTH);

        // Configuração da tabela de livros similares
        modeloTabelaSimilares = new DefaultTableModel(
                new String[]{"Etiqueta", "Título", "Autor"}, 0
        );
        tabelaSimilares = new JTable(modeloTabelaSimilares);
        JScrollPane scrollPaneSimilares = new JScrollPane(tabelaSimilares);

        // Painel para exibir a tabela de livros similares
        JPanel similaresPanel = new JPanel(new BorderLayout());
        similaresPanel.setBorder(BorderFactory.createTitledBorder("Livros Similares"));
        similaresPanel.add(scrollPaneSimilares, BorderLayout.CENTER);

        mainPanel.add(similaresPanel, BorderLayout.CENTER);

        // Botão para fechar o diálogo
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(btnFechar);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Carrega os livros similares na tabela
        carregarLivrosSimilares(livro.getEtiqueta_livro());
    }

    /**
     * Cria o painel para exibir os detalhes do livro.
     *
     * @param livro Livro cujos detalhes serão exibidos.
     * @return JPanel configurado com os campos de detalhes.
     */
    private JPanel criarPainelDetalhes(Livro livro) {
        JPanel detalhesPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        detalhesPanel.setBorder(BorderFactory.createTitledBorder("Detalhes do Livro"));

        // Adiciona os campos de detalhes do livro
        detalhesPanel.add(new JLabel("Etiqueta:"));
        detalhesPanel.add(new JLabel(String.valueOf(livro.getEtiqueta_livro())));

        detalhesPanel.add(new JLabel("Título:"));
        detalhesPanel.add(new JLabel(livro.getTitulo()));

        detalhesPanel.add(new JLabel("Autor:"));
        detalhesPanel.add(new JLabel(livro.getAutor()));

        detalhesPanel.add(new JLabel("Editora:"));
        detalhesPanel.add(new JLabel(livro.getEditora()));

        detalhesPanel.add(new JLabel("Gênero:"));
        detalhesPanel.add(new JLabel(livro.getGeneroNome() != null ? livro.getGeneroNome() : "N/A")); // Exibe o gênero ou N/A

        detalhesPanel.add(new JLabel("ISBN:"));
        detalhesPanel.add(new JLabel(livro.getIsbn()));

        detalhesPanel.add(new JLabel("Data de Publicação:"));
        String dataPublicacao = livro.getData_publicacao() != null
                ? livro.getData_publicacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "N/A";
        detalhesPanel.add(new JLabel(dataPublicacao));

        return detalhesPanel;
    }

    /**
     * Carrega os livros similares na tabela.
     *
     * @param etiquetaLivro Etiqueta do livro base para buscar os similares.
     * @throws SQLException Caso ocorra erro ao buscar os dados.
     */
    private void carregarLivrosSimilares(int etiquetaLivro) throws SQLException, Exception {
        List<Livro> similares = similaresService.buscarLivrosSemelhantes(etiquetaLivro);

        // Limpa os dados da tabela antes de inserir os novos
        modeloTabelaSimilares.setRowCount(0);

        // Adiciona os livros similares na tabela
        for (Livro similar : similares) {
            modeloTabelaSimilares.addRow(new Object[]{
                similar.getEtiqueta_livro(),
                similar.getTitulo(),
                similar.getAutor()
            });
        }
    }
}
