@param java.util.List<fr.uge.booqin.domain.model.books.IsbnModel> isbns

<!DOCTYPE html>
<html>
<head>
    <title>All ISBN</title>
</head>
<body>
<p>Isbns:</p>
    @if(isbns.size() > 0)
        <ul>
            @for(var isbn : isbns)
                <li>${isbn.isbn_13()}</li>
            @endfor
        </ul>
    @else
        <p>Unknown</p>
    @endif
</body>
</html>
