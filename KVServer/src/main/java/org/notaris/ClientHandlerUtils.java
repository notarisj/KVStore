package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.util.HashMap;
import java.util.Map;

public class ClientHandlerUtils {

    protected static String handlePut(String rightPart, Trie mainDB) {
        if (ServerUtils.saveKey(rightPart, mainDB) != null) {
            return SCConstants.RESPONSE_OK + "\nKEY INSERTED";
        } else {
            return SCConstants.RESPONSE_BAD + "\nTHERE WAS AN ERROR IN THE FORMAT OF THE KEY.";
        }
    }

    protected static String handleGet(String rightPart, Trie mainDB) {
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

}
