@import gg.jte.Content
@import org.springframework.security.web.csrf.CsrfToken
@param CsrfToken _csrf = null
@param Content content
@param fr.uge.booqin.app.controller.mvc.session.SessionData session = null
@param String nonce

<!DOCTYPE html>
<html lang="en">
<head>
    <title>BooqIn</title>
    <link rel="stylesheet" href="css/style.css">

</head>
<body>
@template.layout.navbar(session = session)
<main class="max-w-7xl mx-auto">
    <div class="flex justify-center min-h-screen">
        <div class="w-3/4 p-4 bg-white rounded-lg shadow-lg h-full">
            <div class="p-4">
                <div class="max-w-4xl mx-auto mt-8">
                    ${content}
                    @if(content == null)
                        <p>
                            There is nothing to see here.
                        </p>
                    @endif
                </div>
            </div>
        </div>
    </div>
</main>
<script src="https://cdn.tailwindcss.com" nonce="${nonce}"></script>
<script>
    /*console.log("Tailwind:", window.tailwind);
    console.log(tailwind);
    console.log(tailwind.config);*/
</script>

</body>
</html>