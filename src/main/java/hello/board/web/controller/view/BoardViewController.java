package hello.board.web.controller.view;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchType;
import hello.board.dto.view.BoardRequest;
import hello.board.dto.view.CommentViewResponse;
import hello.board.dto.view.UserViewResponse;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.util.ViewPageNumbers;
import hello.board.web.annotation.Login;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static hello.board.dto.service.ArticleServiceDto.LookUp;
import static hello.board.dto.view.ArticleResponse.*;

@Controller
@RequiredArgsConstructor
public class BoardViewController {

    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;

    private final ArticleService articleService;

    private final ArticleServiceDtoResolver dtoResolver;

    @GetMapping("/")
    public String home() {
        return "redirect:/board";
    }

    @GetMapping("/board")
    public String getArticles(@ModelAttribute BoardRequest.ListView request, @Login User user, Model model) {

        Pageable pageable = dtoResolver.toPageable(request);
        ArticleSearchCond cond = dtoResolver.toSearchCond(request);

        Page<ListView> articles = articleQueryService.search(cond, pageable)
                .map(ListView::from);

        model.addAttribute("user", UserViewResponse.of(user));
        model.addAttribute("cond", cond);
        model.addAttribute("articles", articles);
        addPageAttribute(model, ViewPageNumbers.of(articles));

        return "articleList";
    }

    @GetMapping("/board/{id}")
    public String getArticle(@ModelAttribute BoardRequest.View request, @Login User user, @PathVariable Long id, Model model) {

        Pageable pageable = dtoResolver.toPageable(request);
        LookUp article = articleService.lookUp(id, pageable);
        Page<Comment> comments = article.getComments();

        model.addAttribute("user", UserViewResponse.of(user));
        model.addAttribute("article", View.of(article));
        addPageAttribute(model, ViewPageNumbers.of(comments));

        return "article";
    }

    @GetMapping("/board/new-article")
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("article", Write.empty());
        } else {
            Article article = articleQueryService.findById(id);
            model.addAttribute("article", Write.from(article));
        }

        return "newArticle";
    }

    @GetMapping("/board/{articleId}/modifying-comment")
    public String modifyComment(@RequestParam Long id, @PathVariable Long articleId, Model model) {
        Comment comment = commentQueryService.findWithArticle(id, articleId);
        model.addAttribute("comment", CommentViewResponse.of(comment));

        return "modifyComment";
    }

    @ModelAttribute
    public ArticleSearchType[] articleSearchTypes() {
        return ArticleSearchType.values();
    }

    private static void addPageAttribute(Model model, ViewPageNumbers viewPageNumbers) {
        model.addAttribute("prevNumber", viewPageNumbers.getPreviousPage());
        model.addAttribute("pageNumbers", viewPageNumbers.getPageNumbers());
        model.addAttribute("nextNumber", viewPageNumbers.getNextPage());
    }
}
