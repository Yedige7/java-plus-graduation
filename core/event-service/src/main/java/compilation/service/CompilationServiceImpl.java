package compilation.service;

import common.exception.NotFoundException;
import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;
import compilation.mapper.CompilationMapper;
import compilation.model.Compilation;
import compilation.repository.CompilationRepository;
import event.model.Event;
import event.repository.DatabaseEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final DatabaseEventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation comp = new Compilation();
        comp.setTitle(dto.getTitle());
        comp.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        Set<Event> events = loadEvents(dto.getEvents());
        comp.setEvents(events);

        Compilation saved = compilationRepository.save(comp);
        return CompilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        if (dto.getTitle() != null) {
            comp.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            comp.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            comp.setEvents(loadEvents(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(comp);
        return CompilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> comps = (pinned == null)
                ? compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findAllByPinned(pinned, pageable);

        return comps.stream()
                .map(CompilationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto findById(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));
        return CompilationMapper.toDto(comp);
    }

    private Set<Event> loadEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.size() != eventIds.size()) {
            throw new NotFoundException("Some events not found");
        }

        return new HashSet<>(events);
    }
}