package common.dto;

import lombok.Data;

@Data
public class EventInternalDto {
    private Long id;
    private String state;
    private Long initiatorId;
    private Integer participantLimit;
    private Boolean requestModeration;
}
