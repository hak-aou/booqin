package fr.uge.booqin.app.service.book;

import fr.uge.booqin.infra.persistence.entity.book.PublisherEntity;
import fr.uge.booqin.infra.persistence.repository.book.PublisherRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {
    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Transactional
    public PublisherEntity findOrCreatePublisher(String name) {
        var publisher = publisherRepository.findByName(name);
        if(publisher.isPresent()) {
            return publisher.get();
        }
        var publisherEntity = new PublisherEntity(name);
        publisherRepository.save(publisherEntity);
        return publisherEntity;
    }

}
