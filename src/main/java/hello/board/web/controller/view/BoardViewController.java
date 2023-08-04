package hello.board.web.controller.view;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.dto.service.ArticleSearchCond;
import hello.board.dto.service.ArticleSearchType;
import hello.board.dto.view.ArticleListViewResponse;
import hello.board.dto.view.ArticleViewResponse;
import hello.board.dto.view.ArticleWriteResponse;
import hello.board.dto.view.CommentViewResponse;
import hello.board.service.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.util.PageNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardViewController {

    public static final int PAGE_SIZE = 10;

    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;


    @GetMapping("/")
    public String home() {
        return "redirect:/board";
    }

    @GetMapping("/board")
    public String getArticles(@ModelAttribute ArticleSearchCond cond,
                              @RequestParam(name = "page", defaultValue = "1") int page,
                              Model model) {

        Page<ArticleListViewResponse> articles = articleQueryService
                .search(cond, convertToZeroStartIndex(page), PAGE_SIZE)
                .map(ArticleListViewResponse::from);

        validatePageRequest(page, articles);

        model.addAttribute("cond", cond);
        model.addAttribute("articles", articles);

        PageNumberGenerator pg = PageNumberGenerator.buildFrom(articles);

        model.addAttribute("prevNumber", pg.getPreviousPage());
        model.addAttribute("pageNumbers", pg.getPageNumbers());
        model.addAttribute("nextNumber",pg.getNextPage());

        return "articleList";
}

    private static int convertToZeroStartIndex(int page) {
        if (page <= 0) {
            throw new IllegalArgumentException("Wrong Page Number");
        }

        return page - 1;
    }

    private static void validatePageRequest(int page, Page<ArticleListViewResponse> articles) {
        if (page >= articles.getTotalPages()) {
            throw new IllegalArgumentException("Wrong Page Request");
        }
    }

    @GetMapping("/board/{id}")
    public String getArticle(@PathVariable Long id, Model model) {

        Article article = articleService.lookUpArticle(id);
        model.addAttribute("article", ArticleViewResponse.from(article));

        return "article";
    }

    @GetMapping("/board/new-article")
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) {
            createArticle(model);
        } else {
            modifyArticle(id, model);
        }

        return "newArticle";
    }

    private void modifyArticle(Long id, Model model) {
        Article article = articleQueryService.findById(id);
        model.addAttribute("article", ArticleWriteResponse.from(article));

    }

    private void createArticle(Model model) {
        model.addAttribute("article", ArticleWriteResponse.empty());
    }



    @GetMapping("/board/{articleId}/modifying-comment")
    public String modifyComment(@PathVariable Long articleId, @RequestParam Long id, Model model) {
        Comment comment = commentQueryService.getCommentForModifying(id, articleId);
        model.addAttribute("comment", CommentViewResponse.from(comment));

        return "modifyComment";
    }

    @ModelAttribute
    public ArticleSearchType[] articleSearchTypes() {
        return ArticleSearchType.values();
    }

}
