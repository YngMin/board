package hello.board.web.aspect;

import hello.board.dto.view.ArticleResponse;
import hello.board.dto.view.BoardRequest.ArticleListRequest;
import hello.board.dto.view.BoardRequest.ArticleRequest;
import hello.board.exception.WrongPageRequestException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.util.Arrays;

@Slf4j
@Aspect
public class PageRequestValidationAspect {

    @Value("${view.board.article-page-size}")
    private int ARTICLE_PAGE_SIZE;

    @Value("${view.board.comment-page-size}")
    private int COMMENT_PAGE_SIZE;

    @AfterReturning("execution(* hello.board.web.controller.view.BoardViewController.*(..)) && args(request, ..)")
    public void validateMaliciousArticlePageRequest(JoinPoint joinPoint, ArticleListRequest request) {
        Model model = getModel(joinPoint);

        if (model != null) {
            Object attribute = model.getAttribute("articles");

            if (attribute instanceof Page<?> pageResult) {
                filterOutMaliciousRequest(request.getPage(), ARTICLE_PAGE_SIZE, pageResult);
            }
        }
    }

    @AfterReturning("execution(* hello.board.web.controller.view.BoardViewController.*(..)) && args(request, ..)")
    public void validateMaliciousArticlePageRequest(JoinPoint joinPoint, ArticleRequest request) {
        Model model = getModel(joinPoint);

        if (model != null) {
            Object attribute = model.getAttribute("article");

            if (attribute instanceof ArticleResponse.View response) {
                filterOutMaliciousRequest(request.getPage(), COMMENT_PAGE_SIZE, response.getComments());
            }
        }
    }


    private void filterOutMaliciousRequest(int page, int size, Page<?> pageResult) {
        if (pageResult.getTotalPages() == 0) {
            if (page > 1) {
                throw WrongPageRequestException.of(page, size);
            }
        }

        else if (page > pageResult.getTotalPages()) {
            throw WrongPageRequestException.of(page, size);
        }
    }

    private static Model getModel(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof Model)
                .map(o -> (Model) o)
                .findAny()
                .orElse(null);
    }
}
