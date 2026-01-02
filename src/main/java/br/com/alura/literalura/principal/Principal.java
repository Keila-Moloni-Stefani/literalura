package br.com.alura.literalura.principal;

import br.com.alura.literalura.model.*;
import br.com.alura.literalura.repository.AutorRepository;
import br.com.alura.literalura.repository.LivroRepository;
import br.com.alura.literalura.service.ConsumoAPI;
import br.com.alura.literalura.service.ConverteDados;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://gutendex.com/books/?search=";

    private LivroRepository livroRepository;
    private AutorRepository autorRepository;

    public Principal(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public void exibeMenu() {
        var opcao = -1;

        while (opcao != 0) {
            var menu = """
                *************************************************
                Escolha o número de sua opção:
                1 - Buscar livro pelo título
                2 - Buscar autor por nome
                3 - Listar livros registrados
                4 - Listar autores registrados
                5 - Listar autores vivos em determinado ano
                6 - Listar livros em determinado idioma
                7 - Estatísticas por idioma
                0 - Sair
                *************************************************
                """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarLivroWeb();
                    break;
                case 2:
                    buscarAutorPorNome();
                    break;
                case 3:
                    listarLivrosRegistrados();
                    break;
                case 4:
                    listarAutoresRegistrados();
                    break;
                case 5:
                    listarAutoresVivosEmAno();
                    break;
                case 6:
                    listarLivrosPorIdioma();
                    break;
                case 7:
                    estatisticasPorIdioma();
                    break;
                case 0:
                    System.out.println("Saindo... Até logo!");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarLivroWeb() {
        System.out.println("Digite o nome do livro que deseja buscar:");
        var nomeLivro = leitura.nextLine();

        var json = consumoAPI.obterDados(ENDERECO + nomeLivro.replace(" ", "%20"));
        DadosResposta dados = conversor.obterDados(json, DadosResposta.class);

        if (dados.resultados().isEmpty()) {
            System.out.println("Livro não encontrado!");
            return;
        }

        DadosLivro dadosLivro = dados.resultados().get(0);

        // Verificar se o livro já existe
        Optional<Livro> livroExistente = livroRepository.findByTituloContainingIgnoreCase(dadosLivro.titulo());
        if (livroExistente.isPresent()) {
            System.out.println("Livro já está registrado no banco de dados!");
            System.out.println(livroExistente.get());
            return;
        }

        // Criar livro
        Livro livro = new Livro(dadosLivro);

        // Processar autor
        if (!dadosLivro.autores().isEmpty()) {
            DadosAutor dadosAutor = dadosLivro.autores().get(0);

            // Verificar se autor já existe
            Optional<Autor> autorExistente = autorRepository.findByNomeContainingIgnoreCase(dadosAutor.nome());

            Autor autor;
            if (autorExistente.isPresent()) {
                autor = autorExistente.get();
            } else {
                autor = new Autor(dadosAutor);
                autor = autorRepository.save(autor);
            }

            livro.setAutor(autor);
        }

        livroRepository.save(livro);
        System.out.println("\nLivro salvo com sucesso!\n");
        System.out.println(livro);
    }

    private void buscarAutorPorNome() {
        System.out.println("Digite o nome do autor que deseja buscar:");
        var nomeAutor = leitura.nextLine();

        Optional<Autor> autor = autorRepository.findByNomeContainingIgnoreCase(nomeAutor);

        if (autor.isEmpty()) {
            System.out.println("Autor não encontrado!");
            return;
        }

        System.out.println("\n===== AUTOR ENCONTRADO =====");
        System.out.println("Autor: " + autor.get().getNome());
        System.out.println("Ano de nascimento: " + autor.get().getAnoNascimento());
        System.out.println("Ano de falecimento: " + autor.get().getAnoFalecimento());
        System.out.println("Livros:");
        autor.get().getLivros().forEach(l ->
                System.out.println("  - " + l.getTitulo())
        );
        System.out.println("============================\n");
    }

    private void listarLivrosRegistrados() {
        List<Livro> livros = livroRepository.findAll();

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro registrado!");
            return;
        }

        System.out.println("\n----- LIVROS REGISTRADOS -----");
        livros.forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor registrado!");
            return;
        }

        System.out.println("\n----- AUTORES REGISTRADOS -----");
        autores.forEach(a -> {
            System.out.println("\nAutor: " + a.getNome());
            System.out.println("Ano de nascimento: " + a.getAnoNascimento());
            System.out.println("Ano de falecimento: " + a.getAnoFalecimento());
            System.out.println("Livros: " + a.getLivros().stream()
                    .map(Livro::getTitulo)
                    .toList());
            System.out.println("-------------------------------");
        });
    }

    private void listarAutoresVivosEmAno() {
        System.out.println("Digite o ano que deseja pesquisar:");
        var ano = leitura.nextInt();
        leitura.nextLine();

        List<Autor> autores = autorRepository.findAutoresVivosNoAno(ano);

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor encontrado vivo nesse ano!");
            return;
        }

        System.out.println("\n----- AUTORES VIVOS EM " + ano + " -----");
        autores.forEach(a -> {
            System.out.println("\nAutor: " + a.getNome());
            System.out.println("Ano de nascimento: " + a.getAnoNascimento());
            System.out.println("Ano de falecimento: " + a.getAnoFalecimento());
            System.out.println("-------------------------------");
        });
    }

    private void listarLivrosPorIdioma() {
        var menuIdioma = """
                Digite o idioma para realizar a busca:
                es - Espanhol
                en - Inglês
                fr - Francês
                pt - Português
                """;

        System.out.println(menuIdioma);
        var idioma = leitura.nextLine();

        List<Livro> livros = livroRepository.findByIdiomaContainingIgnoreCase(idioma);

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado nesse idioma!");
            return;
        }

        System.out.println("\n----- LIVROS EM " + idioma.toUpperCase() + " -----");
        livros.forEach(System.out::println);
    }


    private void estatisticasPorIdioma() {
        System.out.println("\n===== ESTATÍSTICAS POR IDIOMA =====");

        // Contar livros em Português
        Long quantidadePt = livroRepository.contarLivrosPorIdioma("pt");
        System.out.println("Português: " + quantidadePt + " livro(s)");

        // Contar livros em Inglês
        Long quantidadeEn = livroRepository.contarLivrosPorIdioma("en");
        System.out.println("Inglês: " + quantidadeEn + " livro(s)");

        // Contar livros em Espanhol
        Long quantidadeEs = livroRepository.contarLivrosPorIdioma("es");
        System.out.println("Espanhol: " + quantidadeEs + " livro(s)");

        // Contar livros em Francês
        Long quantidadeFr = livroRepository.contarLivrosPorIdioma("fr");
        System.out.println("Francês: " + quantidadeFr + " livro(s)");

        System.out.println("====================================\n");
    }
}
