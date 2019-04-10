package telran.java23.serviceprivder.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Getter
@Setter
@NoArgsConstructor

public class DayOfWeek implements Cloneable{
    String name;
    Boolean isAvailable;
    //LocalTime
    String startDay;
    String endDay;
    Integer breakeInMinute;
    String breakStart;
    //LocalTime
    LinkedHashMap<String, Boolean> timeRecords;




    public DayOfWeek(String name, Boolean isAvailable, String startDay, String endDay, Integer breakeInMinute, String breakStart) {
        this.name = name;
        this.isAvailable = isAvailable;
        this.startDay = startDay;
        this.endDay = endDay;
        this.breakeInMinute = breakeInMinute;
        this.breakStart = breakStart;
        this.timeRecords = new LinkedHashMap<>();
        if(startDay!=null & endDay!=null){
            mapa();
        }

    }



    public void mapa() {
        DateTimeFormatter pattern=DateTimeFormatter.ofPattern("HH:mm");
        LocalTime endTime = LocalTime.parse(endDay,pattern);
        LocalTime startTime = LocalTime.parse(startDay,pattern);

        int lenthOfWorkDay = endTime.getHour()- startTime.getHour();
        LocalTime[] times=new LocalTime[lenthOfWorkDay+1];
        times[0]=startTime;
        for(int a=0;a<lenthOfWorkDay;a++){
            times[a+1]=times[a].plusMinutes(60);

        }
        for(int b=0;b<times.length-1;b++){
            if(times[b].toString().equals(breakStart)){
                timeRecords.put(times[b].toString(),false);
            }else {
                timeRecords.put(times[b].toString(), true);
            }
        }
        System.out.println(Arrays.asList(timeRecords));


    }

}



