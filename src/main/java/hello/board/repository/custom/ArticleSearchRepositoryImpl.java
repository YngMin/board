package hello.board.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.service.search.ArticleSearchType;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hello.board.domain.QArticle.article;
import static hello.board.domain.QComment.comment;
import static hello.board.domain.QUser.user;

public class ArticleSearchRepositoryImpl implements ArticleSearchRepository {

    private final JPAQueryFactory query;

    public ArticleSearchRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<ArticleSearchDto> search(Pageable pageable) {

        List<Article> articles = query
                .select(article)
                .from(article)
                .leftJoin(article.author, user).fetchJoin()
                .orderBy(article.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> numOfComments = getTheNumberOfComments(articles);

        List<ArticleSearchDto> content = toArticleSearchDtos(articles, numOfComments);
        JPAQuery<Long> countQuery = getSimpleCountQuery();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ArticleSearchDto> search(ArticleSearchCond cond, Pageable pageable) {

        validateCondition(cond);

        List<Article> articles = query
                .select(article)
                .from(article)
                .leftJoin(article.author, user).fetchJoin()
                .where(containsKeyword(cond))
                .orderBy(article.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> numOfComments = getTheNumberOfComments(articles);

        List<ArticleSearchDto> content = toArticleSearchDtos(articles, numOfComments);
        JPAQuery<Long> countQuery = getCountQuery(cond);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<Long> getTheNumberOfComments(List<Article> articles) {
        return articles.isEmpty()
                ? Collections.emptyList()
                : query
                .select(Wildcard.count)
                .from(comment)
                .where(comment.article.in(articles))
                .groupBy(comment.article)
                .orderBy(comment.article.id.desc())
                .fetch();
    }

    private JPAQuery<Long> getSimpleCountQuery() {
        return query
                .select(article.count())
                .from(article);
    }

    private JPAQuery<Long> getCountQuery(ArticleSearchCond cond) {
        JPAQuery<Long> simpleCountQuery = getSimpleCountQuery();

        if (cond.getType() == ArticleSearchType.AUTHOR) {
            simpleCountQuery.join(article.author, user);
        }

        return simpleCountQuery
                .where(containsKeyword(cond));
    }

    private BooleanExpression containsKeyword(ArticleSearchCond cond) {

        String keyword = cond.getKeyword();

        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return switch (cond.getType()) {
            case TITLE -> article.title.contains(keyword);
            case CONTENT -> article.content.contains(keyword);
            case TITLE_AND_CONTENT -> article.title.contains(keyword).or(article.content.contains(keyword));
            case AUTHOR -> article.author.name.contains(keyword);
        };
    }

    private static List<ArticleSearchDto> toArticleSearchDtos(List<Article> articles, List<Long> commentsCounts) {
        List<ArticleSearchDto> content = new ArrayList<>();

        if (articles.size() != commentsCounts.size()) {
            throw new IllegalStateException();
        }

        for (int i = 0; i < articles.size(); i++) {
            content.add(new ArticleSearchDto(articles.get(i), commentsCounts.get(i)));
        }

        return content;
    }

    private static void validateCondition(ArticleSearchCond cond) {
        if (cond == null) {
            throw new IllegalArgumentException("Article Search Condition is Null");
        }
    }

}
