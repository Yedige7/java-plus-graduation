package compilation.service;

import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {


    CompilationDto create(NewCompilationDto dto);

    CompilationDto update(Long compId, UpdateCompilationRequest dto);

    void delete(Long compId);

    List<CompilationDto> findAll(Boolean pinned, int from, int size);

    CompilationDto findById(Long compId);
}
