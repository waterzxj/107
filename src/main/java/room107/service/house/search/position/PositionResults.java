package room107.service.house.search.position;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author WangXiao
 */
@NoArgsConstructor()
@RequiredArgsConstructor
public class PositionResults extends ArrayList<PositionResult> {

    public static final PositionResults EMPTY = new PositionResults();

    private static final long serialVersionUID = 4429909644218646086L;

    @Getter
    @NonNull
    private PositionQuery query;

    public PositionResults(Collection<? extends PositionResult> c) {
        super(c);
    }

}
