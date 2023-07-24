package hello.board.web.controller.view;

import hello.board.domain.Article;
import hello.board.dto.service.ArticleCommentsDto;
import hello.board.dto.view.ArticleListViewResponse;
import hello.board.dto.view.ArticleViewResponse;
import hello.board.dto.view.ArticleWriteResponse;
import hello.board.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BoardViewController {

    private final ArticleService articleService;

    @GetMapping("/")
    public String home() {
        return "redirect:/board";
    }

    @GetMapping("/board")
    public String getArticles(Model model) {
        List<ArticleListViewResponse> articles = articleService.findAll().stream()
                .map(ArticleListViewResponse::from)
                .toList();

        model.addAttribute("articles", articles);

        return "articleList";
    }

    @GetMapping("/board/{id}")
    public String getArticle(@PathVariable Long id, Model model) {
        ArticleCommentsDto articleComments = articleService.findByIdWithComments(id);
        ArticleViewResponse article = ArticleViewResponse.from(articleComments.getArticle(), articleComments.getComments());
        model.addAttribute("article", article);

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
        Article article = articleService.findById(id);
        model.addAttribute("article", ArticleWriteResponse.from(article));
    }

    private void createArticle(Model model) {
        model.addAttribute("article", ArticleWriteResponse.emptyResponse());
    }


}
