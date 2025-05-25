package fr.uge.booqin.infra.external.book.query_parameter;

public enum QueryLanguage {
    ENGLISH("english", "eng", "en"),
    GERMAN("german", "ger", "de"),
    FRENCH("french", "fre", "fr"),
    SPANISH("spanish", "spa", "es"),
    RUSSIAN("russian", "rus", "ru"),
    ITALIAN("italian", "ita", "it"),
    CHINESE("chinese", "chi", "zh-CN"),
    JAPANESE("japanese", "jpn", "ja"),
    ARABIC("arabic", "ara", "ar"),
    PORTUGUESE("portuguese", "por", "pt-BR");

    private final String name;
    private final String openLibraryLanguage;
    private final String googleBooksLanguage;

    QueryLanguage(String name, String openLibraryLanguage, String googleBooksLanguage) {
        this.name = name;
        this.openLibraryLanguage = openLibraryLanguage;
        this.googleBooksLanguage = googleBooksLanguage;
    }

    public String getName() {
        return name;
    }

    public String getOpenLibraryFormat() {
        return openLibraryLanguage;
    }

    public String getGoogleFormat() {
        return googleBooksLanguage;
    }

    public static String getLanguageByAbbreviation(String languageAbbreviation) {
        for (QueryLanguage language : QueryLanguage.values()) {
            if (language.getOpenLibraryFormat().equalsIgnoreCase(languageAbbreviation) || language.getGoogleFormat().equalsIgnoreCase(languageAbbreviation)) {
                return language.name();
            }
        }
        return null;
    }

}
