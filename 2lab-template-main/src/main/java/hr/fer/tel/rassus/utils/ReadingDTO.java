package hr.fer.tel.rassus.utils;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReadingDTO implements Serializable {

    private Integer sensorId;

    private Integer no2;
    private Integer vectorTime;
    private Long scalarTime;

    public ReadingDTO() {
    }

    public ReadingDTO(Integer no2, Integer vectorTime, Long scalarTime) {
        this.no2 = no2;
        this.vectorTime = vectorTime;
        this.scalarTime = scalarTime;
    }

    public Integer getNo2() {
        return no2;
    }

    public void setNo2(Integer no2) {
        this.no2 = no2;
    }

    public Integer getVectorTime() {
        return vectorTime;
    }

    public void setVectorTime(Integer vectorTime) {
        this.vectorTime = vectorTime;
    }

    public Long getScalarTime() {
        return scalarTime;
    }

    public void setScalarTime(Long scalarTime) {
        this.scalarTime = scalarTime;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public String toString() {
        return "Reading{" +
                "sensorId=" + sensorId +
                ", scalarTimestamp=" + scalarTime +
                ", vectorTimestamp=" + vectorTime +
                ", value=" + no2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadingDTO readingModel = (ReadingDTO) o;
        return no2 == readingModel.no2 &&
                scalarTime == readingModel.scalarTime &&
                vectorTime == readingModel.vectorTime &&
                sensorId == readingModel.sensorId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


}
