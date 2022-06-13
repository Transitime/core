package org.transitclock.reporting.service.runTime.prescriptive.timebands.kmeans;

import org.transitclock.reporting.service.runTime.prescriptive.timebands.model.RunTimeData;

import java.util.*;
import java.util.stream.Collectors;

public class EmpiricalCombining {
    public static Map<Centroid, List<RunTimeData>> run(Map<Centroid,
                                                       List<RunTimeData>> clusters,
                                                       Double minLength,
                                                       Double maxLength) {
        //TODO: what rules are we following for combining these clusters?
        //TODO: combine clusters where the min actual time of the first cluster and the max actual time of the second cluster are within 8% of each other

        //sort by the sequence numbers inside the runtimedata lists
        Comparator<Map.Entry<Centroid, List<RunTimeData>>> comparator = new RunTimeDataListSequenceComparator();
        LinkedHashMap<Centroid, List<RunTimeData>> sorted =
                clusters.entrySet().stream().sorted(comparator)
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

        //set the mean value and length for each centroid
        setCentroidMeanAndLength(sorted);

        int num = 0;
        while (num != sorted.size() && sorted.size() > 1) {
            num = combineClusters(sorted, minLength);
            setCentroidMeanAndLength(sorted);
        }

        num = 0;
        while (num != sorted.size() && sorted.size() > 0) {
            num = splitLongClusters(sorted, maxLength);
        }

        //re-sort clusters since the splitLongClusters probably added some out of order to the end
        sorted = sorted.entrySet().stream().sorted(comparator)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));



        return sorted;
    }

    private static void setCentroidMeanAndLength(LinkedHashMap<Centroid, List<RunTimeData>> sorted) {
        sorted.forEach((key, value) -> {
            setCentroidMeanAndLength(key, value);
        });
    }

    private static void setCentroidMeanAndLength(Centroid key, List<RunTimeData> value) {
        Double actualSum = value.stream().map(x -> x.getActualRuntime()).reduce(0.0, (a, b) -> a + b);
        key.setActualMeanValue(actualSum / value.size());

        Double expectedSum = value.stream().map(x -> x.getExpectedRuntime()).reduce(0.0, (a, b) -> a + b);
        key.setExpectedMeanValue(expectedSum / value.size());

        Integer firstStartTime = value.get(0).getScheduledStartTime();
        Integer lastStartTime = value.get(value.size() - 1).getScheduledStartTime();
        Double startTimeDiff = lastStartTime * 1.0 - firstStartTime;
        Double length = startTimeDiff;// + value.get(value.size() - 1).getActualRuntime();
        key.setLength(length);
    }

    private static int combineClusters(LinkedHashMap<Centroid, List<RunTimeData>> sorted, Double minPartitionLength) {
        List<Centroid> keyList = sorted.keySet().stream().collect(Collectors.toList());

        int numberBigEnoughPartitions = 0;
        if (keyList.size() <= 1) return numberBigEnoughPartitions; //only one (or none) partition so nothing to combine

        for (int i = 0; i < keyList.size(); i++) {
            if (keyList.get(i).getLength() >= minPartitionLength) {
                numberBigEnoughPartitions++;
                continue;//already big enough partition
            }

            Centroid currentCentroid = keyList.get(i);

            if (sorted.get(currentCentroid) == null) {
                continue;//list is empty so skip it
            }

            Double prevMean;
            Double nextMean;
            if (i == 0) {
                //must combine with index = i + 1
                Centroid nextCentroid = keyList.get(i + 1);
                List<RunTimeData> currentList = sorted.get(currentCentroid);
                List<RunTimeData> nextList = sorted.get(nextCentroid);
                if (nextList != null) {
                    currentList.addAll(nextList);
                    sorted.replace(nextCentroid, currentList);
                    sorted.replace(currentCentroid, null);
                }
            }
            else if (i == keyList.size() - 1) {
                //must combine with index = i - 1
                Centroid previousCentroid = keyList.get(i - 1);
                List<RunTimeData> prevList = sorted.get(previousCentroid);
                List<RunTimeData> currentList = sorted.get(currentCentroid);
                if (prevList != null) {
                    prevList.addAll(currentList);
                    sorted.replace(currentCentroid, prevList);
                    sorted.replace(previousCentroid, null);
                }
            }
            else {
                Double currentMean = keyList.get(i).getActualMeanValue();
                prevMean = keyList.get(i - 1).getActualMeanValue();
                nextMean = keyList.get(i + 1).getActualMeanValue();
                Double prevDiff = Math.abs(currentMean - prevMean);
                Double nextDiff = Math.abs(currentMean - nextMean);

                if (prevDiff < nextDiff) {//must combine with index = i - 1
                    Centroid previousCentroid = keyList.get(i - 1);
                    List<RunTimeData> prevList = sorted.get(previousCentroid);
                    List<RunTimeData> currentList = sorted.get(currentCentroid);
                    if (prevList != null) {
                        prevList.addAll(currentList);
                        sorted.replace(currentCentroid, prevList);
                        sorted.replace(previousCentroid, null);
                    }
                }
                else {//must combine with index i + 1;
                    Centroid nextCentroid = keyList.get(i + 1);
                    List<RunTimeData> currentList = sorted.get(currentCentroid);
                    List<RunTimeData> nextList = sorted.get(nextCentroid);
                    if (nextList != null) {
                        currentList.addAll(nextList);
                        sorted.replace(nextCentroid, currentList);
                        sorted.replace(currentCentroid, null);
                    }
                }
            }
        }
        //remove empty centroids
        sorted.entrySet().removeIf(e -> e.getValue() == null);
        return numberBigEnoughPartitions;
    }

    private static int splitLongClusters(LinkedHashMap<Centroid, List<RunTimeData>> sorted, Double maxPartitionLength) {
        List<Centroid> keyList = sorted.keySet().stream().collect(Collectors.toList());
        int numberSmallEnoughPartitions = 0;
        for (int i = 0; i < keyList.size(); i++) {
            Centroid currentCentroid = keyList.get(i);
            if (currentCentroid.getLength() <= maxPartitionLength) {
                numberSmallEnoughPartitions++;
                continue;//already small enough partition
            }

            List<RunTimeData> currentData = sorted.get(currentCentroid);
            List<List<RunTimeData>> newLists = splitRunTimeData(currentData);
            if (newLists.size() == 2) {

                Centroid newCentroid = new Centroid(-1.0);//just put negative 1 since it isn't kmeans
                setCentroidMeanAndLength(currentCentroid, newLists.get(0));
                setCentroidMeanAndLength(newCentroid, newLists.get(1));

                sorted.replace(currentCentroid, newLists.get(0));
                sorted.put(newCentroid, newLists.get(1));
            }
        }
        return numberSmallEnoughPartitions;
    }

    private static List<List<RunTimeData>> splitRunTimeData(List<RunTimeData> source) {
        List<Integer> spacings = new ArrayList<>();
        List<List<RunTimeData>> newLists = new ArrayList<>();
        if (source.size() < 2) return newLists;//can't split because there are less than two items in source list

        for (int i = 0; i < source.size() - 1; i++) {
            spacings.add(Math.abs(source.get(i+1).getScheduledStartTime() - source.get(i).getScheduledStartTime()));
        }

        int max = spacings.stream().mapToInt(v -> v).max().getAsInt();

        int secondStartIndex = 0;
        for(int i = 0; i < spacings.size(); i++) {
            if (max == spacings.get(i)) {
                secondStartIndex = i + 1;
                break;
            }
        }

        newLists.add(new ArrayList<>(source.subList(0, secondStartIndex)));
        newLists.add(new ArrayList<>(source.subList(secondStartIndex, source.size())));
        return newLists;
    }


    private static void combineCentroidNear(LinkedHashMap<Centroid, List<RunTimeData>> sorted) {
        List<Centroid> keyList = sorted.keySet().stream().collect(Collectors.toList());
        for (int i = 0; i < keyList.size() - 1; i++) {
            Centroid centroidA = keyList.get(i);
            Centroid centroidB = keyList.get(i+1);
            List<RunTimeData> combinedList = combineOnDifferential(sorted.get(centroidA), sorted.get(centroidB));
            if (combinedList != null) {
                //combined
                sorted.replace(centroidB, combinedList);
                sorted.put(centroidA, null);
            }
        }

        //remove empty centroids
        sorted.entrySet().removeIf(e -> e.getValue() == null);
    }

    private static List<RunTimeData> combineOnDifferential(List<RunTimeData> listA, List<RunTimeData> listB) {
        if (listA == null || listB == null) return null;
        if (listA.size() == 0 || listB.size() == 0) {
            listA.addAll(listB);
            return listA;//just return the combined list
        }


        RunTimeData first = listA.get(0);
        RunTimeData last = listB.get(listB.size() - 1);
        Double allowedDifferentialA = first.getActualRuntime() * 0.08;
        Double allowedDifferentialB = last.getActualRuntime() * 0.08;
        Double allowedDifferential = Math.min(allowedDifferentialA, allowedDifferentialB);
        boolean combine = Math.abs(first.getActualRuntime() - last.getActualRuntime()) <= allowedDifferential;
        if (combine) {
            listA.addAll(listB);
            return listA;//return the combined list
        }
        else return null;
    }

    private static void combineCentroidsWithSameValue(LinkedHashMap<Centroid, List<RunTimeData>> sorted) {
        List<Centroid> keyList = sorted.keySet().stream().collect(Collectors.toList());
        for (int i = 0; i < keyList.size() - 1; i++) {
            Centroid centroidA = keyList.get(i);
            Centroid centroidB = keyList.get(i+1);
            List<RunTimeData> combinedList = combineBasedOnCentroidValue(centroidA, sorted.get(centroidA), centroidB, sorted.get(centroidB));
            if (combinedList != null) {
                //combined
                sorted.replace(centroidB, combinedList);
                sorted.put(centroidA, null);
            }
        }

        //remove empty centroids
        sorted.entrySet().removeIf(e -> e.getValue() == null);
    }

    private static List<RunTimeData> combineBasedOnCentroidValue(Centroid centroidA, List<RunTimeData> listA, Centroid centroidB, List<RunTimeData> listB) {
        if (listA == null || listB == null) return null;
        if (centroidA == null || centroidB == null) return null;

        if (centroidA.getCentroidValue() / 60 == centroidB.getCentroidValue() / 60) {
            listA.addAll(listB);
            return listA;
        }
        return null;
    }
}
