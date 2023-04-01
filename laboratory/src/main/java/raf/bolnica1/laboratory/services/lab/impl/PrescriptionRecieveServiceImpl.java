package raf.bolnica1.laboratory.services.lab.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import raf.bolnica1.laboratory.domain.constants.OrderStatus;
import raf.bolnica1.laboratory.domain.constants.PrescriptionStatus;
import raf.bolnica1.laboratory.domain.lab.AnalysisParameter;
import raf.bolnica1.laboratory.domain.lab.LabWorkOrder;
import raf.bolnica1.laboratory.domain.lab.ParameterAnalysisResult;
import raf.bolnica1.laboratory.domain.lab.Prescription;
import raf.bolnica1.laboratory.dto.prescription.PrescriptionAnalysisDto;
import raf.bolnica1.laboratory.dto.prescription.PrescriptionCreateDto;
import raf.bolnica1.laboratory.dto.prescription.PrescriptionDto;
import raf.bolnica1.laboratory.dto.prescription.PrescriptionUpdateDto;
import raf.bolnica1.laboratory.mappers.PrescriptionMapper;
import raf.bolnica1.laboratory.mappers.PrescriptionRecieveMapper;
import raf.bolnica1.laboratory.repository.AnalysisParameterRepository;
import raf.bolnica1.laboratory.repository.LabWorkOrderRepository;
import raf.bolnica1.laboratory.repository.ParameterAnalysisResultRepository;
import raf.bolnica1.laboratory.repository.PrescriptionRepository;
import raf.bolnica1.laboratory.services.lab.LabWorkOrdersService;
import raf.bolnica1.laboratory.services.lab.PrescriptionRecieveService;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PrescriptionRecieveServiceImpl implements PrescriptionRecieveService {

    private final ParameterAnalysisResultRepository parameterAnalysisResultRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AnalysisParameterRepository analysisParameterRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionRecieveMapper prescriptionrecieveMapper;

    private final LabWorkOrdersService labWorkOrdersService;
    private final LabWorkOrderRepository labWorkOrderRepository;

    @Override
    public void createPrescription(PrescriptionCreateDto dto) {
        Prescription prescription = prescriptionMapper.toEntity(dto);
        prescription = prescriptionRepository.save(prescription);

        LabWorkOrder labWorkOrder=labWorkOrdersService.createWorkOrder(prescription);

        for(PrescriptionAnalysisDto prescriptionAnalysisDto : dto.getPrescriptionAnalysisDtos()){
            Long analysisId=prescriptionAnalysisDto.getAnalysisId();
            for(Long parameterId:prescriptionAnalysisDto.getParametersIds()){
                AnalysisParameter analysisParameter=analysisParameterRepository.findAnalysisParameterByAnalysisIdAndParameterId(analysisId,parameterId);

                ParameterAnalysisResult parameterAnalysisResult=new ParameterAnalysisResult();
                parameterAnalysisResult.setAnalysisParameter(analysisParameter);
                parameterAnalysisResult.setLabWorkOrder(labWorkOrder);
                parameterAnalysisResultRepository.save(parameterAnalysisResult);
            }
        }
    }

    @Override
    public void updatePrescription(PrescriptionUpdateDto dto) {
        Prescription prescription = prescriptionRepository.findById(dto.getId()).orElse(null);
        if(prescription != null && prescription.getStatus().equals(PrescriptionStatus.NEREALIZOVAN)){
            prescription = prescriptionMapper.toEntityUpdate(dto, prescription);

            LabWorkOrder labWorkOrder = labWorkOrderRepository.findByPrescription(prescription.getId()).orElse(null);
            if(labWorkOrder != null && labWorkOrder.getStatus().equals(OrderStatus.NEOBRADJEN)){
                parameterAnalysisResultRepository.deleteAll(parameterAnalysisResultRepository.findParameterAnalysisResultsByLabWorkOrderId(labWorkOrder.getId()));

                for(PrescriptionAnalysisDto prescriptionAnalysisDto : dto.getPrescriptionAnalysisDtos()) {
                    Long analysisId = prescriptionAnalysisDto.getAnalysisId();
                    for (Long parameterId : prescriptionAnalysisDto.getParametersIds()) {
                        AnalysisParameter analysisParameter = analysisParameterRepository.findAnalysisParameterByAnalysisIdAndParameterId(analysisId, parameterId);

                        ParameterAnalysisResult parameterAnalysisResult = new ParameterAnalysisResult();
                        parameterAnalysisResult.setAnalysisParameter(analysisParameter);
                        parameterAnalysisResult.setLabWorkOrder(labWorkOrder);
                        parameterAnalysisResultRepository.save(parameterAnalysisResult);
                    }
                }
            }
            prescriptionRepository.save(prescription);
        }
    }

    @Override
    public void deletePrescription(Long id, String lbz) {
        Prescription prescription = prescriptionRepository.findById(id).orElse(null);
        if(prescription == null)
            return;

        if(!prescription.getDoctorLbz().equals(lbz)){
            return;
        }

        LabWorkOrder labWorkOrder = labWorkOrderRepository.findByPrescription(prescription.getId()).orElse(null);
        if(labWorkOrder != null) {
            labWorkOrdersService.deleteWorkOrder(labWorkOrder);
        }
        prescriptionRepository.delete(prescription);
    }

    @Override
    public Page<PrescriptionDto> findPrescriptionsForPatient(String lbp, String doctorLbz, int page, int size) {
        List<Prescription> prescriptions = prescriptionRepository.findPrescriptionsByLbpAndDoctorLbz(lbp, doctorLbz);
        List<PrescriptionDto> prescriptionDtos = new ArrayList<>();
        for(Prescription prescription : prescriptions){
            prescriptionDtos.add(prescriptionrecieveMapper.toPrescriptionDto(prescription));
        }

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, prescriptionDtos.size());

        List<PrescriptionDto> sublist = prescriptionDtos.subList(startIndex, endIndex);

        Page<PrescriptionDto> paged = new PageImpl<>(sublist, PageRequest.of(page, size), prescriptionDtos.size());
        return paged;
    }
}