package com.insightsystems.dal.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeDTO {
    @Nullable
    public Boolean use24HourTimeFormat;
    public TimezoneDTO timezone = null;

    public TimeDTO() {    }

    public TimeDTO(boolean use24HourTimeFormat) {
        this.use24HourTimeFormat = use24HourTimeFormat;
    }

    public TimeDTO(String id) {
        this.timezone = new TimezoneDTO(id);
    }
}
