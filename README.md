**Sistema de Gerenciamento de Livraria**

Este é um sistema desenvolvido em Java para gerenciar o catálogo de uma livraria, com suporte para cadastro de livros via ISBN, gerenciamento de gêneros, pesquisa avançada, e geração de relatórios.
O projeto utiliza PostgreSQL para persistência de dados e integração com a API OpenLibrary para busca de informações externas.

**Atenção!**
Antes de iniciar o projeto é necessário criar o banco de dados, conforme orientação à seguir e então preencher as credenciais (URL, usuário e senha de acesso ao banco) no arquivo DBConfig.properties.

**Funcionalidades Principais**

**Gerenciamento de Livros**

- **Cadastro de Livros:**
Manualmente ou via API OpenLibrary (requisição baseada no ISBN).
Ao cadastrar pelo ISBN, o usuário insere o ISBN SEM traços e clica em "Cadastrar pelo ISBN".
Campos do livro:
Título, Autor, Editora, Gênero, ISBN e Data de Publicação.
Suporte à validação de ISBN (obrigatório e deve conter 13 caracteres).
Edição e Exclusão
Atualização de livros cadastrados.
Exclusão de livros individualmente.
- **Livros Similares**
  Identificação de livros similares com base no gênero.
  Gerenciamento de livros relacionados diretamente na interface gráfica.
- **Pesquisa e Filtros**
  Busca de livros com filtros dinâmicos:
  Título, Autor, Gênero, ISBN e Data de Publicação.
  Resultados exibidos em uma tabela.
- **Relatórios**
  Geração de relatórios de livros filtrados por gênero.
  Relatórios salvos no formato XLS (compatível com Excel).
  O caminho de salvamento é definido pelo usuário localmente.

**Requisitos do Sistema**

- Software Necessário
  Java SE Development Kit (JDK) 11 ou superior.
  Apache Maven para gerenciamento de dependências.
  PostgreSQL 14+ configurado no pgAdmin. 

- Dependências,o projeto utiliza as seguintes bibliotecas externas:
  Jackson: Para manipulação de JSON.
  Apache POI: Para criação de relatórios XLS.
  Toedter Calendar: Para seleção de datas.
  Hibernate: Para persistência de dados no banco de dados.
  Swing: Para a interface gráfica.
  Configuração do Banco de Dados

**Criação do Banco e Tabelas**
No pgAdmin, crie um banco de dados com o nome livraria.
Execute os scripts que se encontra na pasta "Livraria1\src\main\java\giovanna\projeto\livraria1\resources\script" para criar o banco de dados.
O primeiro script, chamado "ScripCriacaoBD" se encarrega da criação do banco de dados em si,
enquanto que o ScriptCriacaoTabelas se encarrega da criação das tabelas, triggers e funções.

**Configuração no Código**
No arquivo DBConfig.properties, que se encontra na pasta "Livraria1\src\main\java\giovanna\projeto\livraria1\resources\",
configure os dados de acesso ao banco:
Url para conexão com o banco
private static final String URL = "jdbc:postgresql://localhost:5432/livraria";
Usuário para conexão
private static final String USER = "seu_usuario";
Senha para conexão
private static final String PASSWORD = "sua_senha";

**Como Executar o Sistema**
Clone o repositório: 
git clone https://github.com/Giovannarcruz/SistemaDeGerenciamentoDeLivraria.git
cd caminho_da_pasta
Compile e execute o projeto com Maven:
mvn clean install
mvn exec:java -Dexec.mainClass="giovanna.projeto.livraria1.Livraria1Application"
A interface gráfica será aberta automaticamente.

**Como Utilizar o Sistema**
Navegação no Sistema
Menu Principal
Ao abrir o sistema, a janela principal exibe as seguintes opções:
Cadastro
        Livro
        Genero
Cátalogo
        Livro
Relatórios
          Livro por Gênero

Arquivo
        Sair
        
Cadastro de Livros
Clique no botão "Cadastro> Livros" no menu principal.
Opção 1: Cadastro Manual
Preencha os campos obrigatórios:
Título, Autor, Editora, Gênero, ISBN (13 caracteres) e Data de Publicação.
Clique em "Salvar".
Opção 2: Cadastro via ISBN
Clique no botão "Cadastrar pelo ISBN".
Insira o ISBN do livro SEM traços (ex.: 9783161484100).
O sistema buscará automaticamente os dados do livro pela API OpenLibrary.
Verifique e ajuste os campos, se necessário, e clique em "Salvar".
Gerenciamento de Gêneros
Acesse o menu de Gerenciamento de Gêneros.
Use os botões disponíveis para:
Incluir Gênero: Insira o nome e clique em "Salvar".
Editar Gênero: Selecione o gênero desejado e edite o nome.
Excluir Gênero: Selecione o gênero e clique em "Excluir".
Catálogo de Livros
Permite a pesquisa e visualização dos livros cadastrados.
Preencha um ou mais campos de filtro na área superior:
Título, Autor, Gênero, ISBN ou Data de Publicação.
Clique em "Aplicar Filtros"para que seja exibidos os livros correspondentes;
Para limpar os filtros e exibir todos os livros, clique em "Limpar Filtros".

Cadastro> Gênero
Acesso para cadastrar, editar ou excluir gêneros de livros.
Para cadastrar um livro, basta clicar em incluir e preencher seu nome. 
Para editar, selecione o registro desejado e clique em editar. 
Para excluir selecione o gênero desejado e clique em "Excluir".

Relatórios> Generos
Ferramenta para gerar relatórios em formato XLS.
Acesse o menu Relatórios>Generos.
Digite o Gênero desejado para filtrar os livros.
Clique em "Atualizar".
Serão exibidos os livros correspondentes
Para salvar, clique em "Salvar".
Escolha o local para salvar o arquivo XLS.
O relatório estará disponível para consulta no Excel.

**Estrutura do Projeto**
src/
├── main/
│   ├── java/giovanna/projeto/livraria1/
│   │   ├── Livraria1Application.java  # Classe principal.
│   │   ├── dao/                      # Classes para acesso ao banco de dados.
│   │   ├── model/                    # Classes modelo (Livro, Gênero, etc.).
│   │   ├── services/                 # Lógica de negócios (LivroService, etc.).
│   │   ├── util/                     # Utilitários (API, conexão com BD).
│   │   └── view/                     # Interface gráfica do usuário.
│   ├── resources/                    # Arquivos de configuração e suporte.
└── test/                             # Testes unitários.
