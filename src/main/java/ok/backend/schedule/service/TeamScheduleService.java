package ok.backend.schedule.service;

import ok.backend.schedule.domain.entity.TeamSchedule;
import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
import ok.backend.schedule.domain.repository.TeamScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamScheduleService {

    @Autowired
    private TeamScheduleRepository teamScheduleRepository;

}