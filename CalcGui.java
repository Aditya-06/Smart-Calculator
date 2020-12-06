package mini_project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;

// creating a GUI for the user
class GUI extends JFrame implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // Elements required to build the GUI
    private Container c;
    private JLabel title;
    private JLabel input;
    private JTextField line;
    private JTextArea operation;
    private JButton result;
    private JButton reset;
    public static HashMap<String, String> variables = new HashMap<>();

    public GUI() {

        setTitle("Smart Calculator");
        setBounds(300, 90, 900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        c = getContentPane();
        c.setLayout(null);

        title = new JLabel("Smart Calculator");
        title.setFont(new Font("Arial", Font.PLAIN, 30));
        title.setSize(300, 30);
        title.setLocation(300, 30);
        c.add(title);

        input = new JLabel("input");
        input.setFont(new Font("Arial", Font.PLAIN, 20));
        input.setSize(100, 20);
        input.setLocation(100, 100);
        c.add(input);

        line = new JTextField();
        line.setFont(new Font("Arial", Font.PLAIN, 15));
        line.setSize(190, 20);
        line.setLocation(200, 100);
        c.add(line);

        result = new JButton("result");
        result.setFont(new Font("Arial", Font.PLAIN, 15));
        result.setSize(100, 20);
        result.setLocation(150, 450);
        result.addActionListener(this);
        c.add(result);

        operation = new JTextArea();
        operation.setFont(new Font("Arial", Font.ITALIC, 32));
        operation.setSize(300, 400);
        operation.setLocation(500, 100);
        operation.setLineWrap(true);
        operation.setEditable(false);
        c.add(operation);

        reset = new JButton("Reset");
        reset.setFont(new Font("Arial", Font.PLAIN, 15));
        reset.setSize(100, 20);
        reset.setLocation(270, 450);
        reset.addActionListener(this);
        c.add(reset);

        setVisible(true);

    }

    // handle actions
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == result) {

            // if user wants result, we execute the caclResult() function
            operation.setText(calcResult(line.getText()));
        } else if (e.getSource() == reset) {

            // clear input text field
            line.setText("");
        }
    }

    public static String calcResult(String userInput) {

        // HashMap<String, String> variables = new HashMap<>();
        while (true) {

            // replace all -+ combinations with appropriate signs and removing all extra
            // white spaces
            String line = userInput.trim().replaceAll("((--)*-)", "-").replaceAll("(\\+|(--))+", "+");

            // check if user wants to declare a variable or not
            if (isDeclaration(line, variables))
                return "Variable has Been Stored!";
            if (!line.isEmpty())
                switch (line) {
                    case "/exit":
                        return "Bye!";
                    case "/help":
                        return "The program calculates the sum of numbers using addition and subtraction";
                    default:
                        if (!getPostfix(line).equals("error"))
                            return getResult(line, variables);
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

    static String getResult(String line, HashMap<String, String> variables) {
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
                    return "Result is: " + tokens[0];
            }
            if (!stack.isEmpty())
                return stack.peek().toString();
        } catch (NumberFormatException e) {
            if (line.matches("([a-z]|[A-Z])+") && !variables.containsKey(line))
                return "Unknown variable";
            else if (line.matches("/.+"))
                return "Unknown command";
            else
                return "Invalid expression";
        }
        return line;
    }

}

public class CalcGui {
    public static void main(String[] args) throws Exception {
        GUI g = new GUI();
    }
}