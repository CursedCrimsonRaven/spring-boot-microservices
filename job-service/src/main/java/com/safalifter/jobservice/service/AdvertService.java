package com.safalifter.jobservice.service;

import com.safalifter.jobservice.client.FileStorageClient;
import com.safalifter.jobservice.client.UserServiceClient;
import com.safalifter.jobservice.enums.AdvertStatus;
import com.safalifter.jobservice.enums.Advertiser;
import com.safalifter.jobservice.exc.NotFoundException;
import com.safalifter.jobservice.model.Advert;
import com.safalifter.jobservice.model.Job;
import com.safalifter.jobservice.repository.AdvertRepository;
import com.safalifter.jobservice.request.advert.AdvertCreateRequest;
import com.safalifter.jobservice.request.advert.AdvertUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdvertService {
    private final AdvertRepository advertRepository;
    private final JobService jobService;
    private final UserServiceClient userServiceclient;
    private final FileStorageClient fileStorageClient;

    public Advert createAdvert(AdvertCreateRequest request, MultipartFile file) {
        String userId = isTheUserRegistered(request.getUserId());
        Job job = jobService.getJobById(request.getJobId());

        String imageId = null;

        if (file != null)
            imageId = fileStorageClient.uploadImageToFIleSystem(file).getBody();

        Advert toSave = Advert.builder()
                .userId(userId)
                .job(job)
                .name(request.getName())
                .advertiser(request.getAdvertiser())
                .deliveryTime(request.getDeliveryTime())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(AdvertStatus.OPEN)
                .imageId(imageId)
                .build();
        return advertRepository.save(toSave);
    }

    public List<Advert> getAll() {
        return advertRepository.findAll();
    }

    public Advert getAdvertById(String id) {
        return findAdvertById(id);
    }

    public List<Advert> getAdvertsByUserId(String id, Advertiser type) {
        String userId = isTheUserRegistered(id);
        return advertRepository.getAdvertsByUserIdAndAdvertiser(userId, type);
    }

    public Advert updateAdvertById(AdvertUpdateRequest request, MultipartFile file) {
        Advert toUpdate = findAdvertById(request.getId());
        toUpdate.setName(Optional.ofNullable(request.getName()).orElse(toUpdate.getName()));
        toUpdate.setDeliveryTime(Optional.of(request.getDeliveryTime()).orElse(toUpdate.getDeliveryTime()));
        toUpdate.setDescription(Optional.ofNullable(request.getDescription()).orElse(toUpdate.getDescription()));
        toUpdate.setPrice(Optional.of(request.getPrice()).orElse(toUpdate.getPrice()));
        toUpdate.setStatus(Optional.ofNullable(request.getStatus()).orElse(toUpdate.getStatus()));

        if (file != null) {
            String imageId = fileStorageClient.uploadImageToFIleSystem(file).getBody();
            if (imageId != null) {
                fileStorageClient.deleteImageFromFileSystem(toUpdate.getImageId());
                toUpdate.setImageId(imageId);
            }
        }

        return advertRepository.save(toUpdate);
    }

    public void deleteAdvertById(String id) {
        advertRepository.deleteById(id);
    }

    protected Advert findAdvertById(String id) {
        return advertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Advert not found"));
    }

    protected String isTheUserRegistered(String id) {
        return Objects.requireNonNull(userServiceclient.getUserById(id).getBody()).getId();
    }
}
