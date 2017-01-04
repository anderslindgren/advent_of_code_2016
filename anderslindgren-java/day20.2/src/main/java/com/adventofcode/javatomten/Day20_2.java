package com.adventofcode.javatomten;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Advent of Code 2016-12-20.
 */
public class Day20_2 {

    private List<Range> blacklist = new ArrayList<>();

    class Range implements Comparable {
        long low;
        long high;

        Range(long low, long high) {
            this.low = low;
            this.high = high;
        }

        boolean isWithinRange(long value) {
            return low < value && value < high;
        }

        void extendRange(Range range) {
            if (range.low < this.low) {
                this.low = range.low;
            }
            if (range.high > this.high) {
                this.high = range.high;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Range range = (Range) o;

            return low == range.low && high == range.high;
        }

        @Override
        public int hashCode() {
            int result = (int) (low ^ (low >>> 32));
            result = 31 * result + (int) (high ^ (high >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "[" + low + "-" + high + ']';
        }

        @Override
        public int compareTo(Object o) {
            return Long.valueOf(low).compareTo(((Range) o).low);
        }
    }

    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        Day20_2 day = new Day20_2();
        List<String> lines = day.parseArgs(args);
        day.parse(lines);
        day.findFirstWhite();
    }

    private List<String> parseArgs(String[] args) throws IOException {
        Path path = FileSystems.getDefault().getPath(args[0]);
        return Files.readAllLines(path);
    }

    private void parse(List<String> lines) {
        lines.forEach(this::parse);
    }

    private void parse(String line) {
        String[] range = line.split("-");
        final long low = Long.parseLong(range[0]);
        final long high = Long.parseLong(range[1]);

        Range r = new Range(low, high);
        blacklist.add(r);
    }

    private void findFirstWhite() {
        List<Range> newBlacklist = compactBlacklist(blacklist);
        newBlacklist.forEach(System.out::println);
        int whiteListed = 0;
        for (int i = 1; i < newBlacklist.size(); i++) {
            whiteListed += newBlacklist.get(i).low - newBlacklist.get(i - 1).high - 1;
        }
        System.out.printf("%2d %d\n", newBlacklist.size(), whiteListed);
    }

    private List<Range> compactBlacklist(List<Range> blacklist) {
        List<Range> result = new ArrayList<>();
        Collections.sort(blacklist);
        for (int i = 0; i < blacklist.size(); i++) {
            Range blackRange = blacklist.get(i);
            for (int j = 0; j < result.size(); j++) {
                Range resultRange = result.get(j);
                if (resultRange.isWithinRange(blackRange.low) && resultRange.isWithinRange(blackRange.high)) {
                    blackRange = null;
                    break;
                } else if (blackRange.low < resultRange.low && resultRange.isWithinRange(blackRange.high)) {
                    resultRange.extendRange(blackRange);
                    blackRange = null;
                    break;
                } else if (blackRange.high > resultRange.high && resultRange.isWithinRange(blackRange.low)) {
                    resultRange.extendRange(blackRange);
                    blackRange = null;
                    break;
                } else if (blackRange.low == resultRange.high + 1) {
                    resultRange.extendRange(blackRange);
                    blackRange = null;
                    break;
                } else if (blackRange.high == resultRange.low - 1) {
                    resultRange.extendRange(blackRange);
                    blackRange = null;
                    break;
                }
            }
            if (blackRange != null) {
                result.add(blackRange);
            }
        }
        return result;
    }

}
