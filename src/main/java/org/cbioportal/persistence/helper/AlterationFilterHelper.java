package org.cbioportal.persistence.helper;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

public final class AlterationFilterHelper {
   
   public static AlterationFilterHelper build(@Nullable AlterationFilter alterationFilter) {
       if (Objects.isNull(alterationFilter)) {
           alterationFilter = new AlterationFilter();
       }
       return new AlterationFilterHelper(alterationFilter);
   } 
    
    private final AlterationFilter alterationFilter;
    private final Select<String> mappedMutationTypes;
    
    private final Select<Short> mappedCnaTypes;
   
    private AlterationFilterHelper(@NonNull AlterationFilter alterationFilter){
        this.alterationFilter = alterationFilter;
        this.mappedMutationTypes = buildMutationTypeList();
        this.mappedCnaTypes = buildCnaTypeList();
    }
    
    private Select<String> buildMutationTypeList() {
        if (alterationFilter.getMutationTypeSelect().hasNone()) {
            return Select.none();
        }
        if (alterationFilter.getMutationTypeSelect().hasAll()) {
            return Select.all();
        }
        Select<String> typeSelects = alterationFilter.getMutationTypeSelect().map(MutationEventType::getMutationType);
        typeSelects.inverse(alterationFilter.getMutationTypeSelect().inverse());

        return typeSelects; 
    }

    public Select<String> getMutationTypeList() {
       return mappedMutationTypes; 
    }
    
    public Select<Short> getCnaTypeList() {
        return mappedCnaTypes;
    }

    public Select<Short> buildCnaTypeList() {
        if (alterationFilter.getCNAEventTypeSelect().hasNone()) {
            return Select.none();
        }
        if (alterationFilter.getCNAEventTypeSelect().hasAll()) {
            return Select.all();
        }
        return alterationFilter.getCNAEventTypeSelect().map(CNA::getCode);
    }
    
    public boolean hasDriver() {
        return alterationFilter.getIncludeDriver();
    }
    
    public boolean hasVUSDriver() {
        return alterationFilter.getIncludeVUS();
    }
    
    public boolean hasUnknownOncogenicity() {
        return alterationFilter.getIncludeUnknownOncogenicity();
    }
    
    public boolean hasGermline() {
        return alterationFilter.getIncludeGermline();
    }
    
    public boolean hasSomatic() {
        return alterationFilter.getIncludeSomatic();
    }
    
    public boolean hasUnknownMutationStatus() {
        return alterationFilter.getIncludeUnknownStatus();
    }
    
    public Select<String> getSelectedTiers() {
        return alterationFilter.getSelectedTiers();
    }
    
    public boolean hasUnknownTier() {
        return alterationFilter.getIncludeUnknownTier();
    }
    
    public boolean isAllDriverAnnotationSelected() {
        return alterationFilter.getIncludeDriver() && alterationFilter.getIncludeVUS() && alterationFilter.getIncludeUnknownOncogenicity();
    }
    
    public boolean isNoDriverAnnotationSelected() {
        return !alterationFilter.getIncludeDriver() && !alterationFilter.getIncludeVUS() && !alterationFilter.getIncludeUnknownOncogenicity();
    }
    
    public boolean isSomeDriverAnnotationsSelected() {
        return !isAllDriverAnnotationSelected() && !isNoDriverAnnotationSelected();
    }

    public boolean isAllMutationStatusSelected() {
        return alterationFilter.getIncludeGermline() 
            && alterationFilter.getIncludeSomatic() 
            && alterationFilter.getIncludeUnknownStatus();
    }

    public boolean isNoMutationStatusSelected() {
        return !alterationFilter.getIncludeGermline() 
            && !alterationFilter.getIncludeSomatic() 
            && !alterationFilter.getIncludeUnknownStatus();
    }

    public boolean isSomeMutationStatusSelected() {
        return !isAllMutationStatusSelected() && !isNoMutationStatusSelected();
    } 
    
    public boolean isAllTierOptionsSelected() {
        return !Objects.isNull(alterationFilter.getSelectedTiers()) 
            && alterationFilter.getSelectedTiers().hasAll() 
            && alterationFilter.getIncludeUnknownTier();
    }
    
    public boolean isNoTierOptionsSelected() {
        return (Objects.isNull(alterationFilter.getSelectedTiers()) || alterationFilter.getSelectedTiers().hasNone())
            && !alterationFilter.getIncludeUnknownTier();
    }
    
    public boolean isSomeTierOptionsSelected() {
        return !isAllTierOptionsSelected() && !isNoTierOptionsSelected();
    }
    
    public boolean shouldApplyMutationAlterationFilter() {
        return isSomeDriverAnnotationsSelected() 
            || isSomeMutationStatusSelected() 
            || isSomeTierOptionsSelected()
            || mappedMutationTypes.hasNone() 
            || (!mappedMutationTypes.hasNone() && !mappedMutationTypes.hasAll());
    }
    
    public boolean shouldApplyCnaAlterationFilter() {
        return isSomeDriverAnnotationsSelected()
            || mappedCnaTypes.hasNone() 
            || (!mappedCnaTypes.hasNone() && !mappedCnaTypes.hasAll());
    }
}
