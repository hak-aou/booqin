package fr.uge.booqin.app.dto.book;

import fr.uge.booqin.domain.model.books.BookModelBuilder;
import fr.uge.booqin.domain.model.books.ImageFormatModel;
import fr.uge.booqin.domain.model.books.IsbnModel;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.images.GoogleImageLinks;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.informations.GoogleVolumeInfo;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.isbns.GoogleIsbnStructure;
import fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.book.OpenLibraryBook;
import fr.uge.booqin.infra.external.book.query_parameter.QueryLanguage;
import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.book.CategoryEntity;
import fr.uge.booqin.infra.persistence.entity.book.PublisherEntity;

import java.util.List;

public class BookMapper {

    private static String getIsbnByType(List<GoogleIsbnStructure> isbns, String type) {
        return isbns.stream()
                .filter(isbn -> type.equals(isbn.type()))
                .map(GoogleIsbnStructure::identifier)
                .findFirst()
                .orElse(null);
    }


    private static String changeZoomGoogleImage(String url, int zoom) {
        return url.replaceAll("zoom=\\d+", "zoom=" + zoom).replace("http:", "https:");
    }

    private static ImageFormatModel imageFromGoogle(GoogleImageLinks imageLinks) {
        if(imageLinks == null || imageLinks.smallThumbnail() == null) {
            return new ImageFormatModel(null, null, null);
        }

        String small = changeZoomGoogleImage(imageLinks.smallThumbnail(), 2);
        String medium = changeZoomGoogleImage(imageLinks.smallThumbnail(), 3);
        String big = changeZoomGoogleImage(imageLinks.smallThumbnail(), 4);
        return new ImageFormatModel(small, medium, big);
    }

    public static BookModel fromDTOtoModel(GoogleVolumeInfo googleVolumeInfo) {
        return new BookModelBuilder()
                .title(googleVolumeInfo.title())
                .isbn(
                        googleVolumeInfo.isbns() == null ?
                                new IsbnModel(
                                        null,
                                        null
                                )
                                :
                                new IsbnModel(
                                        getIsbnByType(googleVolumeInfo.isbns(), "ISBN_13"),
                                        getIsbnByType(googleVolumeInfo.isbns(), "ISBN_10")
                                )
                )
                .authors(googleVolumeInfo.authors())
                .publisher(googleVolumeInfo.publisher())
                .publishedDate(DateMapper.convertTo(googleVolumeInfo.publishedDate()))

                .categories(googleVolumeInfo.categories())
                .language(
                        QueryLanguage.getLanguageByAbbreviation(googleVolumeInfo.language())
                )
                .imageLinks(imageFromGoogle(googleVolumeInfo.imageLinks()))
                .subtitle(googleVolumeInfo.subtitle())
                .description(googleVolumeInfo.description())
                .pageCount(googleVolumeInfo.pageCount())
                .build();
    }

    public static BookModel fromDTOtoModel(OpenLibraryBook openLibraryMetadata) {
        return new BookModelBuilder()
                .title(openLibraryMetadata.title())
                .isbn(
                        new IsbnModel(
                                openLibraryMetadata.isbn_13() == null ? null : openLibraryMetadata.isbn_13().getFirst(),
                                openLibraryMetadata.isbn_10() == null ? null : openLibraryMetadata.isbn_10().getFirst()
                        )
                )
                .authors(openLibraryMetadata.authors())

                .publisher(openLibraryMetadata.publishers())
                .publishedDate(DateMapper.convertTo(openLibraryMetadata.publish_date()))

                .categories(openLibraryMetadata.subjects())
                .language(
                        QueryLanguage.getLanguageByAbbreviation(
                                openLibraryMetadata.languages().stream().map(OpenLibraryBook.Language::key).findFirst().orElse(null).replace("/languages/", "")
                        )
                )
                .imageLinks(
                        openLibraryMetadata.covers() == null ? new ImageFormatModel(
                                null,
                                null,
                                null
                        ) :
                                new ImageFormatModel(
                                        "https://covers.openlibrary.org/b/id/" + openLibraryMetadata.covers().getFirst() + "-S.jpg",
                                        "https://covers.openlibrary.org/b/id/" + openLibraryMetadata.covers().getFirst() + "-M.jpg",
                                        "https://covers.openlibrary.org/b/id/" + openLibraryMetadata.covers().getFirst() + "-L.jpg"
                                )
                )

                .subtitle(openLibraryMetadata.subtitle())
                .description(openLibraryMetadata.description())
                .pageCount(openLibraryMetadata.number_of_pages())
                .build();
    }

    public static BookModel fromEntityToModel(BookEntity bookEntity) {
        return new BookModelBuilder()
                .votableId(bookEntity.getVotable().getId())
                .title(bookEntity.getTitle())
                .id(bookEntity.getId())
                .commentableId(bookEntity.getCommentable().getId())
                .followableId(bookEntity.getFollowable().getId())
                .isbn(
                        new IsbnModel(
                                bookEntity.getIsbn13(),
                                bookEntity.getIsbn10()
                        )
                )
                .authors(bookEntity.getAuthors().stream().map(AuthorEntity::getName).toList())
                .publisher(bookEntity.getPublishers().stream().map(PublisherEntity::getPublisherName).toList())
                .publishedDate(bookEntity.getPublishedDate())
                .categories(bookEntity.getCategories().stream().map(CategoryEntity::getCategoryName).toList())
                .language(bookEntity.getLanguage().getLanguageName())
                .imageLinks(bookEntity.getImageLinks())
                .subtitle(bookEntity.getSubtitle())
                .description(bookEntity.getDescription())
                .pageCount(bookEntity.getPageCount())
                .build();
    }
}