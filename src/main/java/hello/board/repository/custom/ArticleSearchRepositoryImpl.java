package hello.board.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleCommentCountDto;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.service.search.ArticleSearchType;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static com.querydsl.core.types.Projections.constructor;
import static hello.board.domain.QArticle.article;
import static hello.board.domain.QComment.comment;
import static hello.board.domain.QUser.user;
import static java.util.stream.Collectors.toMap;

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

        List<ArticleSearchDto> content = addCommentCountInfo(articles);

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

        List<ArticleSearchDto> content = addCommentCountInfo(articles);

        JPAQuery<Long> countQuery = getConditionCountQuery(cond);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<ArticleSearchDto> addCommentCountInfo(List<Article> articles) {

        List<ArticleCommentCountDto> articleCommentCounts = query
                .select(constructor(ArticleCommentCountDto.class,
                        comment.article,
                        Wildcard.count
                ))
                .from(comment)
                .where(comment.article.in(articles))
                .groupBy(comment.article)
                .fetch();

        return toArticleSearchDtos(articles, articleCommentCounts);
    }

    private static List<ArticleSearchDto> toArticleSearchDtos(List<Article> articles, List<ArticleCommentCountDto> articleCommentCount) {
        final Map<Article, Long> articleCommentCountMap = articleCommentCount.stream()
                .collect(toMap(
                        ArticleCommentCountDto::getArticle,
                        ArticleCommentCountDto::getNumberOfComments
                ));

        return articles.stream()
                .map(atc -> new ArticleSearchDto(atc, articleCommentCountMap.getOrDefault(atc, 0L)))
                .toList();
    }

    private JPAQuery<Long> getSimpleCountQuery() {
        return query
                .select(article.count())
                .from(article);
    }

    private JPAQuery<Long> getConditionCountQuery(ArticleSearchCond cond) {
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

    private static void validateCondition(ArticleSearchCond cond) {
        if (cond == null) {
            throw new IllegalArgumentException("Article Search Condition is Null");
        }
    }

}
