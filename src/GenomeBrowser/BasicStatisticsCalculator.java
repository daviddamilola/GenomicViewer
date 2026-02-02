package GenomeBrowser;

import java.io.*;
import java.util.*;

public class BasicStatisticsCalculator {

    public AssemblyMetrics calculateAssemblyMetrics(String fastaFilePath) {
        List<Integer> contigLengths = new ArrayList<>();
        int currentLength = 0;
        int totalLength = 0;
        int largestContig = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fastaFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (currentLength > 0) {
                        contigLengths.add(currentLength);
                        totalLength += currentLength;
                        if (currentLength > largestContig) {
                            largestContig = currentLength;
                        }
                        currentLength = 0;
                    }
                } else {
                    currentLength += line.trim().length();
                }
            }
            if (currentLength > 0) {
                contigLengths.add(currentLength);
                totalLength += currentLength;
                if (currentLength > largestContig) {
                    largestContig = currentLength;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new AssemblyMetrics(0, 0, 0, 0, 0);
        }

        int totalContigs = contigLengths.size();
        double averageLength = totalContigs > 0 ? (double) totalLength / totalContigs : 0;
        int n50 = calculateN50(contigLengths, totalLength);

        return new AssemblyMetrics(totalContigs, totalLength, largestContig, averageLength, n50);
    }

    private int calculateN50(List<Integer> contigLengths, int totalLength) {
        if (contigLengths.isEmpty()) return 0;

        contigLengths.sort(Collections.reverseOrder());
        int halfLength = totalLength / 2;
        int cumulativeLength = 0;

        for (int length : contigLengths) {
            cumulativeLength += length;
            if (cumulativeLength >= halfLength) {
                return length;
            }
        }
        return 0;
    }
}

// Helper class to store metrics
class AssemblyMetrics {
    int totalContigs;
    int totalLength;
    int largestContig;
    double averageLength;
    int n50;

    public AssemblyMetrics(int totalContigs, int totalLength, int largestContig, double averageLength, int n50) {
        this.totalContigs = totalContigs;
        this.totalLength = totalLength;
        this.largestContig = largestContig;
        this.averageLength = averageLength;
        this.n50 = n50;
    }
}


