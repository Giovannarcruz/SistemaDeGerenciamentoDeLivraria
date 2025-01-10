package giovanna.projeto.livraria1.view;

import com.toedter.calendar.JDateChooser;
import giovanna.projeto.livraria1.dao.GeneroDAO;
import giovanna.projeto.livraria1.dao.LivroDAO;
import giovanna.projeto.livraria1.model.Genero;
import giovanna.projeto.livraria1.model.Livro;
import giovanna.projeto.livraria1.services.GeneroService;
import giovanna.projeto.livraria1.services.LivroService;
import giovanna.projeto.livraria1.services.LivroSimilaresService;
import giovanna.projeto.livraria1.util.AutoComplete;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.rpc.ServiceException;

/**
 * Classe responsável pela criação e edição de livros, permitindo integração com
 * livros similares e suporte para autocomplete de gêneros.
 */
public class LivroDialog extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(LivroDialog.class.getName());

    // Campos do formulário
    private final JTextField txtTitulo = new JTextField(30);
    private final JTextField txtAutores = new JTextField(30);
    private final JTextField txtISBN = new JTextField(14);
    private final JTextField txtEditora = new JTextField(30);
    private final JDateChooser dataPublicacao = new JDateChooser();
    private final JTextField txtGenero = new JTextField(30);
    private final JPopupMenu popupGenero = new JPopupMenu();
    private List<Genero> generos;

    // Tabela de livros similares
    private final DefaultTableModel modeloTabelaSimilares = new DefaultTableModel(
            new String[]{"Etiqueta", "Título", "Autor"}, 0
    );
    private final JTable tabelaLivrosSimilares = new JTable(modeloTabelaSimilares);

    // Botões de ação
    private final JButton btnSalvar = new JButton("Salvar");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnIncluirSimilares = new JButton("Incluir");
    private final JButton btnExcluirSimilares = new JButton("Excluir");

    // Serviços
    private final LivroSimilaresService similaresService;
    private final LivroService livroService;
    private final LivroDAO livroDAO;
    private final GeneroDAO generoDAO;

    // Objeto Livro em edição
    private Livro livro;

    /**
     * Construtor da classe LivroDialog.
     *
     * @param parent Janela pai que chamou o diálogo.
     * @param livro Livro a ser editado ou null para novo cadastro.
     * @throws SQLException Caso ocorra erro na comunicação com o banco de
     * dados.
     */
    public LivroDialog(JFrame parent, Livro livro) throws SQLException {
        super(parent, true);
        this.livro = livro;
        this.similaresService = new LivroSimilaresService();
        this.livroService = new LivroService();
        this.livroDAO = new LivroDAO();
        this.generoDAO = new GeneroDAO();

        setTitle(livro == null ? "Incluir Livro" : "Editar Livro");
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        configureListeners();

        if (livro != null) {
            preencherCampos();
            carregarLivrosSimilares();
        }

        carregarGeneros();
        setVisible(true);
    }

    /**
     * Inicializa os componentes da interface gráfica.
     */
    private void initComponents() {
        // Painel do formulário
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.add(new JLabel("Título*:"));
        formPanel.add(txtTitulo);
        formPanel.add(new JLabel("Autores:"));
        formPanel.add(txtAutores);
        formPanel.add(new JLabel("ISBN*:"));
        formPanel.add(txtISBN);
        formPanel.add(new JLabel("Editora*:"));
        formPanel.add(txtEditora);
        formPanel.add(new JLabel("Data de Publicação*:"));
        formPanel.add(dataPublicacao);
        formPanel.add(new JLabel("Gênero*:"));
        formPanel.add(txtGenero);
        add(formPanel, BorderLayout.NORTH);

        // Painel de livros similares
        JPanel panelSimilares = new JPanel(new BorderLayout());
        panelSimilares.setBorder(BorderFactory.createTitledBorder("Livros Similares"));
        JScrollPane scrPaneSimilares = new JScrollPane(tabelaLivrosSimilares);
        panelSimilares.add(scrPaneSimilares, BorderLayout.CENTER);

        JPanel btnPanelSimilares = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanelSimilares.add(btnIncluirSimilares);
        btnPanelSimilares.add(btnExcluirSimilares);
        panelSimilares.add(btnPanelSimilares, BorderLayout.SOUTH);

        add(panelSimilares, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners para os botões de ação.
     */
    private void configureListeners() {
        btnSalvar.addActionListener(e -> {
            try {
                salvarLivro();
            } catch (ServiceException | SQLException ex) {
                Logger.getLogger(LivroDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(LivroDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnCancelar.addActionListener(e -> dispose());
        btnIncluirSimilares.addActionListener(e -> incluirLivrosSimilares());
        btnExcluirSimilares.addActionListener(e -> excluirLivrosSimilares());
    }

    /**
     * Preenche os campos do formulário com os dados do livro em edição.
     */
    private void preencherCampos() throws SQLException {
        if (livro == null) {
            return;
        }
        txtTitulo.setText(livro.getTitulo());
        txtAutores.setText(livro.getAutor());
        txtISBN.setText(livro.getIsbn());
        txtEditora.setText(livro.getEditora());
        txtGenero.setText(generoDAO.buscarNomeGeneroPorId(livro.getGenero_id()));
        LocalDate localData = livro.getData_publicacao();
        if (localData != null) {
            Date date = Date.from(localData.atStartOfDay(ZoneId.systemDefault()).toInstant());
            dataPublicacao.setDate(date);
        } else {
            dataPublicacao.setDate(null);
        }
    }

    /**
     * Carrega a lista de gêneros para o campo de autocomplete.
     */
    private void carregarGeneros() {
        try {
            GeneroService generoService = new GeneroService();
            this.generos = generoService.listaGeneros();
            if (this.generos != null) {
                AutoComplete.configureAutoComplete(txtGenero, popupGenero, this.generos, Genero::getNome);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar gêneros: " + ex.getMessage());
        }
    }

    /**
     * Salva ou atualiza o livro no banco de dados.
     */
    private void salvarLivro() throws ServiceException, SQLException, Exception {
        try {
            if (livro == null || livro.getEtiqueta_livro() == 0) {
                // Caso de inclusão (livro novo)
                Livro livroParaSalvar = prepararLivroParaSalvar();

                // Verifica se o ISBN já está cadastrado
                Livro livroExistente = livroDAO.busca_porISBN(livroParaSalvar.getIsbn());
                if (livroExistente != null) {
                    JOptionPane.showMessageDialog(this, "Livro com este ISBN já está cadastrado!");
                    return;
                }

                // Realiza o cadastro
                livro = livroService.cadastrarLivro(livroParaSalvar);
                JOptionPane.showMessageDialog(this, "Livro cadastrado com sucesso!");
                dispose();
            } else {
                // Caso de edição
                Livro livroParaAtualizar = prepararLivroParaSalvar();
                livroParaAtualizar.setEtiqueta_livro(livro.getEtiqueta_livro()); // Mantém a etiqueta do livro existente

                livroService.atualizarLivro(livroParaAtualizar);
                JOptionPane.showMessageDialog(this, "Livro atualizado com sucesso!");
                dispose();
            }

            // Após salvar ou atualizar, adiciona similares automaticamente
            adicionarSimilaresAutomaticamente(livro);

            carregarLivrosSimilares(); // Atualiza a tabela de similares
            dispose(); // Fecha o diálogo
        } catch (HeadlessException | ServiceException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar ou atualizar livro: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Erro ao salvar ou atualizar livro", ex);
        }
    }

    /**
     * Prepara o objeto Livro com os dados do formulário.
     *
     * @return Objeto Livro preenchido.
     */
    private Livro prepararLivroParaSalvar() {
        Livro livroParaSalvar = (this.livro == null) ? new Livro() : this.livro;

        livroParaSalvar.setTitulo(txtTitulo.getText());
        livroParaSalvar.setAutor(txtAutores.getText());
        livroParaSalvar.setIsbn(txtISBN.getText());
        livroParaSalvar.setEditora(txtEditora.getText());
        livroParaSalvar.setGeneroNome(txtGenero.getText());

        // Converte a data selecionada para LocalDate
        Date dataSelecionada = dataPublicacao.getDate();
        LocalDate localData = dataSelecionada != null
                ? dataSelecionada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        livroParaSalvar.setData_publicacao(localData);

        return livroParaSalvar;
    }

    /**
     * Inclui livros similares ao livro atual. Exibe um diálogo para selecionar
     * livros similares, e os adiciona à lista de livros similares do livro
     * atual.
     */
    private void incluirLivrosSimilares() {
        if (livro == null) {
            JOptionPane.showMessageDialog(this, "Salve o livro antes de incluir livros similares.");
            return;
        }

        // Abre o diálogo de seleção de livros similares
        SelecionarLivroDialog selecionarLivroDialog = new SelecionarLivroDialog(this, livro.getEtiqueta_livro());
        selecionarLivroDialog.setVisible(true);

        // Obtém a lista de livros selecionados
        List<Integer> selecionados = selecionarLivroDialog.selecionarLivros();

        // Adiciona os livros similares selecionados
        try {
            for (int etiquetaSimilar : selecionados) {
                similaresService.adicionarLivroSimilar(livro.getEtiqueta_livro(), etiquetaSimilar);
                LOGGER.info("Similar adicionado " + etiquetaSimilar);
            }

            // Atualiza a tabela de similares no diálogo principal
            carregarLivrosSimilares();
            JOptionPane.showMessageDialog(this, "Livros similares adicionados com sucesso!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao incluir livros similares: " + ex.getMessage());
        }
    }

    /**
     * Exclui um livro similar da lista de livros similares do livro atual.
     */
    private void excluirLivrosSimilares() {
        int linhaSelecionada = tabelaLivrosSimilares.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um livro para excluir.");
            return;
        }

        int etiquetaSimilar = (int) modeloTabelaSimilares.getValueAt(linhaSelecionada, 0);
        try {
            similaresService.excluirLivroSimilar(livro.getEtiqueta_livro(), etiquetaSimilar);
            carregarLivrosSimilares();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir livro similar: " + ex.getMessage());
        }
    }

    /**
     * Adiciona livros similares automaticamente ao livro recém-cadastrado.
     *
     * @param livro Livro recém-cadastrado.
     * @throws SQLException Caso ocorra erro na comunicação com o banco de
     * dados.
     */
    private void adicionarSimilaresAutomaticamente(Livro livro) throws SQLException, Exception {
        try {
            // Verifica se o livro tem um gênero
            if (livro.getGeneroNome() != null && !livro.getGeneroNome().isEmpty()) {
                // Procura livros com o mesmo gênero
                List<Livro> livrosSimilares = livroDAO.buscarLivrosPorGeneros(livro.getGenero_id(), livro.getEtiqueta_livro());

                // Filtra os livros que já estão como similares para evitar duplicidade
                for (Livro similar : livrosSimilares) {
                    if (!livroSimilaresExistem(livro.getEtiqueta_livro(), similar.getEtiqueta_livro())) {
                        // Adiciona o livro similar
                        similaresService.adicionarLivroSimilar(livro.getEtiqueta_livro(), similar.getEtiqueta_livro());
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar livros similares automaticamente", e);
            throw new SQLException("Erro ao adicionar livros similares automaticamente.", e);
        }
    }

    /**
     * Verifica se o livro similar já foi adicionado à lista de similares.
     *
     * @param etiquetaLivro Etiqueta do livro principal.
     * @param etiquetaSimilar Etiqueta do livro similar.
     * @return true se o livro já foi adicionado como similar, caso contrário
     * false.
     * @throws SQLException Caso ocorra erro na comunicação com o banco de
     * dados.
     */
    private boolean livroSimilaresExistem(int etiquetaLivro, int etiquetaSimilar) throws SQLException, Exception {
        // Verifica se já existe um vínculo entre os livros no banco de dados
        return similaresService.similaridadeExiste(etiquetaLivro, etiquetaSimilar);
    }

    /**
     * Método para carregar os livros que são similares em comparação com o
     * livro selecionado.
     */
    private void carregarLivrosSimilares() {
        if (livro == null) {
            return;
        }

        try {
            List<Livro> similares = similaresService.buscarLivrosSemelhantes(livro.getEtiqueta_livro());

            modeloTabelaSimilares.setRowCount(0); // Limpa a tabela
            for (Livro similar : similares) {
                if (similar.getEtiqueta_livro() != livro.getEtiqueta_livro()) { // Evita auto-referência
                    modeloTabelaSimilares.addRow(new Object[]{similar.getEtiqueta_livro(), similar.getTitulo(), similar.getAutor()});

                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar livros similares: " + ex.getMessage());
        }
    }
}
