package mini_project;

// importing dependancies
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;

public class calculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, String> variables = new HashMap<>();
        while (true) {

            // replace all -+ combinations with appropriate signs and removing all extra
            // white spaces
            String line = scanner.nextLine().trim().replaceAll("((--)*-)", "-").replaceAll("(\\+|(--))+", "+");
            if (isDeclaration(line, variables))
                continue;
            if (!line.isEmpty())
                switch (line) {
                    case "/exit":
                        System.out.println("Bye!");
                        return;
                    case "/help":
                        System.out.println("The program calculates the sum of numbers using addition and subtraction");
                        break;
                    default:
                        if (!getPostfix(line).equals("error"))
                            getResult(line, variables);
                }
        }
    }

    static boolean isDeclaration(String line, HashMap<String, String> variables) {
        String[] tokens;
        if (line.contains("=")) {
            tokens = line.replaceAll("\\s*=\\s*", " = ").split("\\s+");
            if (!tokens[0].matches("([a-z]|[A-Z])+")) {
                System.out.println("Invalid identifier");
            } else if (tokens[2].matches("([a-z]|[A-Z])+") && !variables.containsKey(tokens[2]))
                System.out.println("Unknown variable");
            else if (tokens[2].matches("([a-z]|[A-Z])+"))
                variables.put(tokens[0], variables.get(tokens[2]));
            else if (!tokens[2].matches("-?\\d+") || tokens.length != 3) {
                System.out.println("Invalid assignment");
            } else
                variables.put(tokens[0], tokens[2]);
            return true;
        } else
            return false;
    }

    static String getPostfix(String line) {
        Pattern error = Pattern.compile(".*[*/^]{2,}.*|.*[+\\-][*/^].*");
        if (line.matches(error.pattern())) {
            System.out.println("Invalid expression");
            return "error";
        }
        Stack<String> stack = new Stack<>();
        String[] tokens = line.replaceAll("\\(", "( ").replaceAll("\\)", " )").replaceAll("\\+", " + ")
                .replaceAll("-", " + 0 - ").replaceAll("\\*", " * ").replaceAll("\\^", " ^ ").replaceAll("/", " / ")
                .split("\\s+");
        String postfix = "";

        for (String token : tokens) {

            if (token.matches("[a-zA-Z0-9]+"))
                postfix += token + " ";
            else if (token.equals(")")) {
                if (!stack.contains("("))
                    return "Invalid expression";
                do {
                    postfix += stack.pop() + " ";
                } while (!stack.peek().equals("("));

                stack.pop();

            } else if (stack.isEmpty() || stack.peek().equals("(") || token.equals("("))
                stack.push(token);
            else if (getPriority(token) > getPriority(stack.peek()))
                stack.push(token);
            else if (getPriority(token) <= getPriority(stack.peek())) {

                while (!(stack.isEmpty() || getPriority(token) > getPriority(stack.peek()) || stack.peek().equals("(")))
                    postfix += stack.pop() + " ";

                stack.push(token);
            }

        }
        while (!stack.isEmpty()) {
            if (stack.contains("(") && !stack.contains(")"))
                return "Invalid expression";
            else
                postfix += stack.pop() + " ";
        }

        return postfix.replaceAll("[()]", "");
    }

    static int getPriority(String token) {
        if (token.matches("\\^"))
            return 2;
        else if (token.matches("[*/]"))
            return 1;
        else if (token.matches("[+\\-]"))
            return 0;
        else
            return -1;
    }

    static void getResult(String line, HashMap<String, String> variables) {
        Pattern variable = Pattern.compile("[a-zA-Z]+");
        Pattern operator = Pattern.compile("[+\\-*/^]");
        Stack<BigInteger> stack = new Stack<>();
        try {
            String[] tokens = getPostfix(line).split("\\s+");
            for (String token : tokens) {
                if (token.matches(variable.pattern()) && variables.get(token) == null)
                    throw new NumberFormatException();
                if (token.matches(variable.pattern()))
                    stack.push(new BigInteger(variables.get(token)));
                else if (token.matches("\\d+"))
                    stack.push(new BigInteger(token));
                else if (token.matches(operator.pattern()) && tokens.length > 1) {
                    BigInteger second = stack.pop();
                    BigInteger first = stack.pop();
                    switch (token) {
                        case "+":
                            stack.push(first.add(second));
                            break;
                        case "-":
                            stack.push(first.subtract(second));
                            break;
                        case "*":
                            stack.push(first.multiply(second));
                            break;
                        case "/":
                            stack.push(first.divide(second));
                            break;
                        case "^":
                            stack.push(first.pow(second.intValue()));
                            break;
                    }
                } else
                    System.out.println(tokens[0]);
            }
            if (!stack.isEmpty())
                System.out.println(stack.peek());
        } catch (NumberFormatException e) {
            if (line.matches("([a-z]|[A-Z])+") && !variables.containsKey(line))
                System.out.println("Unknown variable");
            else if (line.matches("/.+"))
                System.out.println("Unknown command");
            else
                System.out.println("Invalid expression");
        }
    }
}