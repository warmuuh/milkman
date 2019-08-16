package milkman.plugin.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import milkman.ui.plugin.ContentTypePlugin;

public class SqlContentType implements ContentTypePlugin {


    private static final String[] KEYWORDS = new String[] {
    		"ADD", "EXTERNAL", "PROCEDURE", "ALL", "FETCH", "PUBLIC", "ALTER", "FILE", "RAISERROR", 
    		"AND", "FILLFACTOR", "READ", "ANY", "FOR", "READTEXT", "AS", "FOREIGN", "RECONFIGURE", 
    		"ASC", "FREETEXT", "REFERENCES", "AUTHORIZATION", "FREETEXTTABLE", "BACKUP", "FROM", 
    		"RESTORE", "BEGIN", "FULL", "RESTRICT", "BETWEEN", "FUNCTION", "RETURN", "BREAK", 
    		"GOTO", "REVERT", "BROWSE", "GRANT", "REVOKE", "BULK", "GROUP", "RIGHT", "BY", "HAVING", 
    		"ROLLBACK", "CASCADE", "HOLDLOCK", "ROWCOUNT", "CASE", "IDENTITY", "ROWGUIDCOL", "CHECK", 
    		"IDENTITY_INSERT", "RULE", "CHECKPOINT", "IDENTITYCOL", "SAVE", "CLOSE", "IF", "SCHEMA", 
    		"CLUSTERED", "IN", "SECURITYAUDIT", "COALESCE", "INDEX", "SELECT", "COLLATE", "INNER", 
    		"SEMANTICKEYPHRASETABLE", "COLUMN", "INSERT", "SEMANTICSIMILARITYDETAILSTABLE", "COMMIT", 
    		"INTERSECT", "SEMANTICSIMILARITYTABLE", "COMPUTE", "INTO", "SESSION_USER", "CONSTRAINT", 
    		"IS", "SET", "CONTAINS", "JOIN", "SETUSER", "CONTAINSTABLE", "KEY", "SHUTDOWN", "CONTINUE", 
    		"KILL", "SOME", "CONVERT", "LEFT", "STATISTICS", "CREATE", "LIKE", "SYSTEM_USER", "CROSS", 
    		"LINENO", "TABLE", "CURRENT", "LOAD", "TABLESAMPLE", "CURRENT_DATE", "MERGE", "TEXTSIZE", 
    		"CURRENT_TIME", "NATIONAL", "THEN", "CURRENT_TIMESTAMP", "NOCHECK", "TO", "CURRENT_USER", 
    		"NONCLUSTERED", "TOP", "CURSOR", "NICHT", "TRAN", "DATABASE", "NULL", "TRANSACTION", "DBCC", 
    		"NULLIF", "TRIGGER", "DEALLOCATE", "OF", "TRUNCATE", "DECLARE", "OFF", "TRY_CONVERT", "DEFAULT", 
    		"OFFSETS", "TSEQUAL", "DELETE", "ON", "UNION", "DENY", "OPEN", "UNIQUE", "DESC", "OPENDATASOURCE", 
    		"UNPIVOT", "DISK", "OPENQUERY", "UPDATE", "DISTINCT", "OPENROWSET", "UPDATETEXT", "DISTRIBUTED", 
    		"OPENXML", "USE", "DOUBLE", "OPTION", "Benutzer", "DROP", "oder", "VALUES", "DUMP", "ORDER", "VARYING", 
    		"ELSE", "OUTER", "VIEW", "END", "OVER", "WAITFOR", "ERRLVL", "PERCENT", "WHEN", "ESCAPE", "PIVOT", "WHERE", 
    		"EXCEPT", "PLAN", "WHILE", "EXEC", "PRECISION", "mit", "PRIMARY", "WITHIN", "GROUP", "EXISTS", 
    		"PRINT", "WRITETEXT", "EXIT", "PROC"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "--[^\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
);
    
    
	@Override
	public String getName() {
		return "Sql";
	}

	@Override
	public String getContentType() {
		return "application/sql";
	}

	@Override
	public boolean supportFormatting() {
		return false;
	}

	@Override
	public String formatContent(String text) {
		return null;
	}
	
	@Override
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text.toUpperCase());
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "plain" :
                    matcher.group("BRACE") != null ? "object" :
                    matcher.group("BRACKET") != null ? "array" :
                    matcher.group("SEMICOLON") != null ? "plain" :
                    matcher.group("STRING") != null ? "value" :
                    matcher.group("COMMENT") != null ? "comment" :
                    "plain";
            spansBuilder.add(Collections.singleton("plain"), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
}
        spansBuilder.add(Collections.singleton("plain"), text.length() - lastKwEnd);
        return spansBuilder.create();
	}

}
