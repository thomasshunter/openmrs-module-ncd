/**
 * Auto generated file comment
 */
package org.openmrs.module.ncd.database.dao;

import java.util.List;

import org.openmrs.module.ncd.database.NlpCriticConcept;

/**
 *
 */
public interface INlpCriticConceptDAO {
    public List<NlpCriticConcept> list();
    public void save(NlpCriticConcept concept);
    public void delete(NlpCriticConcept concept);
}
