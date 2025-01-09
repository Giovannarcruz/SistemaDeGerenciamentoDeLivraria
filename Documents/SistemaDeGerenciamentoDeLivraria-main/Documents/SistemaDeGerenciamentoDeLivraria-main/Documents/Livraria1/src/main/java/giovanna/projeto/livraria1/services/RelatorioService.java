package giovanna.projeto.livraria1.services;

import giovanna.projeto.livraria1.model.Genero;
import giovanna.projeto.livraria1.model.Livro;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;

/**
 * Classe responsável pela geração de relatórios de livros por gênero. Suporta
 * exportação única em formato Excel (XLSX).
 *
 * <p>
 * A classe utiliza o Apache POI para gerar relatórios no formato Excel. O
 * relatório gerado contém livros filtrados por gênero e é salvo com a extensão
 * ".xls", não permitindo outros formatos de exportação.
 * </p>
 *
 * @author Giovanna
 */
public class RelatorioService {

    private static final Logger LOGGER = Logger.getLogger(RelatorioService.class.getName());

    // Caminho local onde o arquivo .jasper será salvo.
    private static final String LOCAL_FILE_PATH = System.getProperty("java.io.tmpdir") + "LivroporGenero.jasper"; // Caminho temporário

    // ID do arquivo no Google Drive
    private static final String FILE_ID = "149kOD-aDBS5B2-9SGy4QpGiJ9Eufj2MY";

    /**
     * Gera um relatório de livros filtrados por gênero e exporta em formato
     * Excel (XLS). Este método recebe os nomes dos gêneros selecionados pelo usuário,
     * busca os respectivos IDs no banco de dados, e utiliza esses IDs para consultar
     * os livros do banco.
     *
     * O relatório gerado é então exportado em formato Excel (.xls).
     *
     * @param generos Array de nomes de gêneros para filtrar os livros.
     * @param caminhoSalvar Caminho para salvar o arquivo gerado. A extensão
     * ".xls" será adicionada automaticamente, caso necessário.
     * @throws SQLException Se ocorrer erro ao buscar livros no banco de dados.
     * @throws IOException Se ocorrer erro ao salvar o relatório no disco.
     * @throws net.sf.jasperreports.engine.JRException Se ocorrer erro ao salvar o relatório no disco.
     */
    public void gerarRelatorioPorGenero(String[] generos, String caminhoSalvar)
            throws SQLException, IOException, JRException {

        // Baixar o arquivo .jasper, se necessário
        baixarArquivoGoogleDrive();

        // Serviço para buscar os IDs dos gêneros a partir dos nomes fornecidos
        LivroService livroService = new LivroService();
        List<Genero> generosIds = livroService.buscarGenerosPorNome(generos);

        // Extrair apenas os IDs dos gêneros encontrados
        Integer[] generosIdsArray = new Integer[generosIds.size()];
        for (int i = 0; i < generosIds.size(); i++) {
            generosIdsArray[i] = generosIds.get(i).getId();
        }

        // Garantir que a extensão do arquivo seja ".xls"
        if (!caminhoSalvar.toLowerCase().endsWith(".xls")) {
            caminhoSalvar += ".xls"; // Adiciona a extensão ".xls" se não estiver presente
        }

        // Buscar os livros filtrados pelos IDs dos gêneros
        List<Livro> livros = livroService.buscarLivrosPorGenerosPorId(generosIdsArray);

        // Exportar o relatório no formato Excel
        exportarRelatorioParaExcel(livros, caminhoSalvar);
    }

    /**
     * Exporta um relatório de livros para o formato Excel (XLS).
     *
     * Este método utiliza a biblioteca Apache POI para criar uma planilha Excel
     * contendo os dados dos livros, como título, autor, gênero, ISBN, editora e
     * data de publicação. O relatório é salvo no caminho especificado.
     *
     * @param livros Lista de livros para gerar o relatório.
     * @param caminhoSalvar Caminho para salvar o arquivo Excel gerado.
     * @throws JRException Se ocorrer erro durante a exportação.
     */
    private void exportarRelatorioParaExcel(List<Livro> livros, String caminhoSalvar) throws JRException {
        // Usando Apache POI diretamente para exportação para Excel
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório de Livros");

            // Cria o cabeçalho da tabela
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Título", "Autor", "Gênero", "ISBN", "Editora", "Data Publicação"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // Estilo opcional para cabeçalho
                CellStyle headerStyle = workbook.createCellStyle();
                XSSFFont font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Adiciona os dados dos livros
            int rowNum = 1;
            for (Livro livro : livros) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(livro.getTitulo());
                row.createCell(1).setCellValue(livro.getAutor());
                row.createCell(2).setCellValue(livro.getGeneroNome());
                row.createCell(3).setCellValue(livro.getIsbn());
                row.createCell(4).setCellValue(livro.getEditora());
                if (livro.getData_publicacao() != null) {
                    row.createCell(5).setCellValue(livro.getData_publicacao().toString());
                }
            }

            // Salva o arquivo Excel no disco
            try (FileOutputStream fileOut = new FileOutputStream(caminhoSalvar)) {
                workbook.write(fileOut);
            }
            LOGGER.info("Relatório salvo em Excel no caminho: " + caminhoSalvar);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao gerar relatório Excel: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Erro ao gerar relatório Excel: " + e.getMessage());
        }
    }

    /**
     * Baixa o arquivo .jasper do Google Drive, caso ainda não tenha sido
     * baixado localmente. Este método verifica se o arquivo já existe no
     * caminho especificado. Se não existir, ele fará o download do arquivo
     * usando a URL pública de download do Google Drive.
     *
     * <p>
     * O arquivo é baixado e salvo no caminho local especificado pela variável
     * {@link #LOCAL_FILE_PATH}. Caso o arquivo já exista no sistema local, ele
     * não será baixado novamente.
     * </p>
     *
     * @throws IOException Se ocorrer algum erro durante o processo de download
     * ou gravação do arquivo.
     */
    private void baixarArquivoGoogleDrive() throws IOException {
        // Verificar se o arquivo já foi baixado
        File arquivoLocal = new File(LOCAL_FILE_PATH);
        if (!arquivoLocal.exists()) {
            // URL de download direto do Google Drive
            String fileURL = "https://drive.google.com/uc?id=" + FILE_ID + "&export=download";
            HttpURLConnection connection = (HttpURLConnection) new URL(fileURL).openConnection();
            connection.setRequestMethod("GET");

            // Baixar o arquivo e salvar no caminho local
            try (InputStream inputStream = connection.getInputStream(); OutputStream outputStream = new FileOutputStream(LOCAL_FILE_PATH)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.info("Arquivo .jasper baixado para: " + LOCAL_FILE_PATH);
        } else {
            LOGGER.info("Arquivo .jasper já existe em: " + LOCAL_FILE_PATH);
        }
    }
}
