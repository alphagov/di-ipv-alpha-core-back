package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.data.VerificationType;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;

@Data
public class ActivityHistoryDto {

    private Object activityHistoryData;

    private Score activityHistoryScore;
}
