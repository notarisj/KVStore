package org.notaris;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.notaris.tree.trie.Trie;
import org.notaris.tree.trie.TrieNode;
import org.notaris.tree.trie.TrieUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientHandlerUtils {

    private static final Logger logger = LogManager.getLogger(ClientHandlerUtils.class);

    protected static String handlePut(String rightPart, Trie mainDB) {
        ServerUtils.saveKey(rightPart, mainDB);
        logger.info("KEY INSERTED");
        return "0\nKEY INSERTED";
    }

    protected static String handleGet(String rightPart, Trie mainDB) {
        TrieNode keyValue = TrieUtils.find(rightPart, mainDB);
        if (keyValue != null) {
            Trie keyToFind = (Trie) keyValue.getValue();
            return "0\n" + rightPart + " -> " + TrieUtils.getKey(keyToFind);
        } else {
            return "1\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleQuery(String rightPart, Trie mainDB) {
        Object key = ServerUtils.findKey(rightPart, mainDB);
        if (key != null && key instanceof Trie) {
            return "0\n" + rightPart + " -> " + TrieUtils.getKey((Trie) key);
        } else if (key != null && key instanceof String) {
            return "0\n" + rightPart + " -> " + key;
        } else {
            return "1\nKEY WAS NOT FOUND";
        }
    }

    protected static String handleIndex(String rightPart, Trie mainDB) {
        Set<String> indexFile = IO.readFile(rightPart);
        for (String _key : indexFile) {
            ServerUtils.saveKey(_key, mainDB);
            logger.info("IMPORTED KEY" + _key);
        }
        logger.info("FILE INDEXED");
        return "0\nFILE INDEXED";
    }

    protected static String handleDelete(String rightPart, Trie mainDB) {
        boolean keyDeleted = TrieUtils.delete(rightPart, mainDB);
        if (keyDeleted) {
            return "0\nKEY DELETED SUCCESSFULLY";
        } else {
            return "1\nTHERE WAS AN ERROR DELETING KEY: " + rightPart;
        }
    }

    protected static String handleCompute(String rightPart, Trie mainDB) {
        String strExpression = rightPart.substring(0, rightPart.indexOf("WHERE ") - 1);
        String[] queryParameters = rightPart.substring(rightPart.indexOf("WHERE ") + 6, rightPart.length()).split("AND");
        HashMap<String, Double> parameters = new HashMap<>();

        for (String parameter : queryParameters) {
            String[] queryArray = parameter.split("=");
            String variable = queryArray[0].trim();
            String query = queryArray[1].replace("QUERY ", "").trim();

            String resolvedValue = handleQuery(query, mainDB);
            String parsedResolvedValue = resolvedValue.substring(resolvedValue.indexOf(" -> ") + 4, resolvedValue.length());
            parameters.put(variable, Double.valueOf(parsedResolvedValue));
        }

        Expression expression = new ExpressionBuilder(strExpression)
                .variables(parameters.keySet())
                .build();

        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            expression.setVariable(entry.getKey(), entry.getValue());
        }

        return "0\n" + expression.evaluate();
    }

}
