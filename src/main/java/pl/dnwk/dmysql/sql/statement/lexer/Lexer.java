package pl.dnwk.dmysql.sql.statement.lexer;

import com.mysql.cj.util.StringUtils;
import pl.dnwk.dmysql.sql.statement.ParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    public static final int T_NONE = 1;
    public static final int T_INT = 2;
    public static final int T_FLOAT = 3;
    public static final int T_STRING = 4;
    public static final int T_COMMA = 5;
    public static final int T_DOT = 6;
    public static final int T_EQUALS = 7;
    public static final int T_GT = 8;
    public static final int T_LT = 9;
    public static final int T_NEGATE = 10;
    public static final int T_MINUS = 11;
    public static final int T_MULTIPLY = 12;
    public static final int T_PLUS = 13;
    public static final int T_DIVIDE = 14;
    public static final int T_OPEN_PARENTHESIS = 15;
    public static final int T_CLOSE_PARENTHESIS = 16;
    public static final int T_EOF = 99;

    public static final int T_IDENTIFIER = 101;

    public static final int T_SELECT = 201;
    public static final int T_FROM = 202;
    public static final int T_WHERE = 203;
    public static final int T_GROUP = 204;
    public static final int T_BY = 205;
    public static final int T_HAVING = 206;
    public static final int T_ORDER = 207;
    public static final int T_OUTER = 208;
    public static final int T_INNER = 209;
    public static final int T_JOIN = 210;
    public static final int T_AND = 211;
    public static final int T_OR = 212;
    public static final int T_AS = 213;
    public static final int T_IN = 214;
    public static final int T_LEFT = 215;
    public static final int T_RIGHT = 216;
    public static final int T_ON = 217;
    public static final int T_LIKE = 218;
    public static final int T_NOT = 219;
    public static final int T_NULL = 220;
    public static final int T_ASC = 221;
    public static final int T_DESC = 222;

    public static final int T_INSERT = 230;
    public static final int T_INTO = 231;
    public static final int T_VALUES = 232;
    public static final int T_DELETE = 233;
    public static final int T_UPDATE = 234;
    public static final int T_SET = 235;

    private static final Map<String, Integer> reserved = new HashMap<>();
    private static final Map<Integer, String> tokenNames = new HashMap<>();

    private String input;
    private Token[] tokens = new Token[0];
    private int position = 0;
    private int peek = 0;

    private Token currentToken;
    private Token nextToken;

    public Lexer(String input) {
        this.input = input;

        scan();
        reset();
    }

    public boolean moveNext() {
        peek = 0;
        currentToken = nextToken;
        nextToken = tokens.length > position ? tokens[position++] : null;

        return currentToken != null;
    }

    public Token getToken() {
        return currentToken;
    }

    public Token getNextToken() {
        return nextToken;
    }

    public boolean is(int type) {
        return currentToken.type == type;
    }

    public boolean nextIs(int type) {
        return nextToken != null && nextToken.type == type;
    }

    public Token peek() {
        if (tokens.length - 1 <= position + peek) {
            return null;
        }

        return tokens[position + peek++];
    }

    public void resetPeek() {
        peek = 0;
    }

    public void match(int type) {
        if(!nextIs(type)) {
            StringBuilder after = new StringBuilder();
            for(int i = 25; i >=2; i--) {
                after.append(position >= i ? tokens[position - i].value + " " : "");
            }
            after.append(position >= 1 ? ">>> " + tokens[position - 1].value + " <<<" : "??");
            throw new ParseException("Expected token " + tokenNames.get(type) + " got " + tokenNames.get(getNextToken().type) + " in: " + after );
        }

        moveNext();
    }

    public void reset() {
        peek = 0;
        position = 0;
        moveNext();
    }

    private void scan() {
        String regex = "/([catchablePatterns])|[nonCatchablePatterns]/iu"
                .replace("[catchablePatterns]", String.join(")|(", getCatchablePatterns()))
                .replace("[nonCatchablePatterns]", String.join("|", getNonCatchablePatterns()));

        Pattern p = Pattern.compile(regex);
        Matcher regexMatcher = p.matcher(input);

        List<Token> tokensList = new ArrayList<>();
        while (regexMatcher.find()) {
            for (int i = 1; i <= regexMatcher.groupCount(); i++) {
                String match = regexMatcher.group(i);
                if (match != null) {
                    match = match.trim();
                    if (!match.isEmpty()) {
                        tokensList.add(createToken(match));
                    }
                }
            }
        }

        tokens = tokensList.toArray(new Token[0]);
    }

    private Token createToken(String value) {

        if (StringUtils.isStrictlyNumeric(value)) {
            return new Token(value, T_INT);
        }
        if(value.matches("[-+]?[0-9]*\\.?[0-9]+")) {
            return new Token(value, T_FLOAT);
        }

        if (value.startsWith("'")) {
            return new Token(value.substring(1, value.length() - 1).replace("''", "'"), T_STRING);
        }

        int type = T_IDENTIFIER;

        if (value.equals(".")) type = T_DOT;
        else if (value.equals(",")) type = T_COMMA;
        else if (value.equals("(")) type = T_OPEN_PARENTHESIS;
        else if (value.equals(")")) type = T_CLOSE_PARENTHESIS;
        else if (value.equals("=")) type = T_EQUALS;
        else if (value.equals("!")) type = T_NEGATE;
        else if (value.equals("<")) type = T_LT;
        else if (value.equals(">")) type = T_GT;
        else if (value.equals("+")) type = T_PLUS;
        else if (value.equals("-")) type = T_MINUS;
        else if (value.equals("*")) type = T_MULTIPLY;
        else if (value.equals("/")) type = T_DIVIDE;
        else type = reserved.getOrDefault(value.toUpperCase(), type);

        return new Token(value, type);
    }

    private List<String> getCatchablePatterns() {
        return Arrays.asList("[a-zA-Z_][a-zA-Z0-9_]*\\:[a-zA-Z_][a-zA-Z0-9_]*(?:\\\\[a-zA-Z_][a-zA-Z0-9_]*)*", // aliased name
                "[a-zA-Z_\\\\][a-zA-Z0-9_]*(?:\\\\[a-zA-Z_][a-zA-Z0-9_]*)*", // identifier or qualified name
                "(?:[0-9]+(?:[\\.][0-9]+)*)(?:e[+-]?[0-9]+)?", // numbers
                "'(?:[^']|'')*'", // quoted strings
                "\\?[0-9]*|:[a-zA-Z_][a-zA-Z0-9_]*", // parameters
                "[\\.\\<\\>\\=\\,\\(\\)\\+\\-\\*\\/\\!]?" // tokens
        );
    }

    private List<String> getNonCatchablePatterns() {
        return Arrays.asList("\\s+", "--.*", "(.)");
    }

    static {
        // init reserved and token names
        Field[] fields = Lexer.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("T_")) {
                try {
                    reserved.put(f.getName().replace("T_", ""), (Integer) f.get(Lexer.class));
                    tokenNames.put((Integer) f.get(Lexer.class), f.getName().replace("T_", ""));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
