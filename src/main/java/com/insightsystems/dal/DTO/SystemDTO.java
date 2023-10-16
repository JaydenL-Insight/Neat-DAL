package com.insightsystems.dal.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemDTO {
    @Nullable
    public Boolean autoWakeEnabled;
    @Nullable
    public Boolean shareAnalyticsDisabled;

    public SystemDTO(){}
    public SystemDTO(@Nullable Boolean autoWakeEnabled, @Nullable Boolean shareAnalyticsDisabled) {
        this.autoWakeEnabled = autoWakeEnabled;
        this.shareAnalyticsDisabled = shareAnalyticsDisabled;
    }
}
