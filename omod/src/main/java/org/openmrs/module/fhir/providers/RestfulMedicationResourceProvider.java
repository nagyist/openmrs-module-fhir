package org.openmrs.module.fhir.providers;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir.resources.FHIRMedicationResource;

import java.util.List;

public class RestfulMedicationResourceProvider implements IResourceProvider {

    private static final String SUCCESFULL_CREATE_MESSAGE = "Medication successfully created with id %s";
    private static final String SUCCESFULL_UPDATE_MESSAGE = "Medication successfully updated with id %s";

    private FHIRMedicationResource medicationResource;

    public RestfulMedicationResourceProvider() {
        medicationResource = new FHIRMedicationResource();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }

    @Create()
    public MethodOutcome createFHIRMedication(@ResourceParam Medication medication) {
        Medication createdMedication = medicationResource.createMedication(medication);
        return createMethodOutcome(createdMedication.getId(), SUCCESFULL_CREATE_MESSAGE);
    }

    @Update()
    public MethodOutcome updateFHIRMedication(@ResourceParam Medication medication, @IdParam IdType id) {
        Medication updatedMedication = medicationResource.updateMedication(medication, id.getIdPart());
        return createMethodOutcome(updatedMedication.getId(), SUCCESFULL_UPDATE_MESSAGE);
    }

    @Read()
    public Medication getResourceById(@IdParam IdType id) {
        return medicationResource.getByUniqueId(id);
    }

    @Search()
    public List<Medication> searchMedicationById(
            @RequiredParam(name = Medication.SP_RES_ID)TokenParam id) {
        return medicationResource.searchMedicationById(id);
    }

    private MethodOutcome createMethodOutcome(String resourceId, String messagePattern) {
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(new IdType(Medication.class.getSimpleName(), resourceId));

        OperationOutcome outcome = new OperationOutcome();
        CodeableConcept concept = new CodeableConcept();
        Coding coding = concept.addCoding();
        coding.setDisplay(String.format(messagePattern, resourceId));
        outcome.addIssue().setDetails(concept);
        retVal.setOperationOutcome(outcome);
        return retVal;
    }
}
