# **Sistema de Gerenciamento de Livraria**

Bem-vindo ao **Sistema de Gerenciamento de Livraria**! Este sistema foi desenvolvido em **Java** para gerenciar o catálogo de uma livraria, com suporte para cadastro de livros, pesquisa avançada, geração de relatórios, e integração com a API OpenLibrary.

---

## **Principais Funcionalidades**

### **Gerenciamento de Livros**
- **Cadastro de Livros**:
  - Cadastro manual ou via ISBN (usando a API OpenLibrary).
  - Campos obrigatórios: Título, Autor, Editora, Gênero, ISBN (13 caracteres) e Data de Publicação.
- **Edição e Exclusão**:
  - Atualize ou exclua livros cadastrados diretamente na interface.
- **Livros Similares**:
  - Identificação e gerenciamento de livros relacionados pelo gênero.
- **Pesquisa Avançada**:
  - Filtros dinâmicos por Título, Autor, Gênero, ISBN e Data de Publicação.
- **Relatórios**:
  - Geração de relatórios filtrados por gênero em formato XLS.

---

## **Requisitos do Sistema**
- **Softwares Necessários**:
  - **Java SE Development Kit (JDK)**: Versão 11 ou superior.
  - **Apache Maven**: Para gerenciamento de dependências.
  - **PostgreSQL**: Versão 14+ configurado no pgAdmin.
- **Bibliotecas Externas**:
  - Jackson: Manipulação de JSON.
  - Apache POI: Criação de relatórios em Excel.
  - Toedter Calendar: Seleção de datas.
  - Hibernate: Persistência de dados.
  - Swing: Interface gráfica.

---

## **Configuração do Banco de Dados**

### **Criação do Banco**
1. Crie um banco de dados chamado `livraria` utilizando a interface do **pgAdmin** ou via terminal com o comando:
   ```sql
   CREATE DATABASE livraria;
   ```
2. Execute os scripts localizados em:
   ```
   src/main/java/giovanna/projeto/livraria1/resources/script
   ```
   - `ScriptCriacaoTabelas`: Cria tabelas, triggers e funções.

### **Configuração no Código**
No arquivo `DBConfig.properties`, localizado em:
```
src/main/java/giovanna/projeto/livraria1/resources
```
Preencha os dados de conexão:
```properties
jdbc.url=jdbc:postgresql://localhost:5432/livraria
jdbc.user=seu_usuario
jdbc.password=sua_senha
```

---

## **Como Executar**

### **Opção 1: Via Terminal**
1. Clone o repositório:
   ```bash
   git clone https://github.com/Giovannarcruz/SistemaDeGerenciamentoDeLivraria.git
   cd SistemaDeGerenciamentoDeLivraria
   ```
2. Compile e execute com Maven:
   ```bash
   mvn clean install
   mvn exec:java -Dexec.mainClass="giovanna.projeto.livraria1.Livraria1Application"
   ```

### **Opção 2: Via IDE**
1. Baixe o projeto e descompacte o arquivo.
2. Abra-o na IDE de sua preferência.
3. Execute a classe principal:
   ```
   Livraria1Application
   ```

---

## **Fluxo de Cadastro**

1. **Cadastro de Gêneros**:
   - Antes de cadastrar livros, é necessário cadastrar os gêneros disponíveis no sistema.
   - No menu principal, acesse **Cadastro > Gênero**.
   - Utilize as opções para:
     - Incluir: Insira o nome do gênero e clique em "Salvar".
     - Editar: Selecione o gênero desejado, edite o nome e clique em "Salvar".
     - Excluir: Selecione o gênero desejado e clique em "Excluir".

2. **Cadastro de Livros**:
   - Após cadastrar os gêneros, acesse **Cadastro > Livro**.
   - Preencha os campos obrigatórios, incluindo a associação de um gênero ao livro.
   - O cadastro pode ser realizado manualmente ou via ISBN (integrado com a API OpenLibrary).

---

## **Estrutura do Projeto**

```
src/
├── main/
│   ├── java/giovanna/projeto/livraria1/
│   │   ├── Livraria1Application.java  # Classe principal
│   │   ├── dao/                      # Acesso ao banco de dados
│   │   ├── model/                    # Classes modelo
│   │   ├── services/                 # Regras de negócio
│   │   ├── util/                     # Utilitários
│   │   └── view/                     # Interface gráfica
│   ├── resources/                    # Configurações e scripts
└── test/                             # Testes unitários
```

---
