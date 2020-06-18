package org.transitclock.api.data.reporting.chartjs.pie;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to help serialize PieChart data into the correct format for Chart.js
 * Conforms to data documentation specified for Chart.js 2.x
 *
 * TODO: Should either be its own module or should use other external dependency
 *
 * @author carabalb
 *
 */
public class PieChartData {

    public PieChartData() {}

    @XmlElement(name = "datasets")
    private List<PieChartDataset> dataset = new ArrayList<>();

    @XmlElement(name = "labels")
    private List<String> labels = new ArrayList<>();

    public List<PieChartDataset> getDataset() {
        return dataset;
    }

    public void setDataset(PieChartDataset ... datasets) {
        this.dataset.clear();
        addDataset(datasets);
    }

    public void addDataset(PieChartDataset ... datasets){
        for(PieChartDataset dataset : datasets){
            this.dataset.add(dataset);
        }
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(String ... labels) {
        this.labels.clear();
        addLabels(labels);
    }

    public void addLabels(String ... labels){
        for(String label : labels){
            this.labels.add(label);
        }
    }
}
