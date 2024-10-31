//package ok.backend.schedule.dto.res;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import ok.backend.schedule.domain.entity.Routine;
//import ok.backend.schedule.domain.enums.DayOfWeek;
//import ok.backend.schedule.domain.enums.RepeatType;
//
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//public class RoutineResponseDto {
//    private Long id;
//    private Long scheduleId;
//    private RepeatType repeatType;
//    private Integer repeatInterval;
//    private DayOfWeek[] dayOfWeek;
//    private Integer[] dayOfMonth;
//    private String endDate;
//
//    public RoutineResponseDto(Routine routine) {
//        this.id = routine.getId();
//        this.scheduleId = routine.getSchedule().getId();
//        this.repeatType = routine.getRepeatType();
//        this.repeatInterval = routine.getRepeatInterval();
//        this.dayOfWeek = routine.getDayOfWeek().toArray(new DayOfWeek[0]);
//        this.dayOfMonth = routine.getDayOfMonth().stream().toArray(Integer[]::new);
//        this.endDate = routine.getEndDate().toString();
//    }
//}