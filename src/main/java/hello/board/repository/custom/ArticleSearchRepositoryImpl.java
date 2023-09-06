package hello.board.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.service.search.ArticleSearchType;
import hello.board.dto.service.search.CommentCountDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.querydsl.core.types.Projections.constructor;
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

        List<ArticleSearchDto> content = getArticleSearchDtos(articles);

        JPAQuery<Long> countQuery = getSimpleCountQuery();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ArticleSearchDto> search(ArticleSearchCond cond, Pageable pageable) {

        List<Article> articles = query
                .select(article)
                .from(article)
                .leftJoin(article.author, user).fetchJoin()
                .where(containsKeyword(cond))
                .orderBy(article.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ArticleSearchDto> content = getArticleSearchDtos(articles);

        JPAQuery<Long> countQuery = getConditionCountQuery(cond);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<ArticleSearchDto> getArticleSearchDtos(List<Article> articles) {
        List<CommentCountDto> commentCounts = query
                .select(constructor(CommentCountDto.class,
                        comment.article.id,
                        Wildcard.count
                ))
                .from(comment)
                .where(comment.article.in(articles))
                .groupBy(comment.article.id)
                .orderBy(comment.article.id.desc())
                .fetch();

        return mergeIntoArticleSearchDtos(articles, commentCounts);
    }

    private static List<ArticleSearchDto> mergeIntoArticleSearchDtos(List<Article> articles, List<CommentCountDto> commentCounts) {

        final List<ArticleSearchDto> content = new ArrayList<>();

        int idxArticles = 0, idxCounts = 0;

        while (idxArticles < articles.size() && idxCounts < commentCounts.size()) {

            final Article atcle = articles.get(idxArticles++);
            final CommentCountDto commentCountDto = commentCounts.get(idxCounts);

            if (doTheyPointSameArticle(atcle, commentCountDto)) {
                content.add(new ArticleSearchDto(atcle, commentCountDto.getCount()));
                idxCounts++;
            } else {
                content.add(new ArticleSearchDto(atcle, 0L));
            }
        }

        while (idxArticles < articles.size()) {
            content.add(new ArticleSearchDto(articles.get(idxArticles++), 0L));
        }

        return content;
    }

    private static boolean doTheyPointSameArticle(Article atc, CommentCountDto commentCountDto) {
        return Objects.equals(atc.getId(), commentCountDto.getArticleId());
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

}
