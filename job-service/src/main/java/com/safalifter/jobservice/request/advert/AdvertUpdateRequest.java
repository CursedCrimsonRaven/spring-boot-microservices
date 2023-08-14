package com.safalifter.jobservice.request.advert;

import com.safalifter.jobservice.enums.AdvertStatus;
import lombok.Data;

@Data
public class AdvertUpdateRequest {
    private String id;
    private String name;
    private String description;
    private int deliveryTime;
    private int price;
    private AdvertStatus status;
}
