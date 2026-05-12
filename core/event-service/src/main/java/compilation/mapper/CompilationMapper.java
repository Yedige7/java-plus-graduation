package compilation.mapper;

import compilation.dto.CompilationDto;
import compilation.model.Compilation;
import event.mapper.EventMapper;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation c) {
        return new CompilationDto(
                c.getId(),
                c.getTitle(),
                c.getPinned(),
                c.getEvents().stream()
                        .map(e -> EventMapper.mapToEventShortDto(e, null, null, 0L, 0L))
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

}
