/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SqlScript supporting PostgreSQL specific syntax.
 */
public class PostgreSQLSqlScript extends SqlScript {
    /**
     * Matches $$, $BODY$, $xyz123$, ...
     */
    /*private -> for testing*/
    static final String DOLLAR_QUOTE_REGEX = "\\$[A-Za-z0-9_]*\\$.*";

    /**
     * Creates a new sql script from this source with these placeholders to replace.
     *
     * @param sqlScriptSource     The sql script as a text block with all placeholders still present.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public PostgreSQLSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        super(sqlScriptSource, placeholderReplacer);
    }

    @Override
    protected String changeDelimiterIfNecessary(String statement, String line, String delimiter) {
        return DEFAULT_STATEMENT_DELIMITER;
    }

    /**
     * Checks whether the statement we have assembled so far ends with an open multi-line string literal (which will be
     * continued on the next line).
     *
     * @param statement The current statement, assembled from the lines we have parsed so far. May not yet be complete.
     * @return {@code true} if the statement is unfinished and the end is currently in the middle of a multi-line string
     *         literal. {@code false} if not.
     */
    @Override
    protected boolean endsWithOpenMultilineStringLiteral(String statement) {
        //Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals
        String[] tokens = StringUtils.tokenizeToStringArray(statement, " ;=|(),");

        List<Set<TokenType>> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        boolean insideQuoteStringLiteral = false;
        boolean insideDollarStringLiteral = false;

        for (Set<TokenType> delimitingToken : delimitingTokens) {
            if (!insideDollarStringLiteral && !insideQuoteStringLiteral && delimitingToken.contains(TokenType.QUOTE_OPEN)) {
                insideQuoteStringLiteral = true;
                continue;
            }
            if (insideQuoteStringLiteral && delimitingToken.contains(TokenType.QUOTE_CLOSE)) {
                insideQuoteStringLiteral = false;
                continue;
            }
            if (!insideDollarStringLiteral && !insideQuoteStringLiteral && delimitingToken.contains(TokenType.DOLLAR_OPEN)) {
                insideDollarStringLiteral = true;
                continue;
            }
            if (insideDollarStringLiteral && delimitingToken.contains(TokenType.DOLLAR_CLOSE)) {
                insideDollarStringLiteral = false;
            }
        }

        return insideQuoteStringLiteral || insideDollarStringLiteral;
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     *         impact on string delimiting are discarded.
     */
    private List<Set<TokenType>> extractStringLiteralDelimitingTokens(String[] tokens) {
        String dollarQuote = null;

        List<Set<TokenType>> delimitingTokens = new ArrayList<Set<TokenType>>();
        for (String token : tokens) {
            //Remove escaped quotes as they do not form a string literal delimiter
            String cleanToken = StringUtils.replace(token, "''", "");

            Set<TokenType> tokenTypes = new HashSet<TokenType>();

            if (cleanToken.startsWith("'")) {
                if ((cleanToken.length() > 1) && cleanToken.endsWith("'")) {
                    // Ignore. ' string literal is opened and closed inside the same token.
                    continue;
                }
                tokenTypes.add(TokenType.QUOTE_OPEN);
            }

            if (cleanToken.endsWith("'")) {
                tokenTypes.add(TokenType.QUOTE_CLOSE);
            }

            if ((dollarQuote == null) && cleanToken.matches(DOLLAR_QUOTE_REGEX)) {
                dollarQuote = cleanToken.substring(0, cleanToken.substring(1).indexOf("$") + 2);
                if ((cleanToken.length() > dollarQuote.length()) && cleanToken.endsWith(dollarQuote)) {
                    // Ignore. $$ string literal is opened and closed inside the same token.
                    dollarQuote = null;
                    continue;
                }
                tokenTypes.add(TokenType.DOLLAR_OPEN);
            }

            if ((dollarQuote != null) && !cleanToken.startsWith(dollarQuote) && cleanToken.endsWith(dollarQuote)) {
                tokenTypes.add(TokenType.DOLLAR_CLOSE);
                dollarQuote = null;
            }

            if (!tokenTypes.isEmpty()) {
                delimitingTokens.add(tokenTypes);
            }
        }

        return delimitingTokens;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private static enum TokenType {
        /**
         * Token opens ' string literal
         */
        QUOTE_OPEN,

        /**
         * Token closes ' string literal
         */
        QUOTE_CLOSE,

        /**
         * Token opens $$ string literal
         */
        DOLLAR_OPEN,

        /**
         * Token closes $$ string literal
         */
        DOLLAR_CLOSE
    }
}
