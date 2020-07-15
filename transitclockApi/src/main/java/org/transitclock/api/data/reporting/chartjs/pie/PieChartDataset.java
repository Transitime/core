package org.transitclock.api.data.reporting.chartjs.pie;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
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
public class PieChartDataset {

    @XmlElement(name = "data")
    private List<BigDecimal> data = new ArrayList<>();

    public PieChartDataset(){}

    public List<BigDecimal> getData() {
        return data;
    }

    public void setData(Integer ... integers) {
        this.data.clear();
        addData(integers);
    }

    public void addData(Integer ... integers){
        for(Integer integer : integers){
            this.data.add(new BigDecimal(integer));
        }
    }

    public void setData(BigDecimal ... bigDecimals) {
        this.data.clear();
        addData(bigDecimals);
    }

    public void addData(BigDecimal ... bigDecimals){
        for(BigDecimal bigDecimal : bigDecimals){
            this.data.add(bigDecimal);
        }
    }

    public void setData(Long ... longs) {
        this.data.clear();
        addData(longs);
    }

    public void addData(Long ... longs){
        for(Long l : longs){
            new BigDecimal(l);
        }
    }
}
