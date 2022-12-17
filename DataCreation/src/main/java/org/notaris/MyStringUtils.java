package org.notaris;

import java.util.Stack;

public class MyStringUtils {

    /**
     * Function to find index of closing bracket for given opening bracket.
     * @param expression The string expression to search in.
     * @param openingBracketChar Type of bracket e.g. "{", "[", "(".
     * @param openingBracket Index of the opening bracket.
     * @return The index of the closing bracket.
     */
    public static Integer findClosingBracket(String expression, int openingBracket, Character openingBracketChar) {

        int i;

        // If index given is invalid and is
        // not an opening bracket.
        if (expression.charAt(openingBracket) != openingBracketChar) {
            return null;
        }

        // Find closing bracket
        Character closingBracket = null;
        if (openingBracketChar == '[') {
            closingBracket = ']';
        } else if (openingBracketChar == '{') {
            closingBracket = '}';
        } else if (openingBracketChar == '(') {
            closingBracket = ')';
        }

        // Stack to store opening brackets.
        Stack<Integer> st = new Stack<>();

        // Traverse through string starting from given index.
        for (i = openingBracket; i < expression.length(); i++) {

            // If current character is an opening bracket push it in stack.
            if (expression.charAt(i) == openingBracketChar) {
                st.push((int) expression.charAt(i));
            }
            // If current character is a closing bracket, pop from stack.
            // If stack is empty, then this closing bracket is required bracket.
            else if (expression.charAt(i) == closingBracket) {
                st.pop();
                if (st.empty()) {
                    return i;
                }
            }
        }

        // If no matching closing bracket is found.
        return null;
    }

}
