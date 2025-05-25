package fr.uge.booqin.app.service.book;

import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import fr.uge.booqin.infra.persistence.repository.book.AuthorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public AuthorEntity findOrCreateAuthor(String name) {
        var author =  authorRepository.findByName(name);
        if(author.isPresent()) {
            return author.get();
        }
        var authorEntity = new AuthorEntity(name);
        authorRepository.save(authorEntity);
        return authorEntity;
    }

    @Transactional
    public List<AuthorEntity> findAllAuthors() {
        return authorRepository.findAll();
    }
}
