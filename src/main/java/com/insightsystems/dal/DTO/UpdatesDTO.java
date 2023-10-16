package com.insightsystems.dal.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;


import javax.annotation.Nullable;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatesDTO {
    @Nullable
    public String channel;

    public String type;
    public UpdatesDTO(){}
    public UpdatesDTO(@Nullable String channel, @Nullable String type) {
        this.type = Objects.requireNonNullElse(type, "AUTOMATIC");

        if (channel == null && type != null && type.equals("AUTOMATIC")){
            this.channel = "stable";
        } else {
            this.channel = channel;
        }
    }

    @Override
    public String toString() {
        return "{\"channel\": \""+(this.channel == null ? "null" : this.channel)+"\", \"type\": \""+this.type+"\"}";
    }
}
