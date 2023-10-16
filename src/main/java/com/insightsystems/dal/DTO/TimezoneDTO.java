package com.insightsystems.dal.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class TimezoneDTO {
    public String id;
    public TimezoneDTO(){}
    public TimezoneDTO(String id) {
        this.id = id;
    }


}
