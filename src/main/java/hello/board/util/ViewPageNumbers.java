package hello.board.util;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public final class ViewPageNumbers {

    private final int previousPage;
    private final List<Integer> pageNumbers;
    private final int nextPage;

    private ViewPageNumbers(int previousPage, List<Integer> pageNumbers, int nextPage) {
        this.previousPage = previousPage;
        this.pageNumbers = new ArrayList<>(pageNumbers);
        this.nextPage = nextPage;
    }

    private static ViewPageNumbers build(int pageNumber, int size, int totalPage) {

        int defaultPrevPage = defaultPrevPage(pageNumber, size);
        int startPage = getStartPage(defaultPrevPage);
        int prevPage = finalPrevPage(defaultPrevPage);

        int defaultNextPage = defaultNextPage(pageNumber, size);
        int nextPage = finalNextPage(totalPage, defaultNextPage);
        int endPage = getEndPage(defaultNextPage, nextPage);

        List<Integer> pageNumbers = getPageNumbers(startPage, endPage);

        return new ViewPageNumbers(prevPage + 1, pageNumbers, nextPage + 1);
    }

    public static <T> ViewPageNumbers of(Page<T> page) {
        if (page.isEmpty()) {
            return new ViewPageNumbers(1, List.of(1), 1);
        }
        return ViewPageNumbers.build(page.getNumber(), page.getSize(), page.getTotalPages());
    }

    private static int getStartPage(int defaultPrevPage) {
        return defaultPrevPage + 1;
    }

    private static List<Integer> getPageNumbers(int startPage, int endPage) {
        return IntStream.range(startPage, endPage + 1)
                .boxed()
                .toList();
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
