package hello.board.dto.service.search;

public enum ArticleSearchType {
    TITLE("제목"),
    CONTENT("내용"),
    TITLE_AND_CONTENT("제목 및 내용"),
    AUTHOR("작성자");

    private final String kor;

    ArticleSearchType(String kor) {
        this.kor = kor;
    }

    public String getKor() {
        return kor;
    }
}
