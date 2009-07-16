package com.consol.citrus.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

public class InputBean extends AbstractTestAction {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(InputBean.class);

    private String message = "Press return key to continue...";

    private String variable = "userinput";

    private String validAnswers;

    @Override
    public void execute(TestContext context) throws TestSuiteException {

        String input = null;

        if (context.getVariables().containsKey(variable)) {
            input = context.getVariable(variable);
            log.info("Variable " + variable + " is already set (='" + input + "'). Skip waiting for user input");

            return;
        }

        String display;
        if (validAnswers != null && validAnswers.length() > 0) {
            display = message + " (" + validAnswers + ")";
        } else {
            display = message;
        }


        try {
            do {
                log.info(display);

                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                input = stdin.readLine();
            } while (validAnswers != null && !checkAnswer(input));
        } catch (IOException e) {
            throw new TestSuiteException(e);
        }

        context.setVariable(variable, input.trim());
    }

    private boolean checkAnswer(String input) {
        StringTokenizer tok = new StringTokenizer(validAnswers, "/");

        while (tok.hasMoreTokens()) {
            if (tok.nextElement().toString().trim().toLowerCase().equals(input.trim().toLowerCase()))
                return true;
        }

        log.info("User input is not valid");

        return false;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * @param validAnswers the validAnswers to set
     */
    public void setValidAnswers(String validAnswers) {
        this.validAnswers = validAnswers;
    }

}
