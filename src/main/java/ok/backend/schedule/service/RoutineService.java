package ok.backend.schedule.service;

import ok.backend.schedule.domain.entity.Routine;
import ok.backend.schedule.dto.req.RoutineRequestDto;
import ok.backend.schedule.dto.res.RoutineResponseDto;
import ok.backend.schedule.domain.repository.RoutineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    @Autowired
    private RoutineRepository routineRepository;

}