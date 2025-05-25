@param java.util.List<fr.uge.booqin.domain.model.books.BookModel> books

<!DOCTYPE html>
<html>
<head>
    <title>Book Details</title>
</head>
<body>

<h3>Total : ${books.size()}</h3>

@for(var book : books)
    <p>${book.isbn().isbn_13()}, ${book.title()} by ${String.join(", ", book.authors())}</p>
@endfor


</body>
</html>