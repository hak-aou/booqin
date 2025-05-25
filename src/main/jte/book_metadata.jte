@param fr.uge.booqin.domain.model.books.BookModel book

<!DOCTYPE html>
<html>
<head>
    <title>Book Details</title>
</head>
<body>

<p>Title:
    @if(book.title() != null)
        ${book.title()}
    @else
        Unknow
    @endif
</p>

<p>Isbn 13:
    @if(book.isbn() != null && book.isbn().isbn_13() != null)
        ${book.isbn().isbn_13()}
    @else
        Unknow
    @endif
</p>

<p>Isbn 10:
    @if(book.isbn() != null && book.isbn().isbn_10() != null)
        ${book.isbn().isbn_10()}
    @else
        Unknow
    @endif
</p>

<p>Authors:
    @if(book.authors() != null && !book.authors().isEmpty())
<ul>
    @for(var author : book.authors())
        <li>${author}</li>
    @endfor
</ul>
@else
    Unknow
@endif
</p>

<p>Publishers:
    @if(book.publishers() != null && !book.publishers().isEmpty())
    <ul>
        @for(var publisher : book.publishers())
            <li>${publisher}</li>
        @endfor
    </ul>
@else
    Unknow
@endif
</p>

<p>Published Date:
    @if(book.publishedDate() != null)
        ${book.publishedDate().toString()}
    @else
        Unknow
    @endif
</p>

<p>Categories:
    @if(book.categories() != null && !book.categories().isEmpty())
    <ul>
        @for(var category : book.categories())
            <li>${category}</li>
        @endfor
    </ul>
@else
    Unknow
@endif
</p>

<p>Languages:
    @if(book.language() != null)
        ${book.language()}
    @else
        Unknow
    @endif
</p>

<p>Image Small:
    @if(book.imageLinks() != null && book.imageLinks().getSmall() != null)
        <img src="${book.imageLinks().getSmall()}">
    @else
        Unknow
    @endif
</p>

<p>Image Medium:
    @if(book.imageLinks() != null && book.imageLinks().getMedium() != null)
        <img src="${book.imageLinks().getMedium()}">
    @else
        Unknow
    @endif
</p>

<p>Image Large:
    @if(book.imageLinks() != null && book.imageLinks().getLarge() != null)
        <img src="${book.imageLinks().getLarge()}">
    @else
        Unknow
    @endif
</p>

<p>Subtitle:
    @if(book.subtitle() != null)
        ${book.subtitle()}
    @else
        Unknow
    @endif
</p>

<p>Description:
    @if(book.description() != null)
        ${book.description()}
    @else
        Unknow
    @endif
</p>

<p>Page Count:
    @if(book.pageCount() != null)
        ${book.pageCount()}
    @else
        Unknow
    @endif
</p>

</body>
</html>
