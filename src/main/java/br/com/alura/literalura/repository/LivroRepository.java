package br.com.alura.literalura.repository;

import br.com.alura.literalura.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByTituloContainingIgnoreCase(String titulo);

    List<Livro> findByIdiomaContainingIgnoreCase(String idioma);

    @Query("SELECT COUNT(l) FROM Livro l WHERE l.idioma = :idioma")
    Long contarLivrosPorIdioma(String idioma);
}

