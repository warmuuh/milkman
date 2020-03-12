package milkman.ui.components;

import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContentSearch {

    private final CodeArea codeArea;
    private String currentSearch;
    private List<MatchResult> matches;
    private boolean isFirstSearch = true;

    public ContentSearch(CodeArea codeArea) {
        this.codeArea = codeArea;
    }


    public void moveToNextMatch(String searchText){
        List<MatchResult> matches = getMatches(searchText);
        getNextMatchFrom(getCaretPosition(), matches).ifPresent(m -> {
            codeArea.selectRange(m.start(), m.end());
            codeArea.requestFollowCaret();
        });
    }


    public void moveToPrevMatch(String searchText){
        List<MatchResult> matches = getMatches(searchText);
        getPrevMatchFrom(getCaretPosition(), matches).ifPresent(m -> {
            codeArea.selectRange(m.start(), m.end());
            codeArea.requestFollowCaret();
        });
    }

    private Optional<MatchResult> getPrevMatchFrom(int caretPosition, List<MatchResult> matches) {
        Optional<MatchResult> prev = matches.stream().filter(m -> m.end() < caretPosition).reduce((a,b) -> b); // find last
        return prev.or(() -> matches.stream().filter(m -> m.start() > caretPosition).reduce((a,b) -> b));
    }

    private Optional<MatchResult> getNextMatchFrom(int caretPosition, List<MatchResult> matches) {
        Optional<MatchResult> first = matches.stream().filter(m -> m.start() > caretPosition).findFirst();
        return first.or(() -> matches.stream().filter(m -> m.end() < caretPosition).findFirst());
    }

    private int getCaretPosition() {
        if (isFirstSearch){
            return 0;
        }

        return codeArea.getCaretPosition();
    }

    private List<MatchResult>  getMatches(String searchText) {
        if (Objects.equals(searchText, currentSearch)){
            isFirstSearch = false;
            return  matches;
        }
        Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE + Pattern.LITERAL);
        var matcher = pattern.matcher(codeArea.getText());

        currentSearch = searchText;
        matches = matcher.results().collect(Collectors.toList());
        isFirstSearch = true;
        return matches;
    }

}
