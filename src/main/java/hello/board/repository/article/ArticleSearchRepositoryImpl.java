package hello.board.repository.article;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.board.domain.Article;
import hello.board.dto.service.ArticleSearchCond;
import hello.board.dto.service.ArticleSearchType;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static hello.board.domain.QArticle.article;
import static hello.board.domain.QUser.user;

@Slf4j
public class ArticleSearchRepositoryImpl implements ArticleSearchRepository {

    private final JPAQueryFactory query;

    public ArticleSearchRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<Article> search(Pageable pageable) {
        List<Article> content = query
                .select(article)
                .from(article)
                .join(article.author, user).fetchJoin()
                .orderBy(article.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = getBasicCountQuery();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Article> search(ArticleSearchCond cond, Pageable pageable) {
        validateCondition(cond);

        BooleanExpression booleanExpression = searchTypeMatching(cond);

        List<Article> content = query
                .select(article)
                .from(article)
                .join(article.author, user).fetchJoin()
                .where(booleanExpression)
                .orderBy(article.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = getCountQuery(cond);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private JPAQuery<Long> getBasicCountQuery() {
        return query
                .select(article.count())
                .from(article);
    }

    private JPAQuery<Long> getCountQuery(ArticleSearchCond cond) {

        JPAQuery<Long> basicCountQuery = getBasicCountQuery();

        if (cond.getType() == ArticleSearchType.AUTHOR) {
            basicCountQuery.join(article.author, user).fetchJoin();
        }

        return basicCountQuery
                .where(searchTypeMatching(cond));
    }

    private static void validateCondition(ArticleSearchCond cond) {
        if (cond == null) {
            throw new IllegalArgumentException("Article Search Condition is Null");
        }
    }


    private BooleanExpression searchTypeMatching(ArticleSearchCond cond) {

        if (!StringUtils.hasText(cond.getKeyword())) {
            return null;
        }

        return switch (cond.getType()) {
            case TITLE -> article.title.contains(cond.getKeyword());
            case CONTENT -> article.content.contains(cond.getKeyword());
            case TITLE_AND_CONTENT -> article.title.contains(cond.getKeyword()).or(article.content.contains(cond.getKeyword()));
            case AUTHOR -> article.author.username.contains(cond.getKeyword());
        };
    }

}
