package fr.uge.booqin.infra.persistence.entity.collection.smart.filter;

import fr.uge.booqin.domain.model.collection.smart.filter.AuthorFilterModel;
import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import jakarta.persistence.*;

import java.util.Objects;


@Entity
@Table(name = "authors_filters")
public class AuthorFilterEntity extends SmartCollectionFilterEntity {

    @ManyToOne
    private AuthorEntity author;

    public AuthorFilterEntity() {
    }

    public AuthorFilterEntity(AuthorEntity author) {
        this.author = Objects.requireNonNull(author, "author is required");
    }

    public AuthorEntity getAuthor() {
        return author;
    }

    public void setAuthor(AuthorEntity author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "AuthorFilter{" +
                "author=" + author +
                '}';
    }

    @Override
    public AuthorFilterModel convertTo() {
        return new AuthorFilterModel(author.getName());
    }
}