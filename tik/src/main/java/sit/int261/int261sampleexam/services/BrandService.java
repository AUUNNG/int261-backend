package sit.int261.int261sampleexam.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int261.int261sampleexam.dtos.BrandResponse;
import sit.int261.int261sampleexam.repositories.BrandRepository;

import java.util.List;

@Service
public class BrandService {
    private final BrandRepository brandRepository;
    private final ModelMapper modelMapper;

    public BrandService(BrandRepository brandRepository, ModelMapper modelMapper) {
        this.brandRepository = brandRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAllByOrderByIdAsc().stream()
                .map(brand -> modelMapper.map(brand, BrandResponse.class))
                .toList();
    }
}
