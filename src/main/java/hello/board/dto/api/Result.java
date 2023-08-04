package hello.board.dto.api;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public final class Result<T> {
    private final List<T> args;

    private Result(Collection<T> args) {
        this.args = new ArrayList<>(args);
    }

    public static <T> Result<T> of(Collection<T> args) {
        return new Result<>(args);
    }

}
