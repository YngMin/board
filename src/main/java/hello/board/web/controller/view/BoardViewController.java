package hello.board.web.controller.view;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchType;
import hello.board.dto.view.ArticleResponse;
import hello.board.dto.view.CommentViewResponse;
import hello.board.dto.view.UserViewResponse;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.util.PageNumberGenerator;
import hello.board.web.annotation.Login;
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

    public static final int ARTICLE_PAGE_SIZE = 10;
    public static final int COMMENT_PAGE_SIZE = 20;


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
                              @Login User user,
                              Model model) {

        model.addAttribute("user", UserViewResponse.from(user));

        Page<ArticleResponse.ListView> articles = articleQueryService
                .search(cond, toZeroStartIdx(page), ARTICLE_PAGE_SIZE)
                .map(ArticleResponse.ListView::from);

        validatePageRequest(page, articles);

        model.addAttribute("cond", cond);
        model.addAttribute("articles", articles);

        PageNumberGenerator pg = PageNumberGenerator.buildFrom(articles);

        model.addAttribute("prevNumber", pg.getPreviousPage());
        model.addAttribute("pageNumbers", pg.getPageNumbers());
        model.addAttribute("nextNumber",pg.getNextPage());


        return "articleList";
}

    @GetMapping("/board/{id}")
    public String getArticle(@RequestParam(defaultValue = "1") int page, @Login User user, @PathVariable Long id, Model model) {

        model.addAttribute("user", UserViewResponse.from(user));

        ArticleServiceDto.LookUp article = articleService
                .lookUpWithPaginatedComments(id, toZeroStartIdx(page), COMMENT_PAGE_SIZE);

        validatePageRequest(page, article.getComments());

        model.addAttribute("article", ArticleResponse.View.from(article));

        PageNumberGenerator pg = PageNumberGenerator.buildFrom(article.getComments());

        model.addAttribute("prevNumber", pg.getPreviousPage());
        model.addAttribute("pageNumbers", pg.getPageNumbers());
        model.addAttribute("nextNumber",pg.getNextPage());

        return "article";
    }

    private static int toZeroStartIdx(int page) {
        if (page <= 0) {
            throw new IllegalArgumentException("Wrong Page Number");
        }

        return page - 1;
    }

    private static <T> void validatePageRequest(int page, Page<T> result) {
        if (result.getTotalPages() == 0) {
            if (page > 1) {
                throw new IllegalArgumentException("Wrong Page Request");
            }
        }

        else if (page > result.getTotalPages()) {
            throw new IllegalArgumentException("Wrong Page Request");
        }
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
        model.addAttribute("article", ArticleResponse.Write.from(article));

    }

    private void createArticle(Model model) {
        model.addAttribute("article", ArticleResponse.Write.empty());
    }

    @GetMapping("/board/{articleId}/modifying-comment")
    public String modifyComment(@PathVariable Long articleId, @RequestParam Long id, Model model) {
        Comment comment = commentQueryService.findWithArticle(id, articleId);
        model.addAttribute("comment", CommentViewResponse.from(comment));

        return "modifyComment";
    }

    @ModelAttribute
    public ArticleSearchType[] articleSearchTypes() {
        return ArticleSearchType.values();
    }


}
