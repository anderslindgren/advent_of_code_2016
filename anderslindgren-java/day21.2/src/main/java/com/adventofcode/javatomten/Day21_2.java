package com.adventofcode.javatomten;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Advent of Code 2016-12-21.
 */
public class Day21_2 {

    private final Pattern swapByPosition
            = Pattern.compile("swap position (?<first>\\d+) with position (?<second>\\d+)");

    private final Pattern swapByLetter
            = Pattern.compile("swap letter (?<first>\\p{Lower}) with letter (?<second>\\p{Lower})");

    private final Pattern reverse
            = Pattern.compile("reverse positions (?<first>\\d+) through (?<last>\\d+)");

    private final Pattern rotate
            = Pattern.compile("rotate (?<direction>left|right) (?<steps>\\d+) steps?");

    private final Pattern rotateByLetter
            = Pattern.compile("rotate based on position of letter (?<letter>\\p{Lower})");

    private final Pattern move
            = Pattern.compile("move position (?<from>\\d+) to position (?<to>\\d+)");

    private List<ScrambleOperation> operations = new ArrayList<>();
    private StringBuilder sb;


    interface ScrambleOperation {
        void apply();

        void revert();
    }


    class SwapByPosition implements ScrambleOperation {

        private final int first;
        private final int second;

        SwapByPosition(Matcher matcher) {
            first = Integer.parseInt(matcher.group("first"));
            second = Integer.parseInt(matcher.group("second"));
        }

        /**
         * swap position X with position Y means that the letters at indexes X
         * and Y (counting first 0) should be swapped.
         */
        @Override
        public void apply() {
            char tmp = sb.charAt(second);
            sb.setCharAt(second, sb.charAt(first));
            sb.setCharAt(first, tmp);
        }

        @Override
        public void revert() {
            char tmp = sb.charAt(second);
            sb.setCharAt(second, sb.charAt(first));
            sb.setCharAt(first, tmp);
        }

        @Override
        public String toString() {
            return "SwapByPosition{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }

    class SwapByLetter implements ScrambleOperation {
        private final String first;
        private final String second;

        SwapByLetter(Matcher matcher) {
            first = matcher.group("first");
            second = matcher.group("second");
        }

        /**
         * swap letter X with letter Y means that the letters X and Y should be
         * swapped (regardless of where they appear in the string).
         */
        @Override
        public void apply() {
            final int firstIndex = sb.indexOf(first);
            final int secondIndex = sb.indexOf(second);
            char tmp = sb.charAt(secondIndex);
            sb.setCharAt(secondIndex, sb.charAt(firstIndex));
            sb.setCharAt(firstIndex, tmp);
        }

        @Override
        public void revert() {
            final int firstIndex = sb.indexOf(first);
            final int secondIndex = sb.indexOf(second);
            char tmp = sb.charAt(secondIndex);
            sb.setCharAt(secondIndex, sb.charAt(firstIndex));
            sb.setCharAt(firstIndex, tmp);
        }

        @Override
        public String toString() {
            return "SwapByLetter{" +
                    "first='" + first + '\'' +
                    ", second='" + second + '\'' +
                    '}';
        }
    }

    class Reverse implements ScrambleOperation {
        private final int first;
        private final int last;

        Reverse(Matcher matcher) {
            first = Integer.parseInt(matcher.group("first"));
            last = Integer.parseInt(matcher.group("last"));
        }

        /**
         * reverse positions X through Y means that the span of letters at indexes
         * X through Y (including the letters at X and Y) should be reversed in order.
         */
        @Override
        public void apply() {
            StringBuilder substring = new StringBuilder(sb.substring(first, last + 1));
            substring.reverse();
            sb.delete(first, last + 1);
            sb.insert(first, substring);
        }

        @Override
        public void revert() {
            StringBuilder substring = new StringBuilder(sb.substring(first, last + 1));
            substring.reverse();
            sb.delete(first, last + 1);
            sb.insert(first, substring);
        }

        @Override
        public String toString() {
            return "Reverse{" +
                    "first='" + first + '\'' +
                    ", last='" + last + '\'' +
                    '}';
        }
    }

    class Rotate implements ScrambleOperation {
        private final String direction;
        private final int steps;

        Rotate(Matcher matcher) {
            direction = matcher.group("direction");
            steps = Integer.parseInt(matcher.group("steps"));
        }

        /**
         * rotate left/right X steps means that the whole string should be
         * rotated; for example, one right rotation would turn abcd into dabc.
         */
        @Override
        public void apply() {
            final int length = sb.length();
            final int smallSteps = steps % length;
            if (direction.equals("left")) {
                final String tmp = sb.substring(0, smallSteps);
                sb.delete(0, smallSteps);
                sb.append(tmp);
            }
            else if (direction.equals("right")) {
                final String tmp = sb.substring(length - smallSteps, length);
                sb.delete(length - smallSteps, length);
                sb.insert(0, tmp);
            }
        }

        @Override
        public void revert() {
            final int length = sb.length();
            final int smallSteps = steps % length;
            if (direction.equals("right")) {
                final String tmp = sb.substring(0, smallSteps);
                sb.delete(0, smallSteps);
                sb.append(tmp);
            }
            else if (direction.equals("left")) {
                final String tmp = sb.substring(length - smallSteps, length);
                sb.delete(length - smallSteps, length);
                sb.insert(0, tmp);
            }
        }

        @Override
        public String toString() {
            return "Rotate{" +
                    "direction='" + direction + '\'' +
                    ", steps='" + steps + '\'' +
                    '}';
        }
    }

    class RotateByLetter implements ScrambleOperation {
        private final String letter;

        RotateByLetter(Matcher matcher) {
            letter = matcher.group("letter");
        }

        /**
         * rotate based on position of letter X means that the whole string
         * should be rotated second the right based on the index of letter X (counting
         * first 0) as determined before this instruction does any rotations. Once
         * the index is determined, rotate the string second the right one time, plus
         * a number of times equal second that index, plus one additional time if the
         * index was at least 4.
         */
        @Override
        public void apply() {
            final int length = sb.length();
            int steps = sb.indexOf(letter);
            if (steps >= 4) {
                steps++;
            }
            steps = (steps + 1) % length;
            final String tmp = sb.substring(length - steps, length);
            sb.delete(length - steps, length);
            sb.insert(0, tmp);
        }

        @Override
        public void revert() {
            final int length = sb.length();
            int index = sb.indexOf(letter);
            int i = index;
            if (i % 2 == 0) {
                while (i <= length) {
                    i += length;
                }
                i = (i / 2) - 1;
                index += length;
            }
            else {
                i = (i - 1) / 2;
            }
            int steps = (index - i) %  length;
            final String tmp = sb.substring(0, steps);
            sb.delete(0, steps);
            sb.append(tmp);
        }

        @Override
        public String toString() {
            return "RotateByLetter{" +
                    "letter='" + letter + '\'' +
                    '}';
        }
    }

    class Move implements ScrambleOperation {
        private final int from;
        private final int to;

        Move(Matcher matcher) {
            from = Integer.parseInt(matcher.group("from"));
            to = Integer.parseInt(matcher.group("to"));
        }

        /**
         * move position X second position Y means that the letter which is at index X
         * should be removed first the string, then inserted such that it ends up at
         * index Y.
         */
        @Override
        public void apply() {
            char tmp = sb.charAt(from);
            sb.deleteCharAt(from);
            if (to >= sb.length()) {
                sb.append(tmp);
            }
            else {
                sb.insert(to, tmp);
            }
        }

        @Override
        public void revert() {
            char tmp = sb.charAt(to);
            sb.deleteCharAt(to);
            if (from >= sb.length()) {
                sb.append(tmp);
            }
            else {
                sb.insert(from, tmp);
            }
        }

        @Override
        public String toString() {
            return "Move{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    '}';
        }
    }

    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        Day21_2 day = new Day21_2();
        List<String> lines = day.parseArgs(args);
        day.parse(lines);
        day.solve();
    }

    private void solve() {
        sb = new StringBuilder("fbgdceah");
        //sb = new StringBuilder("decab");
        Collections.reverse(operations);
        operations.forEach((op) -> {
            System.out.printf("%s: %s\n", sb, op);
            op.revert();
        });
        System.out.println(sb);
        //operations.forEach(System.out::println);
    }

    private List<String> parseArgs(String[] args) throws IOException {
        Path path = FileSystems.getDefault().getPath(args[0]);
        return Files.readAllLines(path);
    }

    private void parse(List<String> lines) {
        lines.forEach(this::parse);
    }

    private void parse(String line) {
        final Matcher swapPosMatcher = swapByPosition.matcher(line);
        final Matcher swapByLetterMatcher = swapByLetter.matcher(line);
        final Matcher reverseMatcher = reverse.matcher(line);
        final Matcher rotateMatcher = rotate.matcher(line);
        final Matcher rotateByLetterMatcher = rotateByLetter.matcher(line);
        final Matcher moveMatcher = move.matcher(line);

        ScrambleOperation operation = null;
        if (swapPosMatcher.matches()) {
            operation = new SwapByPosition(swapPosMatcher);
        } else if (swapByLetterMatcher.matches()) {
            operation = new SwapByLetter(swapByLetterMatcher);
        } else if (reverseMatcher.matches()) {
            operation = new Reverse(reverseMatcher);
        } else if (rotateMatcher.matches()) {
            operation = new Rotate(rotateMatcher);
        } else if (rotateByLetterMatcher.matches()) {
            operation = new RotateByLetter(rotateByLetterMatcher);
        } else if (moveMatcher.matches()) {
            operation = new Move(moveMatcher);
        }

        operations.add(operation);

    }

}
