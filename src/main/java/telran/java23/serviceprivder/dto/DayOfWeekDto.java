package telran.java23.serviceprivder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DayOfWeekDto {
    String name;
    Boolean	isAvailable;
    String startDay;
    String endDay;
    Integer	breakeInMinute;
    String breakStart;


}
