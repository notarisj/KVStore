package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.StringUtils;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandlerUtils {

    protected static String handlePut(String rightPart, Trie mainDB) {
        if (ServerUtils.saveKey(rightPart, mainDB) != null) {
            return SCConstants.RESPONSE_OK + "\nKEY INSERTED";
        } else {
            return SCConstants.RESPONSE_BAD + "\nTHERE WAS AN ERROR IN THE FORMAT OF THE KEY.";
        }
    }

    protected static String handleGet(String rightPart, Trie mainDB) {
        if (StringUtils.equals(rightPart, "*")) {
            StringBuilder builder = new StringBuilder();
            HashMap<String, TrieNode> children = new HashMap<>();
            TrieUtils.findChildren(mainDB.getRoot(), builder, children, true);
            StringBuilder sb = new StringBuilder();
            for (String key : children.keySet()) {
                sb.append(key).append("\n");
            }
            String keys = sb.toString();
            return SCConstants.RESPONSE_OK + "\n" + keys;
        }
        TrieNode keyValue = TrieUtils.find(rightPart, mainDB);
        if (keyValue != null) {
            Trie keyToFind = (Trie) keyValue.getValue();
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + TrieUtils.getKey(keyToFind);
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleQuery(String rightPart, Trie mainDB) {
        Object key = ServerUtils.findKey(rightPart, mainDB);
        if (key instanceof Trie) {
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + TrieUtils.getKey((Trie) key);
        } else if (key instanceof String) {
            return SCConstants.RESPONSE_OK + "\n" + rightPart + " -> " + key;
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleDelete(String rightPart, Trie mainDB) {
        boolean keyDeleted = TrieUtils.delete(rightPart, mainDB);
        if (keyDeleted) {
            return SCConstants.RESPONSE_OK + "\nKEY DELETED SUCCESSFULLY";
        } else {
            return SCConstants.RESPONSE_WARN + "\nKEY DOES NOT EXISTS OR IS NOT A TOP LEVEL KEY: " + rightPart;
        }
    }

    protected static String handleCompute(String rightPart, Trie mainDB) {

        rightPart = handleCase(rightPart, "where", "WHERE");
        rightPart = handleCase(rightPart, "query", "QUERY");

        try {
            String strExpression = rightPart.substring(0, rightPart.indexOf("WHERE ") - 1);
            String[] queryParameters = rightPart.substring(rightPart.indexOf("WHERE ") + 6).split("AND");
            HashMap<String, Double> parameters = new HashMap<>();

            for (String parameter : queryParameters) {
                String[] queryArray = parameter.split("=");
                String variable = queryArray[0].trim();
                String query = queryArray[1].replace("QUERY ", "").trim();

                String resolvedValue = handleQuery(query, mainDB);
                String parsedResolvedValue = resolvedValue.substring(resolvedValue.indexOf(" -> ") + 4);
                parameters.put(variable, Double.valueOf(parsedResolvedValue));
            }

            Expression expression = new ExpressionBuilder(strExpression)
                    .variables(parameters.keySet())
                    .build();

            for (Map.Entry<String, Double> entry : parameters.entrySet()) {
                expression.setVariable(entry.getKey(), entry.getValue());
            }

            return SCConstants.RESPONSE_OK + "\n" + expression.evaluate();
        } catch (NumberFormatException e3) { // Double.valueOf(parsedResolvedValue)
            return SCConstants.RESPONSE_WARN + "\nPROVIDED VARIABLES ARE NOT NUMERIC";
        } catch (IllegalArgumentException e1) { // new ExpressionBuilder(strExpression)
            return SCConstants.RESPONSE_WARN + "\nMATH EXPRESSION CANNOT BE EMPTY";
        } catch (IndexOutOfBoundsException e2) { // substrings
            return SCConstants.RESPONSE_WARN + "\nTHERE WAS AN ERROR IN THE SYNTAX OF THE QUERY";
        }
    }

    private static String handleCase(String str, String search, String replacement) {
        Pattern pattern = Pattern.compile(search);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(replacement);
    }

}
