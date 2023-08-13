package hello.board.util;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class PageNumberGenerator {

    private final int previousPage;
    private final List<Integer> pageNumbers;
    private final int nextPage;

    private PageNumberGenerator(int previousPage, List<Integer> pageNumbers, int nextPage) {
        this.previousPage = previousPage;
        this.pageNumbers = new ArrayList<>(pageNumbers);
        this.nextPage = nextPage;
    }

    private static PageNumberGenerator build(int pageNumber, int size, int totalPage) {

        int defaultPrevPage = defaultPrevPage(pageNumber, size);
        int startPage = getStartPage(defaultPrevPage);
        int prevPage = finalPrevPage(defaultPrevPage);

        int defaultNextPage = defaultNextPage(pageNumber, size);
        int nextPage = finalNextPage(totalPage, defaultNextPage);
        int endPage = getEndPage(defaultNextPage, nextPage);

        List<Integer> pageNumbers = getPageNumbers(startPage, endPage);

        return new PageNumberGenerator(prevPage + 1, pageNumbers, nextPage + 1);
    }

    public static <T> PageNumberGenerator buildFrom(Page<T> page) {
        if (page.isEmpty()) {
            return new PageNumberGenerator(1, List.of(1), 1);
        }
        return PageNumberGenerator.build(page.getNumber(), page.getSize(), page.getTotalPages());
    }

    private static int getStartPage(int defaultPrevPage) {
        return defaultPrevPage + 1;
    }

    private static List<Integer> getPageNumbers(int startPage, int endPage) {
        List<Integer> pageNums = new ArrayList<>();

        for (int p = startPage; p <= endPage; p++) {
            pageNums.add(p+1);
        }
        return pageNums;
    }

    private static int getEndPage(int defaultNextPage, int nextPage) {
        return Math.min(defaultNextPage - 1, nextPage);
    }

    private static int finalNextPage(int totalPage, int defaultNextPage) {
        return Math.min(defaultNextPage, (totalPage - 1));
    }

    private static int defaultNextPage(int pageNumber, int size) {
        return (pageNumber / size + 1) * size;
    }

    private static int finalPrevPage(int defaultPrevPage) {
        return Math.max(defaultPrevPage, 0);
    }

    private static int defaultPrevPage(int pageNumber, int size) {
        return (pageNumber / size) * size - 1;
    }
}
