package hr.fer.tel.rassus.utils;

import java.util.Comparator;

public class CustomComparator implements Comparator<ReadingDTO> {

    @Override
    public int compare(ReadingDTO o1, ReadingDTO o2) {
        if(o1.getVectorTime() == o2.getVectorTime())
        {
            return o1.getScalarTime() < o2.getScalarTime() ? 1 : -1;
        }

        return o1.getVectorTime() < o2.getVectorTime() ? 1 : -1;
    }
}
