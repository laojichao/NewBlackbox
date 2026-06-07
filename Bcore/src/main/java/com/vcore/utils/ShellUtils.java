package com.vcore.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility class for executing shell commands on Android devices. Supports both
 * root ({@code su}) and non-root ({@code sh}) command execution with optional
 * result message capture.
 * <p>
 * Based on Trinea's shell utility implementation.
 * </p>
 */
public class ShellUtils {
    /** Shell command to start a root shell. */
    public static final String COMMAND_SU = "su";
    /** Shell command to start a non-root shell. */
    public static final String COMMAND_SH = "sh";
    /** Shell command to exit the current shell session. */
    public static final String COMMAND_EXIT = "exit\n";
    /** Line terminator for shell commands. */
    public static final String COMMAND_LINE_END = "\n";

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws AssertionError always
     */
    private ShellUtils() {
        throw new AssertionError();
    }

    /**
     * Execute shell command, default return result msg
     *
     * @param command command
     * @param isRoot  whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static void execCommand(String command, boolean isRoot) {
        execCommand(new String[]{command}, isRoot, true);
    }

    /**
     * Execute shell commands
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
     * <li>if {@link CommandResult#result} is -1, there maybe some exception.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        StringBuilder successMsg = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());

            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }

            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;

                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }

                if (successResult != null) {
                    successResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString());
    }

    /**
     * Result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to execute in
     * linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
     */
    public static class CommandResult {
        /**
         * result of command
         **/
        public final int result;
        /**
         * success message of command result
         **/
        public final String successMsg;

        /**
         * Constructs a command result with the given exit code and output message.
         *
         * @param result     the process exit code (0 for success)
         * @param successMsg the captured stdout output, or {@code null} if not requested
         */
        public CommandResult(int result, String successMsg) {
            this.result = result;
            this.successMsg = successMsg;
        }
    }
}
