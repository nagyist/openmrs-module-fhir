package org.openmrs.module.fhir.api.strategies.medication;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.openmrs.Drug;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir.api.util.FHIRMedicationUtil;
import org.openmrs.module.fhir.api.util.FHIRUtils;

import java.util.ArrayList;
import java.util.List;

public class MedicationStrategy implements GenericMedicationStrategy {
    @Override
    public Medication getMedicationById(String uuid) {
        Drug drug = getConceptService().getDrugByUuid(uuid);
        if (drug == null) {
            return null;
        }
        return FHIRMedicationUtil.generateMedication(drug);
    }

    @Override
    public List<Medication> searchMedicationById(String uuid) {
        Drug drug = getConceptService().getDrugByUuid(uuid);
        List<Medication> medicationList = new ArrayList<>();
        if (drug != null) {
            medicationList.add(FHIRMedicationUtil.generateMedication(drug));
        }
        return medicationList;
    }

    @Override
    public Medication createMedication(Medication medication) {
        List<String> errors = new ArrayList<>();

        Drug drug = FHIRMedicationUtil.generateDrug(medication, errors);

        FHIRUtils.checkGeneratorErrorList(errors);

        try {
            drug = getConceptService().saveDrug(drug);
        } catch (APIException e) {
            throw new UnprocessableEntityException(
                    "The request cannot be processed due to the following issues \n" + e.getMessage());
        }

        return FHIRMedicationUtil.generateMedication(drug);
    }

    @Override
    public Medication updateMedication(Medication medication, String uuid) {
        Drug drug = getConceptService().getDrug(FHIRUtils.extractUuid(uuid));

        return drug != null ? updateDrug(medication, drug) : createMedication(medication, uuid);
    }

    private Medication createMedication(Medication medication, String uuid) {
        if (medication.getId() == null) {
            IdType idType = new IdType();
            idType.setValue(uuid);
            medication.setId(idType);
        }
        return createMedication(medication);
    }

    private Medication updateDrug(Medication medication, Drug drugToUpdate) {
        List<String> errors = new ArrayList<>();

        Drug newDrug = FHIRMedicationUtil.generateDrug(medication, errors);

        FHIRUtils.checkGeneratorErrorList(errors);

        drugToUpdate = FHIRMedicationUtil.updateDrug(newDrug, drugToUpdate);
        try {
            drugToUpdate = getConceptService().saveDrug(drugToUpdate);
        } catch (APIException e) {
            throw new UnprocessableEntityException(
                    "The request cannot be processed due to the following issues \n" + e.getMessage());
        }
        return FHIRMedicationUtil.generateMedication(drugToUpdate);
    }

    private ConceptService getConceptService() {
        return Context.getConceptService();
    }
}
