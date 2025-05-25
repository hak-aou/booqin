package fr.uge.booqin.app.service.book;

import fr.uge.booqin.infra.persistence.entity.book.LanguageEntity;
import fr.uge.booqin.infra.persistence.repository.book.LanguageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageService {
    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Transactional
    public LanguageEntity findOrCreateLanguage(String name) {
        var language = languageRepository.findByLanguageName(name);
        if(language.isPresent()) {
            return language.get();
        }
        var languageEntity = new LanguageEntity(name);
        languageRepository.save(languageEntity);
        return languageEntity;
    }

    @Transactional
    public List<String> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(LanguageEntity::getLanguageName)
                .toList();
    }


}
