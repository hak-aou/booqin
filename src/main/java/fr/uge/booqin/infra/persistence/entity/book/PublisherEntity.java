package fr.uge.booqin.infra.persistence.entity.book;

import jakarta.persistence.*;

@Entity
@Table(name = "publishers")
public class PublisherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publisher_name", unique = true)
    private String publisherName;

    public PublisherEntity() {
    }

    public PublisherEntity(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

}

